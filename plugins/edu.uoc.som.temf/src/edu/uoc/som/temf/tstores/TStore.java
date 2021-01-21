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

import java.time.Instant;
import java.util.SortedMap;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.InternalEObject.EStore;

/**
 * This interface extends the {@link EStore} interface and allows to query an {@link EStore}
 * at a given moment in time 
 * @author agomez
 * 
 */
public interface TStore extends InternalEObject.EStore {

	final Instant EARLIEST_INSTANT = Instant.MIN;

	final Instant OLDEST_INSTANT = Instant.MAX;
	
	/**
	 * Returns the value at the index in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given instant.
	 * 
	 * @param instant
	 *            the instant
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a feature of the object.
	 * @param index
	 *            an index within the content or {@link #NO_INDEX}.
	 * @return the value at the index in the content of the object's feature.
	 */
	Object getAt(Instant instant, InternalEObject object, EStructuralFeature feature, int index);

	/**
	 * Returns all the values at the index in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature between the given instants.
	 * 
	 * @param startInstant
	 *            the starting instant
	 * @param endInstant
	 *            the end instant
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a feature of the object.
	 * @param index
	 *            an index within the content or {@link #NO_INDEX}.
	 * @return the value at the index in the content of the object's feature.
	 */
	SortedMap<Instant, Object> getAllBetween(Instant startInstant, Instant endInstant, InternalEObject object, EStructuralFeature feature, int index);
	
	/**
	 * Returns whether the object's feature is considered set at a given instant.
	 * 
	 * @param instant
	 *            the instant
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a feature of the object.
	 * @return <code>true</code> if the object's feature is considered set.
	 */
	boolean isSetAt(Instant instant, InternalEObject object, EStructuralFeature feature);

	/**
	 * Returns whether the {@link EObject#eGet(EStructuralFeature,boolean) content}
	 * of the object's feature is empty at a given instant.
	 * 
	 * @param instant
	 *            the instant
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @return <code>true</code> if the content of the object's feature is empty.
	 */
	boolean isEmptyAt(Instant instant, InternalEObject object, EStructuralFeature feature);

	/**
	 * Returns the number of values in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given instant.
	 * 
	 * @param instant
	 *            the instant
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @return the number of values in the content of the object's feature.
	 */
	int sizeAt(Instant instant, InternalEObject object, EStructuralFeature feature);

	/**
	 * Returns whether the {@link EObject#eGet(EStructuralFeature,boolean) content}
	 * of the object's feature contains the given value at a given instant.
	 * 
	 * @param instant
	 *            the instant
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @param value
	 *            the value in question.
	 * @return <code>true</code> if the content of the object's feature contains the
	 *         given value.
	 */
	boolean containsAt(Instant instant, InternalEObject object, EStructuralFeature feature, Object value);

	/**
	 * Returns the first index of the given value in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given instant.
	 * 
	 * @param instant
	 *            the instant
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @param value
	 *            the value in question.
	 * @return the first index of the given value in the content of the object's
	 *         feature.
	 */
	int indexOfAt(Instant instant, InternalEObject object, EStructuralFeature feature, Object value);

	/**
	 * Returns the last index of the given value in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given instant.
	 * 
	 * @param instant
	 *            the instant
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @param value
	 *            the value in question.
	 * @return the last index of the given value in the content of the object's
	 *         feature.
	 */
	int lastIndexOfAt(Instant instant, InternalEObject object, EStructuralFeature feature, Object value);

	/**
	 * Returns a new array of the values in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given instant.
	 * 
	 * @param instant
	 *            the instant
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @return a new array of the values in the content of the object's feature.
	 */
	Object[] toArrayAt(Instant instant, InternalEObject object, EStructuralFeature feature);

	/**
	 * Returns an array of the values in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given instant. The given array will be used, unless it's too
	 * small, in which case a new array of the same type is allocated instead.
	 * 
	 * @param instant
	 *            the instant
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @param array
	 *            the array to fill.
	 * @return an array of the values in the content of the object's feature.
	 */
	<T> T[] toArrayAt(Instant instant, InternalEObject object, EStructuralFeature feature, T[] array);

	/**
	 * Returns a new array of the values in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given moment.
	 * 
	 * @param startInstant
	 *            the starting instant
	 * @param endInstant
	 *            the end instant
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @return a new array of the values in the content of the object's feature.
	 */
	SortedMap<Instant, Object[]> toArrayAllBetween(Instant startInstant, Instant endInstant, InternalEObject object, EStructuralFeature feature);
	
	/**
	 * Returns the hash code of the {@link EObject#eGet(EStructuralFeature,boolean)
	 * content} of the object's feature at a given instant.
	 * 
	 * @param instant
	 *            the instant
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @return the hash code of the content of the object's feature.
	 */
	int hashCodeAt(Instant instant, InternalEObject object, EStructuralFeature feature);

	/**
	 * Returns the object's {@link EObject#eContainer container} at a given instant.
	 * 
	 * @param instant
	 *            the instant
	 * @return the object's container.
	 * @see EObject#eContainer
	 */
	InternalEObject getContainerAt(Instant instant, InternalEObject object);

	/**
	 * Returns the object's {@link EObject#eContainingFeature containing feature} at
	 * a given instant.
	 * 
	 * @param instant
	 *            the instant
	 * @return the object's containing feature.
	 * @see EObject#eContainingFeature
	 */
	EStructuralFeature getContainingFeatureAt(Instant instant, InternalEObject object);

}
