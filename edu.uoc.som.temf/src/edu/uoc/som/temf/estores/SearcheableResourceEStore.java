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
package edu.uoc.som.temf.estores;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.InternalEObject.EStore;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * This interface extends the {@link EStore} interface and allows to establish a
 * mapping between {@link Resource}s and {@link EStore}s
 * 
 * @author agomez
 * 
 */
public interface SearcheableResourceEStore extends InternalEObject.EStore {

	/**
	 * Returns the {@link Resource} to which this {@link EStore} is associated
	 * 
	 * @return
	 */
	Resource getResource();

	/**
	 * Returns the resolved {@link EObject} identified by the given <code>id</code>
	 * or <code>null</code> if no {@link EObject} can be resolved.
	 * 
	 * @param id
	 * @return
	 */
	EObject getEObject(String id);

}
