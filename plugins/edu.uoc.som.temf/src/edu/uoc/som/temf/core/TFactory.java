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
package edu.uoc.som.temf.core;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;

import edu.uoc.som.temf.core.impl.TFactoryImpl;


public interface TFactory extends EFactory {

	static TFactory eINSTANCE = new TFactoryImpl();

	@Override
	TObject create(EClass eClass);
	
}
