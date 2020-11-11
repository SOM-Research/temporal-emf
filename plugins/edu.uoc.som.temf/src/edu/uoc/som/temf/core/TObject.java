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
package edu.uoc.som.temf.core;

import java.time.Instant;
import java.util.SortedMap;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public interface TObject extends EObject {

	String tId();

	EList<EObject> eContentsAt(Instant instant);

	EObject eContainerAt(Instant instant);
	
	EStructuralFeature eContainingFeatureAt(Instant instant);
	
	Object eGetAt(Instant instant, EStructuralFeature feature);

	boolean eIsSetAt(Instant instant, EStructuralFeature feature);
	
	SortedMap<Instant, Object> eGetAllBetween(Instant start, Instant end, EStructuralFeature feature);

}
