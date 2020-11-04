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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import edu.uoc.som.temf.estores.SearcheableResourceEStore;
import edu.uoc.som.temf.estores.SearcheableResourceTStore;

/**
 * A {@link SearcheableResourceEStore} wrapper that caches the size data
 * 
 * @author agomez
 * 
 */
public class SizeCachingDelegatedTStoreImpl extends DelegatedResourceTStoreImpl implements SearcheableResourceTStore {

	protected class MapKey {
		protected InternalEObject object;
		protected EStructuralFeature feature;
		
		public MapKey(InternalEObject object, EStructuralFeature feature) {
			this.object = object;
			this.feature = feature;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((object == null) ? 0 : object.hashCode());
			result = prime * result + ((feature == null) ? 0 : feature.hashCode());
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
			if (object == null) {
				if (other.object != null)
					return false;
			} else if (!object.equals(other.object))
				return false;
			if (feature == null) {
				if (other.feature != null)
					return false;
			} else if (!feature.equals(other.feature))
				return false;
			return true;
		}
		
		private SizeCachingDelegatedTStoreImpl getOuterType() {
			return SizeCachingDelegatedTStoreImpl.this;
		}
	}
	
	protected static final int DEFAULT_SIZE_CACHE_SIZE = 10000;
	
	protected LoadingCache<MapKey, Integer> sizeCache;
	
	
	public SizeCachingDelegatedTStoreImpl(SearcheableResourceTStore eStore) {
		this(eStore, DEFAULT_SIZE_CACHE_SIZE);
	}

	public SizeCachingDelegatedTStoreImpl(SearcheableResourceTStore eStore, int sizeCacheSize) {
		super(eStore);
		this.sizeCache = CacheBuilder.newBuilder().maximumSize(sizeCacheSize).build(new CacheLoader<MapKey, Integer>() {
			@Override
			public Integer load(MapKey key) throws Exception {
				return SizeCachingDelegatedTStoreImpl.super.size(key.object, key.feature);
			}
		});
	}
	
	@Override
	public void unset(InternalEObject object, EStructuralFeature feature) {
		super.unset(object, feature);
		sizeCache.put(new MapKey(object, feature), 0);
	}

	@Override
	public boolean isEmpty(InternalEObject object, EStructuralFeature feature) {
		Integer size = sizeCache.getUnchecked(new MapKey(object, feature));
		if (size != null) {
			return size == 0;
		} else {
			return super.isEmpty(object, feature);
		}
	}

	@Override
	public int size(InternalEObject object, EStructuralFeature feature) {
		return sizeCache.getUnchecked(new MapKey(object, feature));
	}

	@Override
	public void add(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		Integer size = sizeCache.getUnchecked(new MapKey(object, feature));
		if (size != null) {
			sizeCache.put(new MapKey(object, feature), size + 1); 
		} 
		super.add(object, feature, index, value);
	}

	@Override
	public Object remove(InternalEObject object, EStructuralFeature feature, int index) {
		Integer size = sizeCache.getUnchecked(new MapKey(object, feature));
		if (size != null) {
			sizeCache.put(new MapKey(object, feature), size - 1); 
		} 
		return super.remove(object, feature, index);
	}

	@Override
	public void clear(InternalEObject object, EStructuralFeature feature) {
		sizeCache.put(new MapKey(object, feature), 0); 
		super.clear(object, feature);
	}
}
