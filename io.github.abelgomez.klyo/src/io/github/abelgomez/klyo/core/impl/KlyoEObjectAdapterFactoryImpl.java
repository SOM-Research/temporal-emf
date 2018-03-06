/*******************************************************************************
 * Copyright (c) 2018 Abel G�mez.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Abel G�mez - initial API and implementation
 *******************************************************************************/
package io.github.abelgomez.klyo.core.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang.ClassUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;

import io.github.abelgomez.klyo.core.KlyoEObject;
import io.github.abelgomez.klyo.core.KlyoInternalEObject;
import net.sf.cglib.proxy.Enhancer;

public class KlyoEObjectAdapterFactoryImpl {

	/**
	 * {@link WeakHashMap} that stores the EObjects that have been already
	 * adapted to avoid duplication of {@link KlyoEObject}s. We use a
	 * {@link WeakHashMap} since the adaptor is no longer needed when the
	 * original {@link EObject} has been garbage collected
	 */
	protected static Map<InternalEObject, KlyoInternalEObject> adaptedObjects = new WeakHashMap<>();	
	
	@SuppressWarnings("unchecked")
	public static <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType.isInstance(adaptableObject)) {
			return (T) adaptableObject;
		} else if (adapterType.isAssignableFrom(KlyoInternalEObject.class) 
				&& adaptableObject instanceof InternalEObject) {
			{
				EObject adapter = adaptedObjects.get(adaptableObject);
				if (adapter != null) {
					if (adapterType.isAssignableFrom(adapter.getClass())) {
						return (T) adapter;
					}
				}
			}
			{
				// Compute the interfaces that the proxy has to implement
				// These are the current interfaces + KlyoEObject
				List<Class<?>> interfaces = new ArrayList<>();
				interfaces.addAll(ClassUtils.getAllInterfaces(adaptableObject.getClass()));
				interfaces.add(KlyoInternalEObject.class);
				// Create the proxy
				Enhancer enhancer = new Enhancer();
				enhancer.setClassLoader(KlyoEObjectAdapterFactoryImpl.class.getClassLoader());
				enhancer.setSuperclass(adaptableObject.getClass());
				enhancer.setInterfaces(interfaces.toArray(new Class[] {}));
				enhancer.setCallback(new KlyoEObjectProxyHandlerImpl((InternalEObject) adaptableObject));
				T adapter = (T) enhancer.create();
				adaptedObjects.put((InternalEObject) adaptableObject, (KlyoInternalEObject)  adapter);
				return adapter;
			}
		}
		return null;
	}

}
