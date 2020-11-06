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
public class IsSetCachingDelegatedTStoreImpl extends DelegatedResourceTStoreImpl implements SearcheableResourceTStore {

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
		
		private IsSetCachingDelegatedTStoreImpl getOuterType() {
			return IsSetCachingDelegatedTStoreImpl.this;
		}
	}
	
	protected static final int DEFAULT_IS_SET_CACHE_SIZE = 10000;
	
	protected LoadingCache<MapKey, Boolean> isSetCache;
	
	public IsSetCachingDelegatedTStoreImpl(SearcheableResourceTStore eStore) {
		this(eStore, DEFAULT_IS_SET_CACHE_SIZE);
	}

	public IsSetCachingDelegatedTStoreImpl(SearcheableResourceTStore eStore, int sizeCacheSize) {
		super(eStore);
		this.isSetCache = CacheBuilder.newBuilder().maximumSize(sizeCacheSize).build(new CacheLoader<MapKey, Boolean>() {
			@Override
			public Boolean load(MapKey key) throws Exception {
				return IsSetCachingDelegatedTStoreImpl.super.isSet(key.object, key.feature);
			}
		});
	}
	
	@Override
	public void unset(InternalEObject object, EStructuralFeature feature) {
		super.unset(object, feature);
		isSetCache.put(new MapKey(object, feature), false);
	}

	@Override
	public boolean isSet(InternalEObject object, EStructuralFeature feature) {
		return isSetCache.getUnchecked(new MapKey(object, feature));
	}

	@Override
	public void add(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		tStore.add(object, feature, index, value);
		isSetCache.put(new MapKey(object, feature), true); 
	}

	@Override
	public Object remove(InternalEObject object, EStructuralFeature feature, int index) {
		isSetCache.invalidate(new MapKey(object, feature)); // Remove, next queries will update the right cached value
		return super.remove(object, feature, index);
	}
	
	@Override
	public Object set(InternalEObject object, EStructuralFeature feature, int index, Object value) {
		Object returnValue = tStore.set(object, feature, index, value);
		isSetCache.put(new MapKey(object, feature), true); 
		return returnValue;
	}

	@Override
	public void clear(InternalEObject object, EStructuralFeature feature) {
		isSetCache.put(new MapKey(object, feature), false); 
		tStore.clear(object, feature);
	}
	
	@Override
	public Object move(InternalEObject object, EStructuralFeature feature, int targetIndex, int sourceIndex) {
		Object returnValue = super.move(object, feature, targetIndex, sourceIndex);
		isSetCache.put(new MapKey(object, feature), true);
		return returnValue;
	}
	
	@Override
	public boolean contains(InternalEObject object, EStructuralFeature feature, Object value) {
		boolean returnValue = super.contains(object, feature, value);
		if (returnValue == true) {
			isSetCache.put(new MapKey(object, feature), true);
		}
		return returnValue;
	}
	
	@Override
	public Object get(InternalEObject object, EStructuralFeature feature, int index) {
		Object returnValue = super.get(object, feature, index);
		if (returnValue != null) {
			isSetCache.put(new MapKey(object, feature), true);
		}
		return returnValue;
	}
}
