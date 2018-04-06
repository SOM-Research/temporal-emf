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

import java.util.Date;
import java.util.SortedMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public interface KlyoEObject extends EObject {

	public abstract String klyoId();
	
	EList<EObject> eContents(Date date);
	
	Object eGetAt(Date date, EStructuralFeature feature);

	SortedMap<Date, Object> eGetAllBetween(Date startDate, Date endDate, EStructuralFeature feature);

}
