/*******************************************************************************
 * Copyright (c) 2018 SOM Research Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Abel G�mez - initial API and implementation
 *******************************************************************************/
package edu.uoc.som.temf.estores.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.InternalEObject.EStore;

import edu.uoc.som.temf.estores.TStore;

/**
 * A simple {@link EStore} implementation that uses synchronized collections to
 * store the data in memory.
 * 
 * @author agomez
 * 
 */
public class TransientTStoreImpl implements TStore {

	protected Map<EStoreEntryKey, Object> singleMap = new HashMap<EStoreEntryKey, Object>();
	protected Map<EStoreEntryKey, List<Object>> manyMap = new HashMap<EStoreEntryKey, List<Object>>();

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
			return singleMap.get(entry);
		} else {
			List<Object> saved = manyMap.get(entry);
			if (saved != null) {
				return saved.get(index);
			} else {
				return null;
			}
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
			return singleMap.put(entry, value);
		} else {
			return manyMap.get(entry).set(index, value);
		}
	}

	@Override
	public void add(InternalEObject eObject, EStructuralFeature feature, int index, Object value) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		List<Object> saved = manyMap.get(entry);
		if (saved != null) {
			saved.add(index, value);
		} else {
			List<Object> list = new ArrayList<Object>();
			list.add(value);
			manyMap.put(entry, list);
		}
	}

	@Override
	public Object remove(InternalEObject eObject, EStructuralFeature feature, int index) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		return manyMap.get(entry).remove(index);
	}

	@Override
	public Object move(InternalEObject eObject, EStructuralFeature feature, int targetIndex, int sourceIndex) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		List<Object> list = manyMap.get(entry);
		Object movedObject = list.remove(sourceIndex);
		list.add(targetIndex, movedObject);
		return movedObject;
	}

	@Override
	public void clear(InternalEObject eObject, EStructuralFeature feature) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		List<Object> list = manyMap.get(entry);
		if (list != null) {
			list.clear();
		}
	}

	@Override
	public boolean isSet(InternalEObject eObject, EStructuralFeature feature) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		if (!feature.isMany()) {
			return singleMap.containsKey(entry);
		} else {
			return manyMap.containsKey(entry);
		}
	}

	@Override
	public boolean isSetAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void unset(InternalEObject eObject, EStructuralFeature feature) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		if (!feature.isMany()) {
			singleMap.remove(entry);
		} else {
			manyMap.remove(entry);
		}
	}

	@Override
	public int size(InternalEObject eObject, EStructuralFeature feature) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		List<Object> list = manyMap.get(entry);
		return list != null ? list.size() : 0;
	}

	@Override
	public int sizeAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(InternalEObject eObject, EStructuralFeature feature, Object value) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		List<Object> list = manyMap.get(entry);
		return list != null ? list.indexOf(value) : -1;
	}

	@Override
	public int indexOfAt(Instant instant, InternalEObject object, EStructuralFeature feature, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int lastIndexOf(InternalEObject eObject, EStructuralFeature feature, Object value) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		List<Object> list = manyMap.get(entry);
		return list != null ? list.lastIndexOf(value) : -1;
	}

	@Override
	public int lastIndexOfAt(Instant instant, InternalEObject object, EStructuralFeature feature, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray(InternalEObject eObject, EStructuralFeature feature) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		List<Object> list = manyMap.get(entry);
		return list != null ? list.toArray() : new Object[] {};
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
		List<Object> list = manyMap.get(entry);
		return list != null ? list.toArray(array) : Arrays.copyOf(array, 0);
	}

	@Override
	public <T> T[] toArrayAt(Instant instant, InternalEObject object, EStructuralFeature feature, T[] array) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isEmpty(InternalEObject eObject, EStructuralFeature feature) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		List<Object> list = manyMap.get(entry);
		return list != null ? list.isEmpty() : true;
	}

	@Override
	public boolean isEmptyAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(InternalEObject eObject, EStructuralFeature feature, Object value) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		List<Object> list = manyMap.get(entry);
		return list != null ? list.contains(value) : false;
	}

	@Override
	public boolean containsAt(Instant instant, InternalEObject object, EStructuralFeature feature, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode(InternalEObject eObject, EStructuralFeature feature) {
		EStoreEntryKey entry = new EStoreEntryKey(eObject, feature);
		return manyMap.get(entry).hashCode();
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
