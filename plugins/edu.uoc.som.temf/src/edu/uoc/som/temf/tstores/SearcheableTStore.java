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
package edu.uoc.som.temf.tstores;

import org.eclipse.emf.ecore.EObject;

/**
 * This interface extends the {@link TStore} interface and allows searching for
 * an {@link EObject} in the underlying storage
 * 
 * @author agomez
 * 
 */
public interface SearcheableTStore extends TStore {

	/**
	 * Returns the resolved {@link EObject} identified by the given <code>id</code>
	 * or <code>null</code> if no {@link EObject} can be resolved.
	 * 
	 * @param id
	 * @return
	 */
	EObject getEObject(String id);

}
