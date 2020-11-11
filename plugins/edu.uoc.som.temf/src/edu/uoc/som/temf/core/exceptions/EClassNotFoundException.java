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
package edu.uoc.som.temf.core.exceptions;


public class EClassNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public EClassNotFoundException() {
	}
	
	public EClassNotFoundException(String message) {
		super(message);
	}
	
	public EClassNotFoundException(Throwable t) {
		super(t);
	}

	public EClassNotFoundException(String message, Throwable t) {
		super(message, t);
	}
	
}
	
