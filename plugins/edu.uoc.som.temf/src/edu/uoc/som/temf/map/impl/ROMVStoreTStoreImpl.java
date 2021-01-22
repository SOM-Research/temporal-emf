package edu.uoc.som.temf.map.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.h2.mvstore.MVStore;

import edu.uoc.som.temf.core.TResource;

public class ROMVStoreTStoreImpl extends MVStoreTStoreImpl {

	public ROMVStoreTStoreImpl(MVStore mvStore, TResource resource) {
		super(mvStore, resource);
	}

	@Override
	public Object set(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		throw new UnsupportedOperationException("TObjects in past TResources cannot be modified");
	}

	@Override
	public void add(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		throw new UnsupportedOperationException("TObjects in past TResources cannot be modified");
	}

	@Override
	public Object remove(InternalEObject object, EStructuralFeature feature, int index) {
		throw new UnsupportedOperationException("TObjects in past TResources cannot be modified");
	}

	@Override
	public Object move(InternalEObject object, EStructuralFeature feature, int targetIndex, int sourceIndex) {
		throw new UnsupportedOperationException("TObjects in past TResources cannot be modified");
	}

	@Override
	public void unset(InternalEObject object, EStructuralFeature feature) {
		throw new UnsupportedOperationException("TObjects in past TResources cannot be modified");
	}

	@Override
	public void clear(InternalEObject object, EStructuralFeature feature) {
		throw new UnsupportedOperationException("TObjects in past TResources cannot be modified");
	}

	@Override
	public EObject create(EClass eClass) {
		throw new UnsupportedOperationException("TObjects in past TResources cannot be modified");
	}

}
