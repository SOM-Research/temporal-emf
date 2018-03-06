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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import io.github.abelgomez.klyo.estores.TimedEStore;

public interface KlyoResource extends Resource, Resource.Internal {

	public abstract TimedEStore eStore();
	
	public abstract EList<EObject> getContents(Date date);
	
	public abstract TreeIterator<EObject> getAllContents(Date date);
	
}
