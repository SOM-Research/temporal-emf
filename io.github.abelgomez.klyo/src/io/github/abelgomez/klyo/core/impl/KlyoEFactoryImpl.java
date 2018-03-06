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
package io.github.abelgomez.klyo.core.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.EFactoryImpl;

import io.github.abelgomez.klyo.core.KlyoEFactory;
import io.github.abelgomez.klyo.core.KlyoInternalEObject;

public class KlyoEFactoryImpl extends EFactoryImpl implements KlyoEFactory {

	@Override
	public KlyoInternalEObject create(EClass eClass) {
		KlyoEObjectImpl eObject = new KlyoEObjectImpl();
		eObject.eSetClass(eClass);
		return eObject;
	}
}
