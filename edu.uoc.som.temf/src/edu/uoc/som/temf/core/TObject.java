/*******************************************************************************
 * Copyright (c) 2018 SOM Research Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Abel G�mez - initial API and implementation
 *******************************************************************************/
package edu.uoc.som.temf.core;

import java.util.Date;
import java.util.SortedMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public interface TObject extends EObject {

	public abstract String tId();
	
	EList<EObject> eContents(Date date);
	
	Object eGetAt(Date date, EStructuralFeature feature);

	SortedMap<Date, Object> eGetAllBetween(Date startDate, Date endDate, EStructuralFeature feature);

}
