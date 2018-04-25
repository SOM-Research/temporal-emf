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

import java.util.Date;
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

	public final Date EARLIEST_DATE = new Date(0);

	public final Date OLDEST_DATE = new Date(Long.MAX_VALUE);
	
	/**
	 * Returns the value at the index in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given moment.
	 * 
	 * @param date
	 *            the moment date
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a feature of the object.
	 * @param index
	 *            an index within the content or {@link #NO_INDEX}.
	 * @return the value at the index in the content of the object's feature.
	 */
	Object getAt(Date date, InternalEObject object, EStructuralFeature feature, int index);

	/**
	 * Returns all the values at the index in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature between the given moment.
	 * 
	 * @param startDate
	 *            the starting date
	 * @param endDate
	 *            the end date
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a feature of the object.
	 * @param index
	 *            an index within the content or {@link #NO_INDEX}.
	 * @return the value at the index in the content of the object's feature.
	 */
	SortedMap<Date, Object> getAllBetween(Date startDate, Date endDate, InternalEObject object, EStructuralFeature feature, int index);
	
	/**
	 * Returns whether the object's feature is considered set at a given moment.
	 * 
	 * @param date
	 *            the moment date
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a feature of the object.
	 * @return <code>true</code> if the object's feature is considered set.
	 */
	boolean isSetAt(Date date, InternalEObject object, EStructuralFeature feature);

	/**
	 * Returns whether the {@link EObject#eGet(EStructuralFeature,boolean) content}
	 * of the object's feature is empty at a given moment.
	 * 
	 * @param date
	 *            the moment date
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @return <code>true</code> if the content of the object's feature is empty.
	 */
	boolean isEmptyAt(Date date, InternalEObject object, EStructuralFeature feature);

	/**
	 * Returns the number of values in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given moment.
	 * 
	 * @param date
	 *            the moment date
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @return the number of values in the content of the object's feature.
	 */
	int sizeAt(Date date, InternalEObject object, EStructuralFeature feature);

	/**
	 * Returns whether the {@link EObject#eGet(EStructuralFeature,boolean) content}
	 * of the object's feature contains the given value at a given moment.
	 * 
	 * @param date
	 *            the moment date
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
	boolean containsAt(Date date, InternalEObject object, EStructuralFeature feature, Object value);

	/**
	 * Returns the first index of the given value in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given moment.
	 * 
	 * @param date
	 *            the moment date
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
	int indexOfAt(Date date, InternalEObject object, EStructuralFeature feature, Object value);

	/**
	 * Returns the last index of the given value in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given moment.
	 * 
	 * @param date
	 *            the moment date
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
	int lastIndexOfAt(Date date, InternalEObject object, EStructuralFeature feature, Object value);

	/**
	 * Returns a new array of the values in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given moment.
	 * 
	 * @param date
	 *            the moment date
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @return a new array of the values in the content of the object's feature.
	 */
	Object[] toArrayAt(Date date, InternalEObject object, EStructuralFeature feature);

	/**
	 * Returns an array of the values in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given moment. The given array will be used, unless it's too
	 * small, in which case a new array of the same type is allocated instead.
	 * 
	 * @param date
	 *            the moment date
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @param array
	 *            the array to fill.
	 * @return an array of the values in the content of the object's feature.
	 */
	<T> T[] toArrayAt(Date date, InternalEObject object, EStructuralFeature feature, T[] array);

	/**
	 * Returns a new array of the values in the
	 * {@link EObject#eGet(EStructuralFeature,boolean) content} of the object's
	 * feature at a given moment.
	 * 
	 * @param startDate
	 *            the starting date
	 * @param endDate
	 *            the end date
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @return a new array of the values in the content of the object's feature.
	 */
	SortedMap<Date, Object[]> toArrayAllBetween(Date startDate, Date endDate, InternalEObject object, EStructuralFeature feature);
	
	/**
	 * Returns the hash code of the {@link EObject#eGet(EStructuralFeature,boolean)
	 * content} of the object's feature at a given moment.
	 * 
	 * @param date
	 *            the moment date
	 * @param object
	 *            the object in question.
	 * @param feature
	 *            a {@link ETypedElement#isMany() many-valued} feature of the
	 *            object.
	 * @return the hash code of the content of the object's feature.
	 */
	int hashCodeAt(Date date, InternalEObject object, EStructuralFeature feature);

	/**
	 * Returns the object's {@link EObject#eContainer container} at a given moment.
	 * 
	 * @param date
	 *            the moment date
	 * @return the object's container.
	 * @see EObject#eContainer
	 */
	InternalEObject getContainerAt(Date date, InternalEObject object);

	/**
	 * Returns the object's {@link EObject#eContainingFeature containing feature} at
	 * a given moment.
	 * 
	 * @param date
	 *            the moment date
	 * @return the object's containing feature.
	 * @see EObject#eContainingFeature
	 */
	EStructuralFeature getContainingFeatureAt(Date date, InternalEObject object);

}
