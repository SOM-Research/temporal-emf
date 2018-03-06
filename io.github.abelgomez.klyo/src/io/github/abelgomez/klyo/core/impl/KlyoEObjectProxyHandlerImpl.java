/*******************************************************************************
 * Copyright (c) 2018 Abel Gómez.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Abel Gómez - initial API and implementation
 ******************************************************************************/
package io.github.abelgomez.klyo.core.impl;

import java.lang.reflect.Method;

import org.eclipse.emf.ecore.InternalEObject;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class KlyoEObjectProxyHandlerImpl implements MethodInterceptor {

	protected InternalEObject internalEObject;
	
	public KlyoEObjectProxyHandlerImpl(InternalEObject internalEObject) {
		super();
		this.internalEObject = internalEObject;
	}

	
	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		throw new UnsupportedOperationException();
	}
}