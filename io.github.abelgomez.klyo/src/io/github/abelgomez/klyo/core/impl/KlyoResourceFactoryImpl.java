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
package io.github.abelgomez.klyo.core.impl;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

import io.github.abelgomez.klyo.KlyoURI;
import io.github.abelgomez.klyo.core.KlyoResourceFactory;
import io.github.abelgomez.klyo.hbase.impl.KlyoHbaseResourceImpl;

public class KlyoResourceFactoryImpl implements KlyoResourceFactory {

	@Override
	public Resource createResource(URI uri) {
		if (StringUtils.equals(KlyoURI.KLYO_HBASE_SCHEME, uri.scheme())) {
			return new KlyoHbaseResourceImpl(uri);
		} else {
			return null;
		}
	}

}
