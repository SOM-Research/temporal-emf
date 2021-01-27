/*******************************************************************************
 * Copyright (c) 2018 SOM Research Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Abel Gómez - initial API and implementation
 *******************************************************************************/
package edu.uoc.som.temf.tstores.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.InternalEObject.EStore;

import edu.uoc.som.temf.core.TGlobalClock;
import edu.uoc.som.temf.tstores.TStore;

/**
 * A simple {@link EStore} implementation that uses synchronized collections to
 * store the data in memory.
 * 
 * @author agomez
 * 
 */
public class TransientTStoreImpl implements TStore {

	protected Map<EStoreEntryKey, NavigableMap<Instant, Object>> singleMap = new HashMap<>();
	protected Map<EStoreEntryKey, NavigableMap<Instant, Object[]>> manyMap = new HashMap<>();

	public class EStoreEntryKey {

		protected InternalEObject eObject;
		protected EStructuralFeature eStructuralFeature;

		public EStoreEntryKey(InternalEObject eObject, EStructuralFeature eStructuralFeature) {
			this.eObject = eObject;
			this.eStructuralFeature = eStructuralFeature;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((eObject == null) ? 0 : eObject.hashCode());
			result = prime * result + ((eStructuralFeature == null) ? 0 : eStructuralFeature.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EStoreEntryKey other = (EStoreEntryKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (eObject == null) {
				if (other.eObject != null)
					return false;
			} else if (!eObject.equals(other.eObject))
				return false;
			if (eStructuralFeature == null) {
				if (other.eStructuralFeature != null)
					return false;
			} else if (!eStructuralFeature.equals(other.eStructuralFeature))
				return false;
			return true;
		}

		private TransientTStoreImpl getOuterType() {
			return TransientTStoreImpl.this;
		}

		public InternalEObject getEObject() {
			return eObject;
		}

		public EStructuralFeature getEStructuralFeature() {
			return eStructuralFeature;
		}
	}

	@Override
	public Object get(InternalEObject eObject, EStructuralFeature feature, int index) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		if (index == NO_INDEX) {
			Entry<Instant, Object> lastEntry = singleMap.getOrDefault(entry, new TreeMap<>()).lastEntry();
			return lastEntry != null ? lastEntry.getValue() : null;
		} else {
			Entry<Instant, Object[]> lastEntry = manyMap.getOrDefault(entry, new TreeMap<>()).lastEntry();
			return lastEntry != null ? lastEntry.getValue()[index] :  null;
		}
	}

	@Override
	public Object getAt(Instant instant, InternalEObject object, EStructuralFeature feature, int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SortedMap<Instant, Object> getAllBetween(Instant startInstant, Instant endInstant, InternalEObject object, EStructuralFeature feature, int index) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Object set(InternalEObject eObject, EStructuralFeature feature, int index, Object value) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		if (index == NO_INDEX) {
			singleMap.putIfAbsent(entry, new TreeMap<>());
			return singleMap.get(entry).put(TGlobalClock.INSTANCE.instant(), value);
		} else {
			Object[] newValues = manyMap.get(entry).lastEntry().getValue().clone();
			newValues[index] = value;
			return manyMap.get(entry).put(TGlobalClock.INSTANCE.instant(), newValues);
		}
	}

	@Override
	public void add(InternalEObject eObject, EStructuralFeature feature, int index, Object value) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		manyMap.putIfAbsent(entry, new TreeMap<>());
		NavigableMap<Instant, Object[]> saved = manyMap.get(entry);
		if (saved != null) {
			Object[] values = saved.lastEntry().getValue();
			saved.put(TGlobalClock.INSTANCE.instant(), ArrayUtils.add(values, value));
		} else {
			Object[] values = new Object[] { value };
			NavigableMap<Instant, Object[]> map = new TreeMap<>();
			map.put(TGlobalClock.INSTANCE.instant(), values);
			manyMap.put(entry, map);
		}
	}

	@Override
	public Object remove(InternalEObject eObject, EStructuralFeature feature, int index) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		Object[] values = manyMap.get(entry).lastEntry().getValue();
		Object value = values[index];
		manyMap.get(entry).put(TGlobalClock.INSTANCE.instant(), ArrayUtils.remove(values, index));
		return value;
	}

	@Override
	public Object move(InternalEObject eObject, EStructuralFeature feature, int targetIndex, int sourceIndex) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		Object[] values = manyMap.get(entry).lastEntry().getValue();
		Object value = values[sourceIndex];
		values = ArrayUtils.remove(values, sourceIndex);
		manyMap.get(entry).put(TGlobalClock.INSTANCE.instant(), ArrayUtils.add(values, targetIndex, value));
		return value;
	}

	@Override
	public void clear(InternalEObject eObject, EStructuralFeature feature) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		manyMap.putIfAbsent(entry, new TreeMap<>());
		manyMap.get(entry).put(TGlobalClock.INSTANCE.instant(), new Object[] {});
	}

	@Override
	public boolean isSet(InternalEObject eObject, EStructuralFeature feature) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		if (!feature.isMany()) {
			Entry<Instant, Object> lastEntry = singleMap.getOrDefault(entry, new TreeMap<>()).lastEntry();
			return lastEntry != null ? lastEntry.getValue() != null : false;
		} else {
			Entry<Instant, Object[]> lastEntry = manyMap.getOrDefault(entry, new TreeMap<>()).lastEntry();
			return lastEntry != null ? lastEntry.getValue() != null && ((Object[]) lastEntry.getValue()).length > 0 : false;
		}
	}

	@Override
	public boolean isSetAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Instant whenSet(InternalEObject object, EStructuralFeature feature) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void unset(InternalEObject eObject, EStructuralFeature feature) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		if (!feature.isMany()) {
			singleMap.putIfAbsent(entry, new TreeMap<>());
			singleMap.get(entry).put(TGlobalClock.INSTANCE.instant(), null);
		} else {
			manyMap.putIfAbsent(entry, new TreeMap<>());
			manyMap.get(entry).put(TGlobalClock.INSTANCE.instant(), new Object[] {});
		}
	}

	@Override
	public int size(InternalEObject eObject, EStructuralFeature feature) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		NavigableMap<Instant, Object[]> values = manyMap.getOrDefault(entry, new TreeMap<>());
		return values.lastEntry() != null ? values.lastEntry().getValue().length : 0;
	}

	@Override
	public int sizeAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(InternalEObject eObject, EStructuralFeature feature, Object value) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		NavigableMap<Instant, Object[]> values = manyMap.getOrDefault(entry, new TreeMap<>());
		return values.lastEntry() != null ? ArrayUtils.indexOf(values.lastEntry().getValue(), value) : -1;
	}

	@Override
	public int indexOfAt(Instant instant, InternalEObject object, EStructuralFeature feature, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int lastIndexOf(InternalEObject eObject, EStructuralFeature feature, Object value) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		NavigableMap<Instant, Object[]> values = manyMap.getOrDefault(entry, new TreeMap<>());
		return values.lastEntry() != null ? ArrayUtils.lastIndexOf(values.lastEntry().getValue(), value) : -1;
	}

	@Override
	public int lastIndexOfAt(Instant instant, InternalEObject object, EStructuralFeature feature, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray(InternalEObject eObject, EStructuralFeature feature) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		NavigableMap<Instant, Object[]> values = manyMap.getOrDefault(entry, new TreeMap<>());
		return values.lastEntry() != null ? values.lastEntry().getValue().clone() : new Object[] {};
	}

	@Override
	public Object[] toArrayAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public SortedMap<Instant, Object[]> toArrayAllBetween(Instant startInstant, Instant endInstant, InternalEObject object, EStructuralFeature feature) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public <T> T[] toArray(InternalEObject eObject, EStructuralFeature feature, T[] array) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		NavigableMap<Instant, Object[]> values = manyMap.getOrDefault(entry, new TreeMap<>());
		if (values.lastEntry() != null) {
			Object[] savedArray = values.lastEntry().getValue();
			if (array.length < savedArray.length) {
				array = Arrays.copyOf(array, savedArray.length);
			}
			System.arraycopy(savedArray, 0, array, 0, savedArray.length);
			return array;
		} else {
			return Arrays.copyOf(array, 0);
		}
	}

	@Override
	public <T> T[] toArrayAt(Instant instant, InternalEObject object, EStructuralFeature feature, T[] array) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isEmpty(InternalEObject eObject, EStructuralFeature feature) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		NavigableMap<Instant, Object[]> values = manyMap.getOrDefault(entry, new TreeMap<>());
		return values.lastEntry() != null && values.lastEntry().getValue() != null ? values.lastEntry().getValue().length > 0 : true;
	}

	@Override
	public boolean isEmptyAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(InternalEObject eObject, EStructuralFeature feature, Object value) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		NavigableMap<Instant, Object[]> values = manyMap.getOrDefault(entry, new TreeMap<>());
		return values.lastEntry() != null && values.lastEntry().getValue() != null ? ArrayUtils.contains(values.lastEntry().getValue(), value) : false;
	}

	@Override
	public boolean containsAt(Instant instant, InternalEObject object, EStructuralFeature feature, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode(InternalEObject eObject, EStructuralFeature feature) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		return manyMap.getOrDefault(entry, new TreeMap<>()).lastEntry().hashCode();
	}

	@Override
	public int hashCodeAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		throw new UnsupportedOperationException();
	}

	@Override
	public InternalEObject getContainer(InternalEObject eObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public InternalEObject getContainerAt(Instant instant, InternalEObject object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EStructuralFeature getContainingFeature(InternalEObject eObject) {
		// This should never be called.
		throw new UnsupportedOperationException();
	}

	@Override
	public EStructuralFeature getContainingFeatureAt(Instant instant, InternalEObject object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EObject create(EClass eClass) {
		// Unimplemented
		// TODO: In which case is needed?
		throw new UnsupportedOperationException();
	}
}
