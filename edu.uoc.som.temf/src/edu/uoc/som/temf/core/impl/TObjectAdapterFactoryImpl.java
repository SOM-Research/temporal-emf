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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang.ClassUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;

import edu.uoc.som.temf.core.InternalTObject;
import edu.uoc.som.temf.core.TObject;
import net.sf.cglib.proxy.Enhancer;

public class TObjectAdapterFactoryImpl {

	/**
	 * {@link WeakHashMap} that stores the EObjects that have been already
	 * adapted to avoid duplication of {@link TObject}s. We use a
	 * {@link WeakHashMap} since the adaptor is no longer needed when the
	 * original {@link EObject} has been garbage collected
	 */
	protected static Map<InternalEObject, InternalTObject> adaptedObjects = new WeakHashMap<>();	
	
	@SuppressWarnings("unchecked")
	public static <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType.isInstance(adaptableObject)) {
			return (T) adaptableObject;
		} else if (adapterType.isAssignableFrom(InternalTObject.class) 
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
				// These are the current interfaces + TObject
				List<Class<?>> interfaces = new ArrayList<>();
				interfaces.addAll(ClassUtils.getAllInterfaces(adaptableObject.getClass()));
				interfaces.add(InternalTObject.class);
				// Create the proxy
				Enhancer enhancer = new Enhancer();
				enhancer.setClassLoader(TObjectAdapterFactoryImpl.class.getClassLoader());
				enhancer.setSuperclass(adaptableObject.getClass());
				enhancer.setInterfaces(interfaces.toArray(new Class[] {}));
				enhancer.setCallback(new TObjectProxyHandlerImpl((InternalEObject) adaptableObject));
				T adapter = (T) enhancer.create();
				adaptedObjects.put((InternalEObject) adaptableObject, (InternalTObject)  adapter);
				return adapter;
			}
		}
		return null;
	}

}
