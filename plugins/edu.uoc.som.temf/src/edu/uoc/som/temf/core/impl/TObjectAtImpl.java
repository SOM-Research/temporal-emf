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
package edu.uoc.som.temf.core.impl;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.SortedMap;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;

import edu.uoc.som.temf.core.InternalTObject;
import edu.uoc.som.temf.core.TObject;

public class TObjectAtImpl implements TObject {

	protected Instant instant;

	protected InternalTObject tObject;

	public TObjectAtImpl(Instant instant, InternalTObject tObject) {
		this.tObject = tObject;
	}

	@Override
	public EList<Adapter> eAdapters() {
		return tObject.eAdapters();
	}

	@Override
	public String tId() {
		return tObject.tId();
	}

	@Override
	public EList<EObject> eContentsAt(Instant instant) {
		return tObject.eContentsAt(instant);
	}

	@Override
	public boolean eDeliver() {
		return tObject.eDeliver();
	}

	@Override
	public EObject eContainerAt(Instant instant) {
		return tObject.eContainerAt(instant);
	}

	@Override
	public EStructuralFeature eContainingFeatureAt(Instant instant) {
		return tObject.eContainingFeatureAt(instant);
	}

	public Object eGetAt(Instant instant, EStructuralFeature feature) {
		return tObject.eGetAt(instant, feature);
	}

	@Override
	public boolean eIsSetAt(Instant instant, EStructuralFeature feature) {
		return tObject.eIsSetAt(instant, feature);
	}

	@Override
	public void eSetDeliver(boolean deliver) {
		throw new UnsupportedOperationException("Past TObjects cannot be modified");
	}

	@Override
	public SortedMap<Instant, Object> eGetAllBetween(Instant start, Instant end, EStructuralFeature feature) {
		return tObject.eGetAllBetween(start, end, feature);
	}

	@Override
	public void eNotify(Notification notification) {
		tObject.eNotify(notification);
	}

	@Override
	public EClass eClass() {
		return tObject.eClass();
	}

	@Override
	public Resource eResource() {
		return tObject.eResource();
	}

	@Override
	public EObject eContainer() {
		return tObject.eContainerAt(instant);
	}

	@Override
	public EStructuralFeature eContainingFeature() {
		return tObject.eContainingFeature();
	}

	@Override
	public EReference eContainmentFeature() {
		return tObject.eContainmentFeature();
	}

	@Override
	public EList<EObject> eContents() {
		return tObject.eContentsAt(instant);
	}

	@Override
	public TreeIterator<EObject> eAllContents() {
		return tObject.eAllContents();
	}

	@Override
	public boolean eIsProxy() {
		return tObject.eIsProxy();
	}

	@Override
	public EList<EObject> eCrossReferences() {
		return tObject.eCrossReferences();
	}

	@Override
	public Object eGet(EStructuralFeature feature) {
		return tObject.eGetAt(instant, feature);
	}

	@Override
	public Object eGet(EStructuralFeature feature, boolean resolve) {
		return tObject.eGetAt(instant, feature);
	}

	@Override
	public void eSet(EStructuralFeature feature, Object newValue) {
		throw new UnsupportedOperationException("Past TObjects cannot be modified");
	}

	@Override
	public boolean eIsSet(EStructuralFeature feature) {
		return tObject.eIsSetAt(instant, feature);
	}

	@Override
	public void eUnset(EStructuralFeature feature) {
		throw new UnsupportedOperationException("Past TObjects cannot be modified");
	}

	@Override
	public Object eInvoke(EOperation operation, EList<?> arguments) throws InvocationTargetException {
		throw new UnsupportedOperationException("Past TObjects cannot be modified");
	}
}
