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
import java.util.SortedMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;

import edu.uoc.som.temf.tstores.SearcheableResourceTStore;


/**
 * A {@link SearcheableResourceTStore} wrapper that delegates method calls to an
 * internal {@link SearcheableResourceTStore}
 * 
 * @author agomez
 * 
 */
public class DelegatedResourceTStoreImpl implements SearcheableResourceTStore {

	/**
	 * The wrapped {@link SearcheableResourceTStore}
	 */
	protected SearcheableResourceTStore tStore;

	public DelegatedResourceTStoreImpl(SearcheableResourceTStore eStore) {
		this.tStore = eStore;
	}

	public Resource getResource() {
		return tStore.getResource();
	}

	public EObject getEObject(String id) {
		return tStore.getEObject(id);
	}
	
	public Object getAt(Instant instant, InternalEObject object, EStructuralFeature feature, int index) {
		return tStore.getAt(instant, object, feature, index);
	}

	public SortedMap<Instant, Object> getAllBetween(Instant startInstant, Instant endInstant, InternalEObject object,
			EStructuralFeature feature, int index) {
		return tStore.getAllBetween(startInstant, endInstant, object, feature, index);
	}

	public boolean isEmptyAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		return tStore.isEmptyAt(instant, object, feature);
	}

	public int sizeAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		return tStore.sizeAt(instant, object, feature);
	}

	public boolean containsAt(Instant instant, InternalEObject object, EStructuralFeature feature, Object value) {
		return tStore.containsAt(instant, object, feature, value);
	}

	public int indexOfAt(Instant instant, InternalEObject object, EStructuralFeature feature, Object value) {
		return tStore.indexOfAt(instant, object, feature, value);
	}

	public int lastIndexOfAt(Instant instant, InternalEObject object, EStructuralFeature feature, Object value) {
		return tStore.lastIndexOfAt(instant, object, feature, value);
	}

	public Object[] toArrayAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		return tStore.toArrayAt(instant, object, feature);
	}

	public <T> T[] toArrayAt(Instant instant, InternalEObject object, EStructuralFeature feature, T[] array) {
		return tStore.toArrayAt(instant, object, feature, array);
	}

	public int hashCodeAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		return tStore.hashCodeAt(instant, object, feature);
	}

	public InternalEObject getContainerAt(Instant instant, InternalEObject object) {
		return tStore.getContainerAt(instant, object);
	}

	public EStructuralFeature getContainingFeatureAt(Instant instant, InternalEObject object) {
		return tStore.getContainingFeatureAt(instant, object);
	}

	public Object get(InternalEObject object, EStructuralFeature feature, int index) {
		return tStore.get(object, feature, index);
	}

	public Object set(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		return tStore.set(object, feature, index, value);
	}

	public boolean isSet(InternalEObject object, EStructuralFeature feature) {
		return tStore.isSet(object, feature);
	}

	@Override
	public boolean isSetAt(Instant instant, InternalEObject object, EStructuralFeature feature) {
		return tStore.isSetAt(instant, object, feature);
	}

	public void unset(InternalEObject object, EStructuralFeature feature) {
		tStore.unset(object, feature);
	}

	public boolean isEmpty(InternalEObject object, EStructuralFeature feature) {
		return tStore.isEmpty(object, feature);
	}

	public int size(InternalEObject object, EStructuralFeature feature) {
		return tStore.size(object, feature);
	}

	public boolean contains(InternalEObject object, EStructuralFeature feature, Object value) {
		return tStore.contains(object, feature, value);
	}

	public int indexOf(InternalEObject object, EStructuralFeature feature, Object value) {
		return tStore.indexOf(object, feature, value);
	}

	public int lastIndexOf(InternalEObject object, EStructuralFeature feature, Object value) {
		return tStore.lastIndexOf(object, feature, value);
	}

	public void add(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		tStore.add(object, feature, index, value);
	}

	public Object remove(InternalEObject object, EStructuralFeature feature, int index) {
		return tStore.remove(object, feature, index);
	}

	public Object move(InternalEObject object, EStructuralFeature feature, int targetIndex, int sourceIndex) {
		return tStore.move(object, feature, targetIndex, sourceIndex);
	}

	public void clear(InternalEObject object, EStructuralFeature feature) {
		tStore.clear(object, feature);
	}

	public Object[] toArray(InternalEObject object, EStructuralFeature feature) {
		return tStore.toArray(object, feature);
	}

	public <T> T[] toArray(InternalEObject object, EStructuralFeature feature, T[] array) {
		return tStore.toArray(object, feature, array);
	}
	
	@Override
	public SortedMap<Instant, Object[]> toArrayAllBetween(Instant startInstant, Instant endInstant, InternalEObject object, EStructuralFeature feature) {
		return tStore.toArrayAllBetween(startInstant, endInstant, object, feature);
	}


	public int hashCode(InternalEObject object, EStructuralFeature feature) {
		return tStore.hashCode(object, feature);
	}

	public InternalEObject getContainer(InternalEObject object) {
		return tStore.getContainer(object);
	}

	public EStructuralFeature getContainingFeature(InternalEObject object) {
		return tStore.getContainingFeature(object);
	}

	public EObject create(EClass eClass) {
		return tStore.create(eClass);
	}


}
