package edu.uoc.som.temf.map.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.h2.mvstore.MVStore;

import edu.uoc.som.temf.core.TResource;

public class ROMVStoreResourceTStoreImpl extends MVStoreResourceTStoreImpl {

	public ROMVStoreResourceTStoreImpl(TResource resource, MVStore mvStore) {
		super(resource, mvStore);
	}

	@Override
	public Object set(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		throw new RuntimeException("TObjects in past TResources cannot be modified");
	}

	@Override
	public void add(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		throw new RuntimeException("TObjects in past TResources cannot be modified");
	}

	@Override
	public Object remove(InternalEObject object, EStructuralFeature feature, int index) {
		throw new RuntimeException("TObjects in past TResources cannot be modified");
	}

	@Override
	public Object move(InternalEObject object, EStructuralFeature feature, int targetIndex, int sourceIndex) {
		throw new RuntimeException("TObjects in past TResources cannot be modified");
	}

	@Override
	public void unset(InternalEObject object, EStructuralFeature feature) {
		throw new RuntimeException("TObjects in past TResources cannot be modified");
	}

	@Override
	public void clear(InternalEObject object, EStructuralFeature feature) {
		throw new RuntimeException("TObjects in past TResources cannot be modified");
	}

	@Override
	public EObject create(EClass eClass) {
		throw new RuntimeException("TObjects in past TResources cannot be modified");
	}

}
