/*******************************************************************************
 * Copyright (c) 2020 SOM Research Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Abel Gómez - initial API and implementation
 *******************************************************************************/
package edu.uoc.som.temf.core;

import java.time.Clock;
import java.time.Instant;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import edu.uoc.som.temf.estores.TStore;

/**
 * {@link TResource} extends the EMF {@link Resource} API by providing some
 * time-aware methods
 * 
 * @author agomez
 *
 */
public interface TResource extends Resource, Resource.Internal {

	/**
	 * Returns the Temporal Store {@link TStore} used by this {@link TResource}
	 * 
	 * @return the {@link TStore}
	 */
	TStore tStore();

	/**
	 * Returns the direct contents of this {@link TStore} at the given
	 * {@link Instant}
	 * 
	 * @param instant the {@link Instant}
	 * @return the {@link TResource} contents at {@link Instant}
	 */
	EList<EObject> getContentsAt(Instant instant);

	/**
	 * Returns all direct and indirect contents of this {@link TStore} at the given
	 * {@link Instant}
	 * 
	 * @param instant the {@link Instant}
	 * @return an iterator for all the contents of the {@link TResource} at
	 *         {@link Instant}
	 */
	TreeIterator<EObject> getAllContentsAt(Instant instant);

	/**
	 * Returns the internal clock used by the {@link TResource}. Client code must
	 * use this {@link Clock} in order to query the actual {@link Instant} where the
	 * contents of the {@link TResource} are. This is because, althought the
	 * precission of {@link Instant} is in the order of nanoseconds, its accuracy is
	 * typically in the order of microseconds. As such, multiple operations may
	 * happen virtually at the same time if using the standard system {@link Clock}.
	 * The {@link TResource} clock avoids returning always the same Instant in
	 * subsequent calls to the {@link Clock#instant()} method.
	 * 
	 * @return the {@link TResource} {@link Clock}
	 */
	Clock getClock();
}
