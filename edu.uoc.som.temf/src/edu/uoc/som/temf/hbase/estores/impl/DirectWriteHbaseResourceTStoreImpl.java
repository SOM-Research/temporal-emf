/*******************************************************************************
 * Copyright (c) 2018 SOM Research Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Abel Gómez - initial API and implementation
 ******************************************************************************/
package edu.uoc.som.temf.hbase.estores.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeepDeletedCells;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.jboss.util.collection.SoftValueHashMap;

import edu.uoc.som.temf.Logger;
import edu.uoc.som.temf.core.InternalTObject;
import edu.uoc.som.temf.core.TObject;
import edu.uoc.som.temf.core.impl.TObjectAdapterFactoryImpl;
import edu.uoc.som.temf.estores.SearcheableResourceTStore;

public class DirectWriteHbaseResourceTStoreImpl implements SearcheableResourceTStore {

	protected static final byte[] PROPERTY_FAMILY = Bytes.toBytes("p");
	protected static final byte[] TYPE_FAMILY = Bytes.toBytes("t");
	protected static final byte[] METAMODEL_QUALIFIER = Bytes.toBytes("m");
	protected static final byte[] ECLASS_QUALIFIER = Bytes.toBytes("e");
	protected static final byte[] CONTAINMENT_FAMILY = Bytes.toBytes("c");
	protected static final byte[] CONTAINER_QUALIFIER = Bytes.toBytes("n");
	protected static final byte[] CONTAINING_FEATURE_QUALIFIER = Bytes.toBytes("g");

	// // TODO: Change in final version by short version to save space
//	protected static final byte[] PROPERTY_FAMILY = Bytes.toBytes("property");
//	protected static final byte[] TYPE_FAMILY = Bytes.toBytes("type");
//	protected static final byte[] METAMODEL_QUALIFIER = Bytes.toBytes("metamodel");
//	protected static final byte[] ECLASS_QUALIFIER = Bytes.toBytes("eclass");
//	protected static final byte[] CONTAINMENT_FAMILY = Bytes.toBytes("containment");
//	protected static final byte[] CONTAINER_QUALIFIER = Bytes.toBytes("container");
//	protected static final byte[] CONTAINING_FEATURE_QUALIFIER = Bytes.toBytes("containingFeature");

	@SuppressWarnings("unchecked")
	protected Map<Object, InternalTObject> loadedEObjects = new SoftValueHashMap();

	protected Connection connection;

	protected Table table;

	protected Resource.Internal resource;

	public DirectWriteHbaseResourceTStoreImpl(Resource.Internal resource, Connection connection) throws IOException {
		this.connection = connection;
		this.resource = resource;

		TableName tableName = TableName.valueOf(resource.getURI().path().replaceFirst("/", "").replaceAll("/", "_"));

		if (!connection.getAdmin().tableExists(tableName)) {
			HTableDescriptor desc = new HTableDescriptor(tableName);
			HColumnDescriptor typeFamily = new HColumnDescriptor(TYPE_FAMILY);
			HColumnDescriptor containmentFamily = new HColumnDescriptor(CONTAINMENT_FAMILY);
			containmentFamily.setMaxVersions(Integer.MAX_VALUE);
			containmentFamily.setMinVersions(Integer.MAX_VALUE);
			containmentFamily.setKeepDeletedCells(KeepDeletedCells.TRUE);
			HColumnDescriptor propertyFamily = new HColumnDescriptor(PROPERTY_FAMILY);
			propertyFamily.setMaxVersions(Integer.MAX_VALUE);
			propertyFamily.setMinVersions(Integer.MAX_VALUE);
			propertyFamily.setKeepDeletedCells(KeepDeletedCells.TRUE);
			desc.addFamily(typeFamily);
			desc.addFamily(containmentFamily);
			desc.addFamily(propertyFamily);
			connection.getAdmin().createTable(desc);
		}

		table = connection.getTable(tableName);
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
	public Object getAt(Date date, InternalEObject object, EStructuralFeature feature, int index) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		if (feature instanceof EAttribute) {
			return getAt(date, tObject, (EAttribute) feature, index);
		} else if (feature instanceof EReference) {
			return getAt(date, tObject, (EReference) feature, index);
		} else {
			throw new IllegalArgumentException(feature.toString());
		}
	}

	protected Object getAt(Date date, TObject object, EAttribute eAttribute, int index) {
		Object value = getFromTable(null, date, object, eAttribute);
		if (!eAttribute.isMany()) {
			return parseValue(eAttribute, (String) value);
		} else {
			String[] array = (String[]) value;
			return parseValue(eAttribute, array[index]);
		}
	}

	protected Object getAt(Date date, TObject object, EReference eReference, int index) {
		Object value = getFromTable(null, date, object, eReference);
		if (!eReference.isMany()) {
			return getEObject((String) value);
		} else {
			String[] array = (String[]) value;
			return getEObject(array[index]);
		}
	}

	@Override
	public SortedMap<Date, Object> getAllBetween(Date startDate, Date endDate, InternalEObject object, EStructuralFeature feature, int index) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		if (feature instanceof EAttribute) {
			return getAllBetween(startDate, endDate, tObject, (EAttribute) feature, index);
		} else if (feature instanceof EReference) {
			return getAllBetween(startDate, endDate, tObject, (EReference) feature, index);
		} else {
			throw new IllegalArgumentException(feature.toString());
		}
	}
	
	protected SortedMap<Date, Object> getAllBetween(Date startDate, Date endDate, TObject object, EAttribute eAttribute, int index) {
		SortedMap<Date, Object> result = new TreeMap<>();
		SortedMap<Long, Object> all = getAllFromTable(startDate, endDate, object, eAttribute);
		for (Entry<Long, Object> entry : all.entrySet()) {
			if (!eAttribute.isMany()) {
				result.put(new Date(entry.getKey()), parseValue(eAttribute, (String) entry.getValue()));
			} else {
				result.put(new Date(entry.getKey()), parseValue(eAttribute, ((String[]) entry.getValue())[index]));
			}
		}
		return result;
	}
	
	protected SortedMap<Date, Object> getAllBetween(Date startDate, Date endDate, TObject object, EReference eReference, int index) {
		SortedMap<Date, Object> result = new TreeMap<>();
		SortedMap<Long, Object> all = getAllFromTable(startDate, endDate, object, eReference);
		for (Entry<Long, Object> entry : all.entrySet()) {
			if (!eReference.isMany()) {
				result.put(new Date(entry.getKey()), getEObject((String) entry.getValue()));
			} else {
				result.put(new Date(entry.getKey()), getEObject(((String[]) entry.getValue())[index]));
			}
		}
		return result;
	}

	@Override
	public Object set(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		if (feature instanceof EAttribute) {
			return set(tObject, (EAttribute) feature, index, value);
		} else if (feature instanceof EReference) {
			InternalTObject referencedEObject = TObjectAdapterFactoryImpl.getAdapter(value,
					InternalTObject.class);
			return set(tObject, (EReference) feature, index, referencedEObject);
		} else {
			throw new IllegalArgumentException(feature.toString());
		}
	}

	protected Object set(TObject object, EAttribute eAttribute, int index, Object value) {
		Put put = new Put(Bytes.toBytes(object.tId()));
		Object oldValue = isSet((InternalEObject) object, eAttribute) ? getAt(null, object, eAttribute, index) : null;
		try {
			if (!eAttribute.isMany()) {
				put.addColumn(PROPERTY_FAMILY, Bytes.toBytes(eAttribute.getName()),
						Bytes.toBytes(serializeValue(eAttribute, value)));
			} else {
				String[] array = (String[]) getFromTable(object, eAttribute);
				array[index] = serializeValue(eAttribute, value);
				put.addColumn(PROPERTY_FAMILY, Bytes.toBytes(eAttribute.getName()), toBytes(array));
			}
			table.put(put);
		} catch (IOException e) {
			Logger.log(Logger.SEVERITY_ERROR,
					MessageFormat.format("Unable to set information for element ''{0}''", object));
		}
		return oldValue;
	}

	protected Object set(TObject object, EReference eReference, int index, InternalTObject referencedObject) {
		Put put = new Put(Bytes.toBytes(object.tId()));
		Object oldValue = isSet((InternalEObject) object, eReference) ? getAt(null, object, eReference, index) : null;

		if (referencedObject != null) {
			updateLoadedEObjects(referencedObject);
			updateContainment(object, eReference, referencedObject);
			updateInstanceOf(referencedObject);
		}

		try {
			if (!eReference.isMany()) {
				if (referencedObject != null) {
					put.addColumn(PROPERTY_FAMILY, Bytes.toBytes(eReference.getName()),
							Bytes.toBytes(referencedObject.tId()));
					table.put(put);
				} else {
					unset((InternalEObject) object, eReference);
				}
			} else {
				String[] array = (String[]) getFromTable(object, eReference);
				array[index] = referencedObject.tId();
				put.addColumn(PROPERTY_FAMILY, Bytes.toBytes(eReference.getName()), toBytes(array));
				table.put(put);
			}
		} catch (IOException e) {
			Logger.log(Logger.SEVERITY_ERROR,
					MessageFormat.format("Unable to set information for element ''{0}''", object));
		}
		return oldValue;
	}

	@Override
	public boolean isSet(InternalEObject object, EStructuralFeature feature) {
		return isSetAt(null, object, feature);
	}

	@Override
	public boolean isSetAt(Date date, InternalEObject object, EStructuralFeature feature) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		try {
			Get get = new Get(Bytes.toBytes(tObject.tId()));
			if (date != null) {
				get.setTimeRange(0, date.getTime() + 1);
			}
			Result result = table.get(get);
			byte[] value = result.getValue(PROPERTY_FAMILY, Bytes.toBytes(feature.getName()));
			return value != null;
		} catch (IOException e) {
			Logger.log(Logger.SEVERITY_ERROR,
					MessageFormat.format("Unable to get information for element ''{0}''", tObject));
		}
		return false;
	}

	@Override
	public void add(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		if (feature instanceof EAttribute) {
			add(tObject, (EAttribute) feature, index, value);
		} else if (feature instanceof EReference) {
			InternalTObject referencedEObject = TObjectAdapterFactoryImpl.getAdapter(value,
					InternalTObject.class);
			add(tObject, (EReference) feature, index, referencedEObject);
		} else {
			throw new IllegalArgumentException(feature.toString());
		}
	}

	protected void add(TObject object, EAttribute eAttribute, int index, Object value) {
		try {
			Put put = new Put(Bytes.toBytes(object.tId()));
			String[] array = (String[]) getFromTable(object, eAttribute);
			if (array == null) {
				array = new String[] {};
			}
			array = (String[]) ArrayUtils.add(array, index, serializeValue(eAttribute, value));
			put.addColumn(PROPERTY_FAMILY, Bytes.toBytes(eAttribute.getName()), toBytes(array));
			table.put(put);
		} catch (IOException e) {
			Logger.log(Logger.SEVERITY_ERROR, MessageFormat.format(
					"Unable to add ''{0}'' to ''{1}'' for element ''{2}''", value, eAttribute.getName(), object));
		}
	}

	protected void add(TObject object, EReference eReference, int index, InternalTObject referencedObject) {
		try {
			Put put = new Put(Bytes.toBytes(object.tId()));
			updateLoadedEObjects(referencedObject);
			updateContainment(object, eReference, referencedObject);
			updateInstanceOf(referencedObject);
			String[] array = (String[]) getFromTable(object, eReference);
			if (array == null) {
				array = new String[] {};
			}
			array = (String[]) ArrayUtils.add(array, index, referencedObject.tId());
			put.addColumn(PROPERTY_FAMILY, Bytes.toBytes(eReference.getName()), toBytes(array));
			table.put(put);
		} catch (IOException e) {
			Logger.log(Logger.SEVERITY_ERROR,
					MessageFormat.format("Unable to add ''{0}'' to ''{1}'' for element ''{2}''", referencedObject,
							eReference.getName(), object));
		}

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
		Put put = new Put(Bytes.toBytes(object.tId()));
		Object oldValue = getAt(null, object, eAttribute, index);
		try {
			String[] array = (String[]) getFromTable(object, eAttribute);
			array = (String[]) ArrayUtils.remove(array, index);
			put.addColumn(PROPERTY_FAMILY, Bytes.toBytes(eAttribute.getName()), toBytes(array));
			table.put(put);
		} catch (IOException e) {
			Logger.log(Logger.SEVERITY_ERROR, MessageFormat.format("Unable to delete ''{0}[{1}''] for element ''{2}''",
					eAttribute.getName(), index, object));
		}
		return oldValue;
	}

	protected Object remove(TObject object, EReference eReference, int index) {
		Put put = new Put(Bytes.toBytes(object.tId()));
		Object oldValue = getAt(null, object, eReference, index);
		try {
			String[] array = (String[]) getFromTable(object, eReference);
			array = (String[]) ArrayUtils.remove(array, index);
			put.addColumn(PROPERTY_FAMILY, Bytes.toBytes(eReference.getName()), toBytes(array));
			table.put(put);
		} catch (IOException e) {
			Logger.log(Logger.SEVERITY_ERROR, MessageFormat.format("Unable to delete ''{0}[{1}''] for element ''{2}''",
					eReference.getName(), index, object));
		}
		return oldValue;
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
		Put put = new Put(Bytes.toBytes(tObject.tId()));
		try {
//			Delete delete = new Delete(Bytes.toBytes(tObject.tId()));
//			delete.addColumn(PROPERTY_FAMILY, Bytes.toBytes(feature.getName()));
//			table.delete(delete);
			put.addColumn(PROPERTY_FAMILY, Bytes.toBytes(feature.getName()), null);
			table.put(put);
		} catch (IOException e) {
			Logger.log(Logger.SEVERITY_ERROR,
					MessageFormat.format("Unable to get containment information for {0}", tObject));
		}
	}

	@Override
	public boolean isEmpty(InternalEObject object, EStructuralFeature feature) {
		return isEmptyAt(null, object, feature);
	}
	
	@Override
	public boolean isEmptyAt(Date date, InternalEObject object, EStructuralFeature feature) {
		return sizeAt(date, object, feature) == 0;
	}

	@Override
	public int size(InternalEObject object, EStructuralFeature feature) {
		return sizeAt(null, object, feature);
	}
	
	@Override
	public int sizeAt(Date date, InternalEObject object, EStructuralFeature feature) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		String[] array = (String[]) getFromTable(null, date, tObject, feature);
		return array != null ? array.length : 0;
	}

	@Override
	public boolean contains(InternalEObject object, EStructuralFeature feature, Object value) {
		return containsAt(null, object, feature, value);
	}

	@Override
	public boolean containsAt(Date date, InternalEObject object, EStructuralFeature feature, Object value) {
		return indexOfAt(date, object, feature, value) != -1;
	}
	
	@Override
	public int indexOf(InternalEObject object, EStructuralFeature feature, Object value) {
		return indexOfAt(null, object, feature, value);
	}
	
	@Override
	public int indexOfAt(Date date, InternalEObject object, EStructuralFeature feature, Object value) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		String[] array = (String[]) getFromTable(null, date, tObject, feature);
		if (array == null) {
			return -1;
		}
		if (feature instanceof EAttribute) {
			return ArrayUtils.indexOf(array, serializeValue((EAttribute) feature, value));
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
	public int lastIndexOfAt(Date date, InternalEObject object, EStructuralFeature feature, Object value) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		String[] array = (String[]) getFromTable(null, date, tObject, feature);
		if (array == null) {
			return -1;
		}
		if (feature instanceof EAttribute) {
			return ArrayUtils.lastIndexOf(array, serializeValue((EAttribute) feature, value));
		} else {
			TObject childEObject = TObjectAdapterFactoryImpl.getAdapter(value, TObject.class);
			return ArrayUtils.lastIndexOf(array, childEObject.tId());
		}
	}

	@Override
	public void clear(InternalEObject object, EStructuralFeature feature) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		Put put = new Put(Bytes.toBytes(tObject.tId()));
		try {
			put.addColumn(PROPERTY_FAMILY, Bytes.toBytes(feature.getName()), toBytes(new String[] {}));
			table.put(put);
		} catch (IOException e) {
			Logger.log(Logger.SEVERITY_ERROR,
					MessageFormat.format("Unable to get containment information for {0}", tObject));
		}
	}

	@Override
	public Object[] toArray(InternalEObject object, EStructuralFeature feature) {
		return toArrayAt(null, object, feature);
	}
	
	@Override
	public Object[] toArrayAt(Date date, InternalEObject object, EStructuralFeature feature) {
		int size = sizeAt(date, object, feature);
		Object[] result = new Object[size];
		for (int index = 0; index < size; index++) {
			result[index] = getAt(date, object, feature, index);
		}
		return result;
	}

	@Override
	public <T> T[] toArray(InternalEObject object, EStructuralFeature feature, T[] array) {
		return toArrayAt(null, object, feature, array);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArrayAt(Date date, InternalEObject object, EStructuralFeature feature, T[] array) {
		int size = sizeAt(date, object, feature);
		T[] result = null;
		if (array.length < size) {
			result = Arrays.copyOf(array, size);
		} else {
			result = array;
		}
		for (int index = 0; index < size; index++) {
			result[index] = (T) getAt(date, object, feature, index);
		}
		return result;
	}
	
	@Override
	public SortedMap<Date, Object[]> toArrayAllBetween(Date startDate, Date endDate, InternalEObject object, EStructuralFeature feature) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);

		SortedMap<Date, Object[]> result = new TreeMap<>();
		SortedMap<Long, Object> all = getAllFromTable(startDate, endDate, tObject, feature);

		for (Entry<Long, Object> entry : all.entrySet()) {
			if (feature instanceof EAttribute) {
				result.put(new Date(entry.getKey()), Arrays.asList((String[]) entry.getValue()).stream().map(v -> parseValue((EAttribute) feature, (String) v)).toArray());
			} else if (feature instanceof EReference) {
				result.put(new Date(entry.getKey()), Arrays.asList((String[]) entry.getValue()).stream().map(v -> getEObject((String) v)).toArray());
			} else {
				throw new IllegalArgumentException(feature.toString());
			}
		}
		
		return result;
	}

	@Override
	public int hashCode(InternalEObject object, EStructuralFeature feature) {
		return hashCodeAt(null, object, feature);
	}
	
	@Override
	public int hashCodeAt(Date date, InternalEObject object, EStructuralFeature feature) {
		return toArrayAt(date, object, feature).hashCode();
	}

	@Override
	public InternalEObject getContainer(InternalEObject object) {
		return getContainerAt(null, object);
	}

	@Override
	public InternalEObject getContainerAt(Date date, InternalEObject object) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		
		try {
			Get get = new Get(Bytes.toBytes(tObject.tId()));
			if (date != null) {
				get.setTimeRange(0, date.getTime() + 1);
			}
			Result result = table.get(get);
			String containerId = Bytes.toString(result.getValue(CONTAINMENT_FAMILY, CONTAINER_QUALIFIER));
			String containingFeatureName = Bytes
					.toString(result.getValue(CONTAINMENT_FAMILY, CONTAINING_FEATURE_QUALIFIER));
			
			if (containerId != null && containingFeatureName != null) {
				return (InternalEObject) getEObject(containerId);
			}
			
		} catch (IOException e) {
			Logger.log(Logger.SEVERITY_ERROR,
					MessageFormat.format("Unable to get containment information for {0}", tObject));
		}
		return null;
	}

	@Override
	public EStructuralFeature getContainingFeature(InternalEObject object) {
		return getContainingFeatureAt(null, object);
	}

	@Override
	public EStructuralFeature getContainingFeatureAt(Date date, InternalEObject object) {
		TObject tObject = TObjectAdapterFactoryImpl.getAdapter(object, TObject.class);
		
		try {
			Get get = new Get(Bytes.toBytes(tObject.tId()));
			if (date != null) {
				get.setTimeRange(0, date.getTime() + 1);
			}
			Result result = table.get(get);
			String containerId = Bytes.toString(result.getValue(CONTAINMENT_FAMILY, CONTAINER_QUALIFIER));
			String containingFeatureName = Bytes
					.toString(result.getValue(CONTAINMENT_FAMILY, CONTAINING_FEATURE_QUALIFIER));
			
			if (containerId != null && containingFeatureName != null) {
				EObject container = getEObject(containerId);
				return container.eClass().getEStructuralFeature(containingFeatureName);
			}
			
		} catch (IOException e) {
			Logger.log(Logger.SEVERITY_ERROR,
					MessageFormat.format("Unable to get containment information for {0}", tObject));
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
		if (StringUtils.isEmpty(id)) {
			return null;
		}
		InternalTObject tObject = loadedEObjects.get(id);
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
		try {
			Result result = table.get(new Get(Bytes.toBytes(id)));
			String nsURI = Bytes.toString(result.getValue(TYPE_FAMILY, METAMODEL_QUALIFIER));
			String className = Bytes.toString(result.getValue(TYPE_FAMILY, ECLASS_QUALIFIER));
			if (nsURI != null && className != null) {
				EClass eClass = (EClass) Registry.INSTANCE.getEPackage(nsURI).getEClassifier(className);
				return eClass;
			}
		} catch (IOException e) {
			Logger.log(Logger.SEVERITY_ERROR,
					MessageFormat.format("Unable to get instance of information for {0}", id));
		}
		return null;
	}

	protected void updateLoadedEObjects(InternalTObject eObject) {
		loadedEObjects.put(eObject.tId(), eObject);
	}

	protected void updateContainment(TObject object, EReference eReference, TObject referencedObject) {
		if (eReference.isContainment()) {
			try {
				Put put = new Put(Bytes.toBytes(referencedObject.tId()));
				put.addColumn(CONTAINMENT_FAMILY, CONTAINER_QUALIFIER, Bytes.toBytes(object.tId()));
				put.addColumn(CONTAINMENT_FAMILY, CONTAINING_FEATURE_QUALIFIER, Bytes.toBytes(eReference.getName()));
				table.put(put);
			} catch (IOException e) {
				Logger.log(Logger.SEVERITY_ERROR,
						MessageFormat.format("Unable to update containment information for {0}", object));
			}
		}
	}

	protected void updateInstanceOf(TObject object) {
		try {
			Put put = new Put(Bytes.toBytes(object.tId()));
			put.addColumn(TYPE_FAMILY, METAMODEL_QUALIFIER, Bytes.toBytes(object.eClass().getEPackage().getNsURI()));
			put.addColumn(TYPE_FAMILY, ECLASS_QUALIFIER, Bytes.toBytes(object.eClass().getName()));
			table.checkAndPut(Bytes.toBytes(object.tId()), TYPE_FAMILY, ECLASS_QUALIFIER, null, put);
		} catch (IOException e) {
			Logger.log(Logger.SEVERITY_ERROR,
					MessageFormat.format("Unable to update containment information for {0}", object));
		}
	}

	protected static Object parseValue(EAttribute eAttribute, String value) {
		return value != null ? EcoreUtil.createFromString(eAttribute.getEAttributeType(), value) : null;
	}

	protected static String serializeValue(EAttribute eAttribute, Object value) {
		return value != null ? EcoreUtil.convertToString(eAttribute.getEAttributeType(), value) : null;
	}

	/**
	 * Gets the latest value for {@link EStructuralFeature} {@code feature} from the
	 * {@link Table} for the {@link TObject} {@code object}
	 * 
	 * @param object
	 * @param feature
	 * @return The value of the {@code feature}. It can be a {@link String} for
	 *         single-valued {@link EStructuralFeature}s or a {@link String}[] for
	 *         many-valued {@link EStructuralFeature}s
	 */
	protected Object getFromTable(TObject object, EStructuralFeature feature) {
		return getFromTable(null, null, object, feature);
	}
	
	/**
	 * Gets the latest value for {@link EStructuralFeature} {@code feature} from the
	 * {@link Table} for the {@link TObject} {@code object} between
	 * <code>startDate</code> and <code>endDate</code>. 
	 * 
	 * @param startDate
	 *            the start moment, or <code>null</code> to indicate epoch time
	 * @param endDate
	 *            the end moment or <code>null</null> to indicate the latest possible time.
	 * @param object
	 * @param feature
	 * @return The value of the {@code feature}. It can be a {@link String} for
	 *         single-valued {@link EStructuralFeature}s or a {@link String}[] for
	 *         many-valued {@link EStructuralFeature}s
	 */
	protected Object getFromTable(Date startDate, Date endDate, TObject object, EStructuralFeature feature) {
		long start = startDate != null ? startDate.getTime() : 0;
		long end = endDate != null && endDate.getTime() != Long.MAX_VALUE ? endDate.getTime() + 1 : Long.MAX_VALUE;
		try {
			Get get = new Get(Bytes.toBytes(object.tId()));
			get.setTimeRange(start, end);
			Result result = table.get(get);
			byte[] bytes = result.getValue(PROPERTY_FAMILY, Bytes.toBytes(feature.getName()));
			if (!feature.isMany()) {
				return bytes != null && bytes.length > 0 ? Bytes.toString(bytes) : null;
			} else {
				return toStrings(bytes);
			}
		} catch (IOException e) {
			Logger.log(Logger.SEVERITY_ERROR,
					MessageFormat.format("Unable to get property ''{0}'' for ''{1}''", feature.getName(), object));
		}
		return null;
	}
	

	/**
	 * Gets all the values for {@link EStructuralFeature} {@code feature} from the
	 * {@link Table} for the {@link TObject} {@code object} between
	 * <code>startDate</code> and <code>endDate</code>.
	 * 
	 * @param startDate
	 *            the start moment, or <code>null</code> to indicate epoch time
	 * @param endDate
	 *            the end moment or <code>null</null> to indicate the latest
	 *            possible time.
	 * @param object
	 * @param feature
	 * @return The values of the {@code feature}. It can be a {@link String}[] for
	 *         single-valued {@link EStructuralFeature}s or a {@link String}[][] for
	 *         many-valued {@link EStructuralFeature}s
	 */
	protected SortedMap<Long, Object> getAllFromTable(Date startDate, Date endDate, TObject object, EStructuralFeature feature) {
		long start = startDate != null ? startDate.getTime() : 0;
		long end = endDate != null && endDate.getTime() != Long.MAX_VALUE ? endDate.getTime() + 1 : Long.MAX_VALUE;
		SortedMap<Long, Object> resultMap = new TreeMap<>();
		try {
			// Get the curent value at 'start'
			{
				Get get = new Get(Bytes.toBytes(object.tId()));
				get.setTimeRange(0, start + 1);
				Result result = table.get(get);
				if (!result.isEmpty()) {
					if (!feature.isMany()) {
						resultMap.put(result.rawCells()[0].getTimestamp(), Bytes.toString(result.getValue(PROPERTY_FAMILY, Bytes.toBytes(feature.getName()))));
					} else {
						resultMap.put(result.rawCells()[0].getTimestamp(), toStrings(result.getValue(PROPERTY_FAMILY, Bytes.toBytes(feature.getName()))));
					}
				}
			}
			// Get the values between 'start' and 'end'
			{
				Get get = new Get(Bytes.toBytes(object.tId()));
				get.setTimeRange(start, end);
				get.setMaxVersions();
				Result result = table.get(get);
				List<Cell> columnCells = result.getColumnCells(PROPERTY_FAMILY, Bytes.toBytes(feature.getName()));
				for (Cell cell : columnCells) {
					if (!feature.isMany()) {
						resultMap.put(cell.getTimestamp(), Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
					} else {
						resultMap.put(cell.getTimestamp(), toStrings(cell.getValueArray()));
					}
				}
			}
		} catch (IOException e) {
			Logger.log(Logger.SEVERITY_ERROR, MessageFormat.format("Unable to get property ''{0}'' for ''{1}''", feature.getName(), object));
		}
		return resultMap;
	}
	
	protected static byte[] toBytes(String[] strings) {
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(strings);
			objectOutputStream.flush();
			objectOutputStream.close();
			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			Logger.log(Logger.SEVERITY_ERROR,
					MessageFormat.format("Unable to convert ''{0}'' to byte[]", strings.toString()));
		}
		return null;
	}

	protected static String[] toStrings(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		String[] result = null;
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		try (ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
			result = (String[]) objectInputStream.readObject();
		} catch (IOException e) {
			Logger.log(Logger.SEVERITY_ERROR,
					MessageFormat.format("Unable to convert ''{0}'' to String[]", bytes.toString()));
		} catch (ClassNotFoundException e) {
			Logger.log(Logger.SEVERITY_ERROR, e);
		}
		return result;

	}
}
