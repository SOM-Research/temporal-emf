/**
 * Copyright (c) 2020 SOM Research Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Abel Gómez - initial API and implementation
 */
package edu.uoc.som.temf.testmodel.impl;

import edu.uoc.som.temf.core.impl.TObjectImpl;

import edu.uoc.som.temf.testmodel.Node;
import edu.uoc.som.temf.testmodel.TestmodelPackage;

import java.time.Instant;

import java.util.Date;
import java.util.SortedMap;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Node</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link edu.uoc.som.temf.testmodel.impl.NodeImpl#getName <em>Name</em>}</li>
 *   <li>{@link edu.uoc.som.temf.testmodel.impl.NodeImpl#getChildren <em>Children</em>}</li>
 * </ul>
 *
 * @generated
 */
public class NodeImpl extends TObjectImpl implements Node {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected NodeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return TestmodelPackage.Literals.NODE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected int eStaticFeatureCount() {
		return 0;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return (String)eGet(TestmodelPackage.Literals.NODE__NAME, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		eSet(TestmodelPackage.Literals.NODE__NAME, newName);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getNameAt(Instant instant) {
		return (String)eGetAt(instant, TestmodelPackage.Literals.NODE__NAME);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetNameAt(Instant instant) {
		return eIsSetAt(instant, TestmodelPackage.Literals.NODE__NAME);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public SortedMap<Date,String> getNameAllBetween(Instant start, Instant end) {
		return (SortedMap)eGetAllBetween(start, end, TestmodelPackage.Literals.NODE__NAME);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public EList<Node> getChildren() {
		return (EList<Node>)eGet(TestmodelPackage.Literals.NODE__CHILDREN, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public EList<Node> getChildrenAt(Instant instant) {
		return (EList<Node>)eGetAt(instant, TestmodelPackage.Literals.NODE__CHILDREN);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetChildrenAt(Instant instant) {
		return eIsSetAt(instant, TestmodelPackage.Literals.NODE__CHILDREN);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public SortedMap<Date,EList<Node>> getChildrenAllBetween(Instant start, Instant end) {
		return (SortedMap)eGetAllBetween(start, end, TestmodelPackage.Literals.NODE__CHILDREN);
	}

} //NodeImpl
