/*******************************************************************************
 * Copyright (c) 2018 Abel Gómez.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Abel Gómez - initial API and implementation
 *******************************************************************************/
package edu.uoc.som.temf.mapdb.estores.impl;

import java.io.Serializable;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.h2.mvstore.Cursor;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import edu.uoc.som.temf.Logger;
import edu.uoc.som.temf.core.InternalTObject;
import edu.uoc.som.temf.core.TObject;
import edu.uoc.som.temf.core.exceptions.EClassNotFoundException;
import edu.uoc.som.temf.core.impl.TObjectAdapterFactoryImpl;
import edu.uoc.som.temf.estores.SearcheableResourceTStore;

public class DirectWriteMapDBResourceTStoreImpl implements SearcheableResourceTStore {

	protected static final String DATA = "data";
	protected static final String INSTANCE_OF = "instanceOf";
	protected static final String CONTAINER = "eContainer";

	protected LoadingCache<String, InternalTObject> loadedEObjects = CacheBuilder.newBuilder().softValues().build(new CacheLoader<String, InternalTObject>() {
		@Override
		public InternalTObject load(String key) throws Exception {
				EClass eClass = resolveInstanceOf(key);
				if (eClass == null) {
					throw new EClassNotFoundException(MessageFormat.format("Element {0} does not have an associated EClass", key));
				}
				EObject eObject = EcoreUtil.create(eClass);
				InternalTObject tObject = TObjectAdapterFactoryImpl.getAdapter(eObject, InternalTObject.class);
				tObject.tSetId(key);
				return tObject;
			}
	});
	
	protected MVStore mvStore;
	
	protected MVMap<Object[], Object> dataMap; // Object[] must be an array of { String, String, Date }
	
	protected MVMap<String, EClassInfo> instanceOfMap;

	protected MVMap<Object[], ContainerInfo> containersMap; // Object[] must be an array of { String, Date }
	
	protected Resource.Internal resource;

	@SuppressWarnings("unchecked")
	public DirectWriteMapDBResourceTStoreImpl(Resource.Internal resource, MVStore mvStore) {
		this.mvStore = mvStore;
		this.resource = resource;
		this.dataMap = mvStore.openMap(DATA);
		this.instanceOfMap = mvStore.openMap(INSTANCE_OF);
		this.containersMap = mvStore.openMap(CONTAINER);
	}


	@Override
	public Resource.Internal getResource() {
		return resource;
	}

	@Override
	public Object get(InternalEObject object, EStructuralFeature feature, int index) {
		return getAt(null, object, feature, index);
	}

	@Override
	public Object getAt(Instant instant, InternalEObject object, EStructuralFeature feature, int index) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		if (feature instanceof EAttribute) {
			return getAt(instant, tObject, (EAttribute) feature, index);
		} else if (feature instanceof EReference) {
			return getAt(instant, tObject, (EReference) feature, index);
		} else {
			throw new IllegalArgumentException(feature.toString());
		}
	}

	protected Object getAt(Instant instant, TObject object, EAttribute eAttribute, int index) {
		Object value = getFromDataMap(instant, object, eAttribute);
		if (!eAttribute.isMany()) {
			return parseMapValue(eAttribute, (String) value);
		} else {
			String[] array = (String[]) value;
			return parseMapValue(eAttribute, array[index]);
		}
	}

	protected Object getAt(Instant instant, TObject object, EReference eReference, int index) {
		Object value = getFromDataMap(instant, object, eReference);
		if (!eReference.isMany()) {
			return getEObject((String) value);
		} else {
			String[] array = Arrays.stream((Object[]) value).toArray(String[]::new);
			return getEObject(array[index]);
		}
	}

	@Override
	public SortedMap<Instant, Object> getAllBetween(Instant startInstant, Instant endInstant, InternalEObject object, EStructuralFeature feature, int index) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		if (feature instanceof EAttribute) {
			return getAllBetween(startInstant, endInstant, tObject, (EAttribute) feature, index);
		} else if (feature instanceof EReference) {
			return getAllBetween(startInstant, endInstant, tObject, (EReference) feature, index);
		} else {
			throw new IllegalArgumentException(feature.toString());
		}
	}
	
	protected SortedMap<Instant, Object> getAllBetween(Instant startInstant, Instant endInstant, TObject object, EAttribute eAttribute, int index) {
		SortedMap<Instant, Object> result = new TreeMap<>();
		TreeMap<Object[], Object> all = getAllFromDataMap(startInstant, endInstant, object, eAttribute);
		if (!eAttribute.isMany()) {
			all.forEach((array, obj) ->  result.put(dataKeyInstant(array), parseMapValue(eAttribute, (String) obj)));
		} else {
			all.forEach((array, obj) ->  result.put(dataKeyInstant(array), parseMapValue(eAttribute, parseMapValue(eAttribute, ((String[]) obj)[index]))));
		}
		return result;
	}
	
	protected SortedMap<Instant, Object> getAllBetween(Instant startInstant, Instant endInstant, TObject object, EReference eReference, int index) {
		SortedMap<Instant, Object> result = new TreeMap<>();
		TreeMap<Object[], Object> all = getAllFromDataMap(startInstant, endInstant, object, eReference);
		if (!eReference.isMany()) {
			all.forEach((array, obj) ->  result.put(dataKeyInstant(array), getEObject((String) obj)));
		} else {
			all.forEach((array, obj) ->  result.put(dataKeyInstant(array), getEObject(((String[]) obj)[index])));
		}
		return result;
	}
	@Override
	public Object set(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		if (feature instanceof EAttribute) {
			return set(tObject, (EAttribute) feature, index, value);
		} else if (feature instanceof EReference) {
			TObject referencedEObject = TObjectAdapterFactoryImpl.getAdapter(value, TObject.class);
			return set(tObject, (EReference) feature, index, referencedEObject);
		} else {
			throw new IllegalArgumentException(feature.toString());
		}
	}

	protected Object set(TObject object, EAttribute eAttribute, int index, Object value) {
		if (!eAttribute.isMany()) {
			Object oldValue = dataMap.put(dataKey(object.tId(), eAttribute.getName()), serializeToMapValue(eAttribute, value));
			return parseMapValue(eAttribute, oldValue);
		} else {
			Object[] array = (Object[]) getFromDataMap(object, eAttribute);
			Object oldValue = array[index]; 
			array[index] = serializeToMapValue(eAttribute, value);
			dataMap.put(dataKey(object.tId(), eAttribute.getName()), array);
			return parseMapValue(eAttribute, oldValue);
		}
	}

	protected Object set(TObject object, EReference eReference, int index, TObject referencedObject) {
		updateContainment(object, eReference, referencedObject);
		updateInstanceOf(referencedObject);
		if (!eReference.isMany()) {
			Object oldId = dataMap.put(dataKey(object.tId(), eReference.getName()), referencedObject.tId());
			return oldId != null ? getEObject((String) oldId) : null;
		} else {
			Object[] array = (Object[]) getFromDataMap(object, eReference);
			Object oldId = array[index];
			array[index] = referencedObject.tId();
			dataMap.put(dataKey(object.tId(), eReference.getName()), array);
			return oldId != null ? getEObject((String) oldId) : null;
		}
	}


	@Override
	public boolean isSet(InternalEObject object, EStructuralFeature feature) {
		return isSetAt(now(), object, feature);
	}

	
	@Override
	public boolean isSetAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		return dataMap.get(dataKey(tObject.tId(), feature.getName(), instant)) != null;
	}


	@Override
	public void add(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		if (feature instanceof EAttribute) {
			add(tObject, (EAttribute) feature, index, value);
		} else if (feature instanceof EReference) {
			TObject referencedEObject = TObjectAdapterFactoryImpl.getAdapter(value, TObject.class);
			add(tObject, (EReference) feature, index, referencedEObject);
		} else {
			throw new IllegalArgumentException(feature.toString());
		}
	}

	protected void add(TObject object, EAttribute eAttribute, int index, Object value) {
		Object[] array = (Object[]) getFromDataMap(object, eAttribute);
		if (array == null) {
			array = new Object[] {};
		}
		array = ArrayUtils.add(array, index, serializeToMapValue(eAttribute, value));
		dataMap.put(dataKey(object.tId(), eAttribute.getName()), array);
	}

	protected void add(TObject object, EReference eReference, int index, TObject referencedObject) {
		updateContainment(object, eReference, referencedObject);
		updateInstanceOf(referencedObject);
		Object[] array = (Object[]) getFromDataMap(object, eReference);
		if (array == null) {
			array = new Object[] {};
		}
		array = ArrayUtils.add(array, index, referencedObject.tId());
		dataMap.put(dataKey(object.tId(), eReference.getName()), array);
	}

	@Override
	public Object remove(InternalEObject object, EStructuralFeature feature, int index) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		if (feature instanceof EAttribute) {
			return remove(tObject, (EAttribute) feature, index);
		} else if (feature instanceof EReference) {
			return remove(tObject, (EReference) feature, index);
		} else {
			throw new IllegalArgumentException(feature.toString());
		}
	}

	protected Object remove(TObject object, EAttribute eAttribute, int index) {
		Object[] array = (Object[]) getFromDataMap(object, eAttribute);
		Object oldValue = array[index];
		array = ArrayUtils.remove(array, index);
		dataMap.put(dataKey(object.tId(), eAttribute.getName()), array);
		return parseMapValue(eAttribute, oldValue);
	}

	protected Object remove(TObject object, EReference eReference, int index) {
		Object[] array = (Object[]) getFromDataMap(object, eReference);
		Object oldId = array[index];
		array = ArrayUtils.remove(array, index);
		dataMap.put(dataKey(object.tId(), eReference.getName()), array);
		return getEObject((String) oldId);

	}

	@Override
	public Object move(InternalEObject object, EStructuralFeature feature, int targetIndex, int sourceIndex) {
		Object movedElement = remove(object, feature, sourceIndex);
		add(object, feature, targetIndex, movedElement);
		return movedElement;
	}


	@Override
	public void unset(InternalEObject object, EStructuralFeature feature) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		dataMap.remove(dataKey(tObject.tId(), feature.getName()));
	}

	@Override
	public boolean isEmpty(InternalEObject object, EStructuralFeature feature) {
		return isEmptyAt(null, object, feature);
	}
	
	@Override
	public boolean isEmptyAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		return sizeAt(instant, object, feature) == 0;
	}

	@Override
	public int size(InternalEObject object, EStructuralFeature feature) {
		return sizeAt(null, object, feature);
	}
	
	@Override
	public int sizeAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		String[] array = (String[]) getFromDataMap(instant, tObject, feature);
		return array != null ? array.length : 0;
	}

	@Override
	public boolean contains(InternalEObject object, EStructuralFeature feature, Object value) {
		return containsAt(null, object, feature, value);
	}

	@Override
	public boolean containsAt(Instant instant, InternalEObject object, EStructuralFeature feature, Object value) {
		return indexOfAt(instant, object, feature, value) != -1;
	}
	
	@Override
	public int indexOf(InternalEObject object, EStructuralFeature feature, Object value) {
		return indexOfAt(null, object, feature, value);
	}
	
	@Override
	public int indexOfAt(Instant instant, InternalEObject object, EStructuralFeature feature, Object value) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		String[] array = (String[]) getFromDataMap(instant, tObject, feature);
		if (array == null) {
			return -1;
		}
		if (feature instanceof EAttribute) {
			return ArrayUtils.indexOf(array, serializeToMapValue((EAttribute) feature, value));
		} else {
			TObject childEObject = TObjectAdapterFactoryImpl.getAdapter(value, TObject.class);
			return ArrayUtils.indexOf(array, childEObject.tId());
		}
	}

	@Override
	public int lastIndexOf(InternalEObject object, EStructuralFeature feature, Object value) {
		return lastIndexOfAt(null, object, feature, value);
	}
	
	@Override
	public int lastIndexOfAt(Instant instant, InternalEObject object, EStructuralFeature feature, Object value) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		String[] array = (String[]) getFromDataMap(instant, tObject, feature);
		if (array == null) {
			return -1;
		}
		if (feature instanceof EAttribute) {
			return ArrayUtils.lastIndexOf(array, serializeToMapValue((EAttribute) feature, value));
		} else {
			TObject childEObject = TObjectAdapterFactoryImpl.getAdapter(value, TObject.class);
			return ArrayUtils.lastIndexOf(array, childEObject.tId());
		}
	}

	@Override
	public void clear(InternalEObject object, EStructuralFeature feature) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		dataMap.put(dataKey(tObject.tId(), feature.getName()), new Object[] {});
	}

	@Override
	public Object[] toArray(InternalEObject object, EStructuralFeature feature) {
		return toArrayAt(null, object, feature);
	}
	
	@Override
	public Object[] toArrayAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		int size = sizeAt(instant, object, feature);
		Object[] result = new Object[size];
		for (int index = 0; index < size; index++) {
			result[index] = getAt(instant, object, feature, index);
		}
		return result;
	}

	@Override
	public <T> T[] toArray(InternalEObject object, EStructuralFeature feature, T[] array) {
		return toArrayAt(null, object, feature, array);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArrayAt(Instant instant, InternalEObject object, EStructuralFeature feature, T[] array) {
		int size = sizeAt(instant, object, feature);
		T[] result = null;
		if (array.length < size) {
			result = Arrays.copyOf(array, size);
		} else {
			result = array;
		}
		for (int index = 0; index < size; index++) {
			result[index] = (T) getAt(instant, object, feature, index);
		}
		return result;
	}
	
	@Override
	public SortedMap<Instant, Object[]> toArrayAllBetween(Instant startInstant, Instant endInstant, InternalEObject object, EStructuralFeature feature) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);

		SortedMap<Instant, Object[]> result = new TreeMap<>();
		TreeMap<Object[], Object> all = getAllFromDataMap(startInstant, endInstant, tObject, feature);
		
		if (feature instanceof EAttribute) {
			all.forEach((array, obj) ->  result.put(dataKeyInstant(array), Arrays.asList((String[]) obj).stream().map(v -> parseMapValue((EAttribute) feature, (String) v)).toArray()));
		} else if (feature instanceof EReference) {
			all.forEach((array, obj) ->  result.put(dataKeyInstant(array), Arrays.asList((String[]) obj).stream().map(v -> getEObject((String) v)).toArray()));
		} else {
			throw new IllegalArgumentException(feature.toString());
		}
		
		return result;
	}
	@Override
	public int hashCode(InternalEObject object, EStructuralFeature feature) {
		return hashCodeAt(null, object, feature);
	}
	
	@Override
	public int hashCodeAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		return toArrayAt(instant, object, feature).hashCode();
	}

	@Override
	public InternalEObject getContainer(InternalEObject object) {
		return getContainerAt(now(), object); 
	}


	@Override
	public InternalEObject getContainerAt(Instant instant, InternalEObject object) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		ContainerInfo lower = containersMap.get(containersMap.ceilingKey(containerKey(tObject.tId(), instant)));
		if (lower != null) {
			return (InternalEObject) getEObject(lower.containerId);
		}
		return null;
	}


	@Override
	public EStructuralFeature getContainingFeature(InternalEObject object) {
		return getContainingFeatureAt(now(), object);
	}

	@Override
	public EStructuralFeature getContainingFeatureAt(Instant instant, InternalEObject object) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		ContainerInfo lower = containersMap.get(containersMap.ceilingKey(containerKey(tObject.tId(), instant)));
		if (lower != null) {
			EObject container = getEObject(lower.containerId);
			container.eClass().getEStructuralFeature(lower.containingFeatureName);
		}
		return null;
	}

	@Override
	public EObject create(EClass eClass) {
		// This should not be called
		throw new UnsupportedOperationException();
	}


	@Override
	public EObject getEObject(String id) {
		if (id == null) {
			return null;
		}
		InternalTObject tObject = loadedEObjects.getUnchecked(id);
		if (tObject == null) {
			EClass eClass = resolveInstanceOf(id);
			if (eClass != null) {
				EObject eObject = EcoreUtil.create(eClass);
				if (eObject instanceof InternalTObject) {
					tObject = (InternalTObject) eObject;
				} else {
					tObject = TObjectAdapterFactoryImpl.getAdapter(eObject, InternalTObject.class);
				}
				tObject.tSetId(id.toString());
			} else {
				Logger.log(Logger.SEVERITY_ERROR, 
						MessageFormat.format("Element {0} does not have an associated EClass", id));
			}
			loadedEObjects.put(id, tObject);
		}
		if (tObject.tResource() != getResource()) {
			tObject.tSetResource(getResource());
		}
		return tObject;
	}
	

	protected EClass resolveInstanceOf(String id) {
		EClassInfo eClassInfo = instanceOfMap.get(id);
		if (eClassInfo != null) {
			EClass eClass = (EClass) Registry.INSTANCE.getEPackage(eClassInfo.nsURI).getEClassifier(eClassInfo.className);
			return eClass;
		}
		return null;
	}
	
	protected void updateContainment(TObject object, EReference eReference, TObject referencedObject) {
		if (eReference.isContainment()) {
			ContainerInfo lower = containersMap.get(containersMap.ceilingKey(containerKey(referencedObject.tId())));
			if (lower == null || !StringUtils.equals(lower.containerId, object.tId())) {
				containersMap.put(containerKey(referencedObject.tId()), new ContainerInfo(object.tId(), eReference.getName()));
			}
		}
	}
	
	protected void updateInstanceOf(TObject object) {
		EClassInfo info = instanceOfMap.get(object.tId());
		if (info == null) {
			instanceOfMap.put(object.tId(), new EClassInfo(object.eClass().getEPackage().getNsURI(), object.eClass().getName()));
		}
	}


	protected static Object parseMapValue(EAttribute eAttribute, Object property) {
		return property != null ? EcoreUtil.createFromString(eAttribute.getEAttributeType(), property.toString()) : null;
	}

	protected static String serializeToMapValue(EAttribute eAttribute, Object value) {
		return value != null ? EcoreUtil.convertToString(eAttribute.getEAttributeType(), value) : null;
	}
	
	protected Object getFromDataMap(TObject object, EStructuralFeature feature) {
		return getFromDataMap(Instant.MAX, object, feature);
	}

	/**
	 * Gets the latest value for {@link EStructuralFeature} {@code feature} from the
	 * data map for the {@link TObject} {@code object} before <code>endInstant</code>. 
	 * 
	 * @param endInstant
	 *            the end instant or <code>null</null> to indicate the latest possible time.
	 * @param object
	 * @param feature
	 * @return The value of the {@code feature}. It can be a {@link String} for
	 *         single-valued {@link EStructuralFeature}s or a {@link String}[] for
	 *         many-valued {@link EStructuralFeature}s
	 */
	protected Object getFromDataMap(Instant endInstant, TObject object, EStructuralFeature feature) {
		Object lower = dataMap.get(dataMap.ceilingKey(dataKey(object.tId(), feature.getName(), endInstant)));
		return lower;
	}
	
	protected TreeMap<Object[], Object> getAllFromDataMap(Instant startInstant, Instant endInstant, TObject object, EStructuralFeature feature) {
		TreeMap<Object[], Object> result = new TreeMap<>();
		Object[] firstKey = dataMap.ceilingKey(dataKey(object.tId(), feature.getName(), startInstant));
		Object[] lastKey = dataMap.floorKey(dataKey(object.tId(), feature.getName(), startInstant));
		Cursor<Object[], Object> c = dataMap.cursor(firstKey);
		while (!Arrays.equals(c.getKey(), lastKey)) {
			result.put(c.getKey(), c.getValue());
			c.next();
		};
		return result; 
	}

	private static Instant lastInstant = Instant.MIN;
	
	private static Instant now() {
		// Dirty hack:
		// Instant's precision is in the order of nanoseconds, however, 
		// it's accuracy is usually lower (even in the order of a few 
		// hundreds of nanoseconds). That means that multiple instants 
		// created in a row may be virtually the same instant. To avoid 
		// duplicate keys in the map (and thus missing values), we increment 
		// the instant in 1 nanosecond if the value is previous or the same
		// than the last call to this method.
		// Hopefully, the number of subsequent calls to this method will 
		// be low enough to avoid a big error accumulation 
		synchronized (lastInstant) {
			Instant readInstant = Instant.now();
			while (readInstant.isBefore(lastInstant) || readInstant.equals(lastInstant)) {
				readInstant = readInstant.plusNanos(1);
			}
			lastInstant = readInstant;
			return lastInstant;
		}
	}
	
	private static Object[] containerKey(String id) {
		return containerKey(id, now());
	}

	private static Object[] containerKey(String id, Instant instant) {
		return new Object[] { id, instant };
	}
	
	private static Object[] dataKey(String id, String featureName) {
		return dataKey(id, featureName, now());
	}
	
	private static Object[] dataKey(String id, String featureName, Instant instant) {
		if (instant == null) {
			instant = now();
		}
		return new Object[] { id, featureName, instant };
	}
	
	@SuppressWarnings("unused")
	private static String dataKeyId(Object[] key) {
		return (String) key[0];
	}
	
	@SuppressWarnings("unused")
	private static String dataKeyFeatureName(Object[] key) {
		return (String) key[1];
	}
	
	private static Instant dataKeyInstant(Object[] key) {
		return (Instant) key[2];
	}
	
	private static class ContainerInfo implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		public String containerId;
		
		public String containingFeatureName;
		
		public ContainerInfo(String containerId, String containingFeatureName) {
			this.containerId = containerId;
			this.containingFeatureName = containingFeatureName;
		}
		
	}
	
	private static class EClassInfo implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		public String nsURI;
		
		public String className;
		
		public EClassInfo(String nsURI, String className) {
			this.nsURI = nsURI;
			this.className = className;
		}
	}
}
