/*******************************************************************************
 * Copyright (c) 2018 SOM Research Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Abel G�mez - initial API and implementation
 *******************************************************************************/
package edu.uoc.som.temf.core.impl;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

import edu.uoc.som.temf.TURI;
import edu.uoc.som.temf.core.TResourceFactory;
import edu.uoc.som.temf.hbase.impl.HbaseTResourceImpl;

public class TResourceFactoryImpl implements TResourceFactory {

	@Override
	public Resource createResource(URI uri) {
		if (StringUtils.equals(TURI.TEMF_HBASE_SCHEME, uri.scheme())) {
			return new HbaseTResourceImpl(uri);
		} else {
			return null;
		}
	}

}
