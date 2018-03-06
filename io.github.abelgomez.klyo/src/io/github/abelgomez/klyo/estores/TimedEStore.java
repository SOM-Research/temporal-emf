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
package io.github.abelgomez.klyo.estores;

import java.util.Date;

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
public interface TimedEStore extends InternalEObject.EStore {

	/**
	 * Returns the value at the index in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given moment.
	 * 
	 * @param date
	 *            the moment
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a feature of the object.
	 * @param index
	 *            an index within the content or {@link #NO_INDEX}.
	 * @return the value at the index in the content of the object's feature.
	 */
	Object get(Date date, InternalEObject object, EStructuralFeature feature, int index);

	/**
	 * Returns whether the object's feature is considered set at a given moment.
	 * 
	 * @param date
	 *            the moment
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a feature of the object.
	 * @return <code>true</code> if the object's feature is considered set.
	 */
	boolean isSet(Date date, InternalEObject object, EStructuralFeature feature);

	/**
	 * Returns whether the {@link EObject#eGet(EStructuralFeature,boolean) content}
	 * of the object's feature is empty at a given moment.
	 * 
	 * @param date
	 *            the moment
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @return <code>true</code> if the content of the object's feature is empty.
	 */
	boolean isEmpty(Date date, InternalEObject object, EStructuralFeature feature);

	/**
	 * Returns the number of values in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given moment.
	 * 
	 * @param date
	 *            the moment
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @return the number of values in the content of the object's feature.
	 */
	int size(Date date, InternalEObject object, EStructuralFeature feature);

	/**
	 * Returns whether the {@link EObject#eGet(EStructuralFeature,boolean) content}
	 * of the object's feature contains the given value at a given moment.
	 * 
	 * @param date
	 *            the moment
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
	boolean contains(Date date, InternalEObject object, EStructuralFeature feature, Object value);

	/**
	 * Returns the first index of the given value in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given moment.
	 * 
	 * @param date
	 *            the moment
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
	int indexOf(Date date, InternalEObject object, EStructuralFeature feature, Object value);

	/**
	 * Returns the last index of the given value in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given moment.
	 * 
	 * @param date
	 *            the moment
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
	int lastIndexOf(Date date, InternalEObject object, EStructuralFeature feature, Object value);

	/**
	 * Returns a new array of the values in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given moment.
	 * 
	 * @param date
	 *            the moment
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @return a new array of the values in the content of the object's feature.
	 */
	Object[] toArray(Date date, InternalEObject object, EStructuralFeature feature);

	/**
	 * Returns an array of the values in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given moment. The given array will be used, unless it's too
	 * small, in which case a new array of the same type is allocated instead.
	 * 
	 * @param date
	 *            the moment
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @param array
	 *            the array to fill.
	 * @return an array of the values in the content of the object's feature.
	 */
	<T> T[] toArray(Date date, InternalEObject object, EStructuralFeature feature, T[] array);

	/**
	 * Returns the hash code of the {@link EObject#eGet(EStructuralFeature,boolean)
	 * content} of the object's feature at a given moment.
	 * 
	 * @param date
	 *            the moment
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @return the hash code of the content of the object's feature.
	 */
	int hashCode(Date date, InternalEObject object, EStructuralFeature feature);

	/**
	 * Returns the object's {@link EObject#eContainer container} at a given moment.
	 * 
	 * @param date
	 *            the moment
	 * @return the object's container.
	 * @see EObject#eContainer
	 */
	InternalEObject getContainer(Date date, InternalEObject object);

	/**
	 * Returns the object's {@link EObject#eContainingFeature containing feature} at
	 * a given moment.
	 * 
	 * @param date
	 *            the moment
	 * @return the object's containing feature.
	 * @see EObject#eContainingFeature
	 */
	EStructuralFeature getContainingFeature(Date date, InternalEObject object);

}
