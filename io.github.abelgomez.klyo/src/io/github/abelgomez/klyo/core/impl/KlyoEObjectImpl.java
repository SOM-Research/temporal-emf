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
package io.github.abelgomez.klyo.core.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EStoreEObjectImpl;
import org.eclipse.emf.ecore.impl.MinimalEStoreEObjectImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Internal;
import org.eclipse.emf.ecore.util.EcoreUtil;

import io.github.abelgomez.klyo.core.KlyoEObject;
import io.github.abelgomez.klyo.core.KlyoInternalEObject;
import io.github.abelgomez.klyo.core.KlyoResource;
import io.github.abelgomez.klyo.estores.TimedEStore;
import io.github.abelgomez.klyo.estores.impl.OwnedTransientEStoreImpl;

public class KlyoEObjectImpl extends MinimalEStoreEObjectImpl implements KlyoInternalEObject {

	protected static final int UNSETTED_FEATURE_ID = -1;

	protected String id;

	protected Resource.Internal klyoResource;

	/**
	 * The internal cached value of the eContainer. This information should be also
	 * maintained in the underlying {@link EStore}
	 */
	protected InternalEObject eContainer;

	protected int eContainerFeatureID = UNSETTED_FEATURE_ID;

	protected TimedEStore eStore;

	public KlyoEObjectImpl() {
		this.id = EcoreUtil.generateUUID();
	}

	@Override
	public String klyoId() {
		return id;
	}

	@Override
	public void klyoSetId(String id) {
		this.id = id;
	}

	@Override
	public InternalEObject eInternalContainer() {
		return eContainer;
	}

	@Override
	public EObject eContainer() {
		if (klyoResource instanceof KlyoResource) {
			InternalEObject container = eStore().getContainer(this);
			eBasicSetContainer(container);
			eBasicSetContainerFeatureID(eContainerFeatureID());
			return container;
		} else {
			return super.eContainer();
		}
	}

	@Override
	protected void eBasicSetContainer(InternalEObject newContainer) {
		eContainer = newContainer;
		if (newContainer != null && newContainer.eResource() != klyoResource) {
			klyoSetResource((Resource.Internal) eContainer.eResource());
		}
	}

	@Override
	public int eContainerFeatureID() {
		if (eContainerFeatureID == UNSETTED_FEATURE_ID) {
			if (eDirectResource() instanceof KlyoResource) {
				EReference containingFeature = (EReference) eStore().getContainingFeature(this);
				if (containingFeature != null) {
					EReference oppositeFeature = containingFeature.getEOpposite();
					if (oppositeFeature != null) {
						eBasicSetContainerFeatureID(eClass().getFeatureID(oppositeFeature));
					} else {
						eBasicSetContainerFeatureID(InternalEObject.EOPPOSITE_FEATURE_BASE
								- eInternalContainer().eClass().getFeatureID(containingFeature));
					}
				}
			}
		}
		return eContainerFeatureID;
	}

	@Override
	protected void eBasicSetContainerFeatureID(int newContainerFeatureID) {
		eContainerFeatureID = newContainerFeatureID;
	}

	@Override
	public Resource eResource() {
		if (klyoResource != null) {
			return klyoResource;
		} else {
			return super.eResource();
		}
	}

	@Override
	public Internal klyoResource() {
		return klyoResource;
	}

	@Override
	public void klyoSetResource(Internal resource) {
		this.klyoResource = resource;
		EStore oldStore = eStore;
		// Set the new EStore
		if (resource instanceof KlyoResource) {
			eStore = ((KlyoResource) resource).eStore();
		} else {
			eStore = new OwnedTransientEStoreImpl(this);
		}
		// Move contents from oldStore to eStore
		if (oldStore != null && eStore != null && eStore != oldStore) {
			// If the new store is different, initialize the new store
			// with the data stored in the old store
			for (EStructuralFeature feature : eClass().getEAllStructuralFeatures()) {
				if (oldStore.isSet(this, feature)) {
					if (!feature.isMany()) {
						eStore.set(this, feature, EStore.NO_INDEX, oldStore.get(this, feature, EStore.NO_INDEX));
					} else {
						eStore.clear(this, feature);
						int size = oldStore.size(this, feature);
						for (int i = 0; i < size; i++) {
							eStore.add(this, feature, i, oldStore.get(this, feature, i));
						}
					}
				}
			}
		}
	}

	@Override
	public TimedEStore eStore() {
		if (eStore == null) {
			eStore = new OwnedTransientEStoreImpl(this);
		}
		return eStore;
	}

	@Override
	protected boolean eIsCaching() {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public EList<EObject> eContents(Date date) {
		EStructuralFeature[] eStructuralFeatures = ((EClassImpl.FeatureSubsetSupplier) this.eClass()
				.getEAllStructuralFeatures()).containments();

		EList<EObject> contents = ECollections.newBasicEList();
		if (eStructuralFeatures != null) {
			for (EStructuralFeature feature : eStructuralFeatures) {
				contents.addAll((Collection<? extends EObject>) eGetAt(date, feature));
			}
		}
		return ECollections.unmodifiableEList(contents);
	}

	@Override
	public void dynamicSet(int dynamicFeatureID, Object value) {
		EStructuralFeature feature = eDynamicFeature(dynamicFeatureID);
		if (feature.isMany()) {
			eStore().unset(this, feature);
			@SuppressWarnings("rawtypes")
			EList collection = (EList) value;
			for (int index = 0; index < collection.size(); index++) {
				eStore().set(this, feature, index, value);
			}
		} else {
			eStore().set(this, feature, InternalEObject.EStore.NO_INDEX, value);
		}
	}

	@Override
	public Object eGetAt(Date date, EStructuralFeature feature) {
		return dynamicGetAt(date, eDynamicFeatureID(feature));
	}
	
	
	@Override
	public SortedMap<Date, Object> eGetAllBetween(Date startDate, Date endDate, EStructuralFeature feature) {
		return dynamicGetAllBetween(startDate, endDate, eDynamicFeatureID(feature));
	}
	
	@Override
	public Object dynamicGet(int dynamicFeatureID) {
		EStructuralFeature feature = eDynamicFeature(dynamicFeatureID);
		if (feature.isMany()) {
			return new EStoreEObjectImpl.BasicEStoreEList<Object>(this, feature);
		} else {
			return eStore().get(this, feature, EStore.NO_INDEX);
		}
	}

	public Object dynamicGetAt(Date date, int dynamicFeatureID) {
		EStructuralFeature feature = eDynamicFeature(dynamicFeatureID);
		if (feature.isMany()) {
			return ECollections.unmodifiableEList(ECollections.asEList(eStore().toArrayAt(date, this, feature)));
		} else {
			return eStore().getAt(date, this, feature, EStore.NO_INDEX);
		}
	}
	
	public SortedMap<Date, Object> dynamicGetAllBetween(Date startDate, Date endDate, int dynamicFeatureID) {
		EStructuralFeature feature = eDynamicFeature(dynamicFeatureID);
		SortedMap<Date, Object> result = new TreeMap<>();
		if (feature.isMany()) {
			SortedMap<Date, Object[]> all = eStore().toArrayAllBetween(startDate, endDate, this, feature);
			for (Entry<Date, Object[]> entry : all.entrySet()) {
				result.put(entry.getKey(), ECollections.unmodifiableEList(ECollections.asEList(entry.getValue())));
			}
		} else {
			SortedMap<Date, Object> all = eStore().getAllBetween(startDate, endDate, this, feature, EStore.NO_INDEX);
			result.putAll(all);
		}
		return Collections.unmodifiableSortedMap(result);
	}

	@Override
	public void dynamicUnset(int dynamicFeatureID) {
		EStructuralFeature feature = eDynamicFeature(dynamicFeatureID);
		eStore().unset(this, feature);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		} else {
			KlyoEObject other = (KlyoEObject) obj;
			if (id == null) {
				if (other.klyoId() != null) {
					return false;
				}
			} else if (!id.equals(other.klyoId())) {
				return false;
			}
			return true;
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(getClass().getName());
		result.append('@');
		result.append(Integer.toHexString(hashCode()));

		if (eIsProxy()) {
			result.append(" (eProxyURI: ");
			result.append(eProxyURI());
			if (eDynamicClass() != null) {
				result.append(" eClass: ");
				result.append(eDynamicClass());
			}
			result.append(')');
		} else if (eDynamicClass() != null) {
			result.append(" (eClass: ");
			result.append(eDynamicClass());
			result.append(')');
		} else if (eStaticClass() != null) {
			result.append(" (eClass: ");
			result.append(eStaticClass());
			result.append(')');
		}
		return result.toString();
	}
}
