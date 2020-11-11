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
package edu.uoc.som.temf.core.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
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

import edu.uoc.som.temf.core.InternalTObject;
import edu.uoc.som.temf.core.TObject;
import edu.uoc.som.temf.core.TResource;
import edu.uoc.som.temf.estores.TStore;
import edu.uoc.som.temf.estores.impl.OwnedTransientTStoreImpl;

public class TObjectImpl extends MinimalEStoreEObjectImpl implements InternalTObject {

	protected static final int UNSETTED_FEATURE_ID = -1;

	protected String id;

	protected Resource.Internal tResource;

	/**
	 * The internal cached value of the eContainer. This information should be also
	 * maintained in the underlying {@link EStore}
	 */
	protected InternalEObject eContainer;

	protected int eContainerFeatureID = UNSETTED_FEATURE_ID;

	protected TStore eStore;

	public TObjectImpl() {
		this.id = EcoreUtil.generateUUID();
	}

	@Override
	public String tId() {
		return id;
	}

	@Override
	public void tSetId(String id) {
		this.id = id;
	}

	@Override
	public String eURIFragmentSegment(EStructuralFeature eStructuralFeature, EObject eObject) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public EObject eObjectForURIFragmentSegment(String uriFragmentSegment) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public InternalEObject eInternalContainer() {
		return eContainer;
	}

	@Override
	public EObject eContainer() {
		if (tResource instanceof TResource) {
			InternalEObject container = eStore().getContainer(this);
			eBasicSetContainer(container);
			eBasicSetContainerFeatureID(eContainerFeatureID());
			return container;
		} else {
			return super.eContainer();
		}
	}
	
	@Override
	public EObject eContainerAt(Instant instant) {
		return eStore().getContainerAt(instant, this);
	}

	@Override
	protected void eBasicSetContainer(InternalEObject newContainer) {
		eContainer = newContainer;
		if (newContainer != null && newContainer.eResource() != tResource) {
			tSetResource((Resource.Internal) eContainer.eResource());
		}
	}

	@Override
	public int eContainerFeatureID() {
		if (eContainerFeatureID == UNSETTED_FEATURE_ID) {
			if (eDirectResource() instanceof TResource) {
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
	public EStructuralFeature eContainingFeatureAt(Instant instant) {
		return eStore().getContainingFeatureAt(instant, this);
	}
	
	@Override
	public Resource eResource() {
		if (tResource != null) {
			return tResource;
		} else {
			return super.eResource();
		}
	}

	@Override
	public Internal tResource() {
		return tResource;
	}

	@Override
	public void tSetResource(Internal resource) {
		this.tResource = resource;
		EStore oldStore = eStore;
		// Set the new EStore
		if (resource instanceof TResource) {
			eStore = ((TResource) resource).tStore();
		} else {
			eStore = new OwnedTransientTStoreImpl(this);
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
	public TStore eStore() {
		if (eStore == null) {
			eStore = new OwnedTransientTStoreImpl(this);
		}
		return eStore;
	}

	@Override
	protected boolean eIsCaching() {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public EList<EObject> eContentsAt(Instant instant) {
		EStructuralFeature[] eStructuralFeatures = ((EClassImpl.FeatureSubsetSupplier) this.eClass()
				.getEAllStructuralFeatures()).containments();

		EList<EObject> contents = ECollections.newBasicEList();
		if (eStructuralFeatures != null) {
			for (EStructuralFeature feature : eStructuralFeatures) {
				contents.addAll((Collection<? extends EObject>) eGetAt(instant, feature));
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
	public Object eGetAt(Instant instant, EStructuralFeature feature) {
		return dynamicGetAt(instant, eDynamicFeatureID(feature));
	}
	
	@Override
	public boolean eIsSetAt(Instant instant, EStructuralFeature feature) {
		return dynamicIsSetAt(instant, eDynamicFeatureID(feature));
	}
	
	@Override
	public SortedMap<Instant, Object> eGetAllBetween(Instant start, Instant end, EStructuralFeature feature) {
		return dynamicGetAllBetween(start, end, eDynamicFeatureID(feature));
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

	public Object dynamicGetAt(Instant instant, int dynamicFeatureID) {
		EStructuralFeature feature = eDynamicFeature(dynamicFeatureID);
		if (feature.isMany()) {
			return ECollections.unmodifiableEList(ECollections.asEList(eStore().toArrayAt(instant, this, feature)));
		} else {
			return eStore().getAt(instant, this, feature, EStore.NO_INDEX);
		}
	}
	
	public boolean dynamicIsSetAt(Instant instant, int dynamicFeatureID) {
		EStructuralFeature feature = eDynamicFeature(dynamicFeatureID);
		return eStore().isSetAt(instant, this, feature);
	}
	
	public SortedMap<Instant, Object> dynamicGetAllBetween(Instant start, Instant end, int dynamicFeatureID) {
		EStructuralFeature feature = eDynamicFeature(dynamicFeatureID);
		SortedMap<Instant, Object> result = new TreeMap<>();
		if (feature.isMany()) {
			SortedMap<Instant, Object[]> all = eStore().toArrayAllBetween(start, end, this, feature);
			for (Entry<Instant, Object[]> entry : all.entrySet()) {
				result.put(entry.getKey(), ECollections.unmodifiableEList(ECollections.asEList(entry.getValue())));
			}
		} else {
			SortedMap<Instant, Object> all = eStore().getAllBetween(start, end, this, feature, EStore.NO_INDEX);
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
			TObject other = (TObject) obj;
			if (id == null) {
				if (other.tId() != null) {
					return false;
				}
			} else if (!id.equals(other.tId())) {
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
