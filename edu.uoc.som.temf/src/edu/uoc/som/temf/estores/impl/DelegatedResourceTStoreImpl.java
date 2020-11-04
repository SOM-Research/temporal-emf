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
package edu.uoc.som.temf.estores.impl;

import java.time.Instant;
import java.util.SortedMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;

import edu.uoc.som.temf.estores.SearcheableResourceEStore;
import edu.uoc.som.temf.estores.SearcheableResourceTStore;


/**
 * A {@link SearcheableResourceEStore} wrapper that delegates method calls to an
 * internal {@link SearcheableResourceEStore}
 * 
 * @author agomez
 * 
 */
public class DelegatedResourceTStoreImpl implements SearcheableResourceTStore {

	/**
	 * The wrapped {@link SearcheableResourceEStore}
	 */
	protected SearcheableResourceTStore eStore;

	public DelegatedResourceTStoreImpl(SearcheableResourceTStore eStore) {
		this.eStore = eStore;
	}

	public Resource getResource() {
		return eStore.getResource();
	}

	public EObject getEObject(String id) {
		return eStore.getEObject(id);
	}

	public Object getAt(Instant instant, InternalEObject object, EStructuralFeature feature, int index) {
		return eStore.getAt(instant, object, feature, index);
	}

	public SortedMap<Instant, Object> getAllBetween(Instant startInstant, Instant endInstant, InternalEObject object,
			EStructuralFeature feature, int index) {
		return eStore.getAllBetween(startInstant, endInstant, object, feature, index);
	}

	public boolean isEmptyAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		return eStore.isEmptyAt(instant, object, feature);
	}

	public int sizeAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		return eStore.sizeAt(instant, object, feature);
	}

	public boolean containsAt(Instant instant, InternalEObject object, EStructuralFeature feature, Object value) {
		return eStore.containsAt(instant, object, feature, value);
	}

	public int indexOfAt(Instant instant, InternalEObject object, EStructuralFeature feature, Object value) {
		return eStore.indexOfAt(instant, object, feature, value);
	}

	public int lastIndexOfAt(Instant instant, InternalEObject object, EStructuralFeature feature, Object value) {
		return eStore.lastIndexOfAt(instant, object, feature, value);
	}

	public Object[] toArrayAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		return eStore.toArrayAt(instant, object, feature);
	}

	public <T> T[] toArrayAt(Instant instant, InternalEObject object, EStructuralFeature feature, T[] array) {
		return eStore.toArrayAt(instant, object, feature, array);
	}

	public int hashCodeAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		return eStore.hashCodeAt(instant, object, feature);
	}

	public InternalEObject getContainerAt(Instant instant, InternalEObject object) {
		return eStore.getContainerAt(instant, object);
	}

	public EStructuralFeature getContainingFeatureAt(Instant instant, InternalEObject object) {
		return eStore.getContainingFeatureAt(instant, object);
	}

	public Object get(InternalEObject object, EStructuralFeature feature, int index) {
		return eStore.get(object, feature, index);
	}

	public Object set(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		return eStore.set(object, feature, index, value);
	}

	public boolean isSet(InternalEObject object, EStructuralFeature feature) {
		return eStore.isSet(object, feature);
	}

	@Override
	public boolean isSetAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		return eStore.isSetAt(instant, object, feature);
	}

	public void unset(InternalEObject object, EStructuralFeature feature) {
		eStore.unset(object, feature);
	}

	public boolean isEmpty(InternalEObject object, EStructuralFeature feature) {
		return eStore.isEmpty(object, feature);
	}

	public int size(InternalEObject object, EStructuralFeature feature) {
		return eStore.size(object, feature);
	}

	public boolean contains(InternalEObject object, EStructuralFeature feature, Object value) {
		return eStore.contains(object, feature, value);
	}

	public int indexOf(InternalEObject object, EStructuralFeature feature, Object value) {
		return eStore.indexOf(object, feature, value);
	}

	public int lastIndexOf(InternalEObject object, EStructuralFeature feature, Object value) {
		return eStore.lastIndexOf(object, feature, value);
	}

	public void add(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		eStore.add(object, feature, index, value);
	}

	public Object remove(InternalEObject object, EStructuralFeature feature, int index) {
		return eStore.remove(object, feature, index);
	}

	public Object move(InternalEObject object, EStructuralFeature feature, int targetIndex, int sourceIndex) {
		return eStore.move(object, feature, targetIndex, sourceIndex);
	}

	public void clear(InternalEObject object, EStructuralFeature feature) {
		eStore.clear(object, feature);
	}

	public Object[] toArray(InternalEObject object, EStructuralFeature feature) {
		return eStore.toArray(object, feature);
	}

	public <T> T[] toArray(InternalEObject object, EStructuralFeature feature, T[] array) {
		return eStore.toArray(object, feature, array);
	}
	
	@Override
	public SortedMap<Instant, Object[]> toArrayAllBetween(Instant startInstant, Instant endInstant, InternalEObject object, EStructuralFeature feature) {
		return eStore.toArrayAllBetween(startInstant, endInstant, object, feature);
	}


	public int hashCode(InternalEObject object, EStructuralFeature feature) {
		return eStore.hashCode(object, feature);
	}

	public InternalEObject getContainer(InternalEObject object) {
		return eStore.getContainer(object);
	}

	public EStructuralFeature getContainingFeature(InternalEObject object) {
		return eStore.getContainingFeature(object);
	}

	public EObject create(EClass eClass) {
		return eStore.create(eClass);
	}


}
