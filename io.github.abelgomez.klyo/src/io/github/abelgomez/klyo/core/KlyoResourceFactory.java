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
package io.github.abelgomez.klyo.core;

import org.eclipse.emf.ecore.resource.Resource;

import io.github.abelgomez.klyo.core.impl.KlyoResourceFactoryImpl;

public interface KlyoResourceFactory extends Resource.Factory {

	public static KlyoResourceFactory eINSTANCE = new KlyoResourceFactoryImpl();
	
}
