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
package edu.uoc.som.temf.testmodel;

import edu.uoc.som.temf.core.TObject;

import java.time.Instant;

import java.util.Date;
import java.util.SortedMap;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Node</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link edu.uoc.som.temf.testmodel.Node#getName <em>Name</em>}</li>
 *   <li>{@link edu.uoc.som.temf.testmodel.Node#getChildren <em>Children</em>}</li>
 * </ul>
 *
 * @see edu.uoc.som.temf.testmodel.TestmodelPackage#getNode()
 * @model
 * @extends TObject
 * @generated
 */
public interface Node extends TObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see edu.uoc.som.temf.testmodel.TestmodelPackage#getNode_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link edu.uoc.som.temf.testmodel.Node#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Unsets the value of the '{@link edu.uoc.som.temf.testmodel.Node#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetName()
	 * @generated
	 */
	void unsetName();

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute at the given instant.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute at the given instant.
	 * @see #getName()
	 * @generated
	 */
	String getNameAt(Instant instant);

	/**
	 * Returns whether the value of the '{@link edu.uoc.som.temf.testmodel.Node#getName <em>Name</em>}' attribute is set at the given instant.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Name</em>' attribute is set at the given instant.
	 * @see #isSetName()
	 * @generated
	 */
	boolean isSetNameAt(Instant instant);

	/**
	 * Returns the instant when the value of the '{@link edu.uoc.som.temf.testmodel.Node#getName <em>Name</em>}' attribute was last changed.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return when the value or the list of the '<em>Name</em>' attribute was last changed.
	 * @see #isSetName()
	 * @generated
	 */
	Instant whenChangedName();

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute in the given time span.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute in the given time span.
	 * @see #getName()
	 * @generated
	 */
	SortedMap<Date,String> getNameAllBetween(Instant start, Instant end);

	/**
	 * Returns the value of the '<em><b>Children</b></em>' containment reference list.
	 * The list contents are of type {@link edu.uoc.som.temf.testmodel.Node}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Children</em>' containment reference list.
	 * @see edu.uoc.som.temf.testmodel.TestmodelPackage#getNode_Children()
	 * @model containment="true"
	 * @generated
	 */
	EList<Node> getChildren();

	/**
	 * Unsets the value of the '{@link edu.uoc.som.temf.testmodel.Node#getChildren <em>Children</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetChildren()
	 * @generated
	 */
	void unsetChildren();

	/**
	 * Returns the value of the '<em><b>Children</b></em>' containment reference list at the given instant.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Children</em>' containment reference list at the given instant.
	 * @see #getChildren()
	 * @generated
	 */
	EList<Node> getChildrenAt(Instant instant);

	/**
	 * Returns whether the value of the '{@link edu.uoc.som.temf.testmodel.Node#getChildren <em>Children</em>}' containment reference list is set at the given instant.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Children</em>' containment reference list is set at the given instant.
	 * @see #isSetChildren()
	 * @generated
	 */
	boolean isSetChildrenAt(Instant instant);

	/**
	 * Returns the instant when the value of the '{@link edu.uoc.som.temf.testmodel.Node#getChildren <em>Children</em>}' containment reference list was last changed.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return when the value or the list of the '<em>Children</em>' containment reference list was last changed.
	 * @see #isSetChildren()
	 * @generated
	 */
	Instant whenChangedChildren();

	/**
	 * Returns the value of the '<em><b>Children</b></em>' containment reference list in the given time span.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Children</em>' containment reference list in the given time span.
	 * @see #getChildren()
	 * @generated
	 */
	SortedMap<Date,EList<Node>> getChildrenAllBetween(Instant start, Instant end);

} // Node
