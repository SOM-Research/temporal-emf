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

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.InternalEObject.EStore;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import edu.uoc.som.temf.estores.SearcheableResourceEStore;
import edu.uoc.som.temf.estores.SearcheableResourceTStore;

/**
 * A {@link SearcheableResourceEStore} wrapper that caches {@link EStructuralFeature}s
 * 
 * @author agomez
 * 
 */
public class EStructuralFeatureCachingDelegatedTStoreImpl extends DelegatedResourceTStoreImpl implements SearcheableResourceTStore {

	protected class MapKey {
		protected InternalEObject object;
		protected EStructuralFeature feature;
		protected int index;
		
		public MapKey(InternalEObject object, EStructuralFeature feature, int index) {
			this.object = object;
			this.feature = feature;
			this.index = index;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((feature == null) ? 0 : feature.hashCode());
			result = prime * result + index;
			result = prime * result + ((object == null) ? 0 : object.hashCode());
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
			MapKey other = (MapKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (feature == null) {
				if (other.feature != null)
					return false;
			} else if (!feature.equals(other.feature))
				return false;
			if (index != other.index)
				return false;
			if (object == null) {
				if (other.object != null)
					return false;
			} else if (!object.equals(other.object))
				return false;
			return true;
		}

		private EStructuralFeatureCachingDelegatedTStoreImpl getOuterType() {
			return EStructuralFeatureCachingDelegatedTStoreImpl.this;
		}
		
	}
	
	protected static final int DEFAULT_CACHE_SIZE = 10000;
	
	protected LoadingCache<MapKey, Object> cache;
	
	public EStructuralFeatureCachingDelegatedTStoreImpl(SearcheableResourceTStore eStore) {
		this(eStore, DEFAULT_CACHE_SIZE);
	}

	public EStructuralFeatureCachingDelegatedTStoreImpl(SearcheableResourceTStore eStore, int cacheSize) {
		super(eStore);
		this.cache = CacheBuilder.newBuilder().maximumSize(cacheSize).build(new CacheLoader<MapKey, Object> () {
			@Override
			public Object load(MapKey key) throws Exception {
				return EStructuralFeatureCachingDelegatedTStoreImpl.super.get(key.object, key.feature, key.index);
			}
		});
	}
	
	@Override
	public Object get(InternalEObject object, EStructuralFeature feature, int index) {
		return cache.getUnchecked(new MapKey(object, feature, index));
	}
	
	@Override
	public Object set(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		Object returnValue = super.set(object, feature, index, value);
		cache.put(new MapKey(object, feature, index), value);
		return returnValue;
	}
	
	@Override
	public void add(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		super.add(object, feature, index, value);
		cache.put(new MapKey(object, feature, index), value);
		int size = size(object, feature);
		for (int i = index + 1; i < size; i++) {
			cache.invalidate(new MapKey(object, feature, i));
		}
	}
	
	@Override
	public Object remove(InternalEObject object, EStructuralFeature feature, int index) {
		int size = size(object, feature);
		Object returnValue = super.remove(object, feature, index);
		for (int i = index; i < size; i++) {
			cache.invalidate(new MapKey(object, feature, i));
		}
		return returnValue;
	}
	
	@Override
	public void clear(InternalEObject object, EStructuralFeature feature) {
		int size = size(object, feature);
		super.clear(object, feature);
		for (int i = 0; i < size; i++) {
			cache.invalidate(new MapKey(object, feature, i));
		}
	}
	
	@Override
	public Object move(InternalEObject object, EStructuralFeature feature, int targetIndex, int sourceIndex) {
		Object returnValue = super.move(object, feature, targetIndex, sourceIndex);
		int size = size(object, feature);
		for (int i = Math.min(sourceIndex, targetIndex); i < size; i++) {
			cache.invalidate(new MapKey(object, feature, i));
		}
		cache.put(new MapKey(object, feature, targetIndex), returnValue);
		return returnValue;
	}
	
	@Override
	public void unset(InternalEObject object, EStructuralFeature feature) {
		if (!feature.isMany()) {
			cache.invalidate(new MapKey(object, feature, EStore.NO_INDEX));
			super.unset(object, feature);
		} else {
			int size = size(object, feature);
			for (int i = 0; i < size; i++) {
				cache.invalidate(new MapKey(object, feature, i));
			}
			super.unset(object, feature);
		}
	}
}
