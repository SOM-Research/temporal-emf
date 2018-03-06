/*******************************************************************************
 * Copyright (c) 2008, 2013 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 *    Abel Gómez - Adapted to Klyo models
 ******************************************************************************/

package io.github.abelgomez.klyo.ui.migrator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.codegen.ecore.genmodel.GenDelegationKind;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.WrappedException;

import io.github.abelgomez.klyo.KlyoPlugin;
import io.github.abelgomez.klyo.core.KlyoEObject;
import io.github.abelgomez.klyo.core.impl.KlyoEObjectImpl;

/**
 * @author abelgomez
 */
public abstract class KlyoMigratorUtil {

	private static final String ROOT_EXTENDS_CLASS = KlyoEObjectImpl.class.getName();

	private static final String ROOT_EXTENDS_INTERFACE = KlyoEObject.class.getName();

	public static final String PLUGIN_VARIABLE_KLYO = "KLYO=" + KlyoPlugin.PLUGIN_ID;

	private KlyoMigratorUtil() {
	}

	public static String adjustGenModel(GenModel genModel) {
		return adjustGenModel(genModel, GenDelegationKind.REFLECTIVE_LITERAL);
	}

	public static String adjustGenModel(GenModel genModel, GenDelegationKind featureDelegation) {
		StringBuilder builder = new StringBuilder();

		if (genModel.getFeatureDelegation() != featureDelegation) {
			genModel.setFeatureDelegation(featureDelegation);
			builder.append("Set Feature Delegation = ");
			builder.append(featureDelegation);
			builder.append("\n");
		}

		if (!ROOT_EXTENDS_CLASS.equals(genModel.getRootExtendsClass())) {
			genModel.setRootExtendsClass(ROOT_EXTENDS_CLASS);
			builder.append("Set Root Extends Class = ");
			builder.append(ROOT_EXTENDS_CLASS);
			builder.append("\n");
		}

		if (!ROOT_EXTENDS_INTERFACE.equals(genModel.getRootExtendsInterface())) {
			genModel.setRootExtendsInterface(ROOT_EXTENDS_INTERFACE);
			builder.append("Set Root Extends Interface = ");
			builder.append(ROOT_EXTENDS_INTERFACE);
			builder.append("\n");
		}

		EList<String> pluginVariables = genModel.getModelPluginVariables();
		if (!pluginVariables.contains(PLUGIN_VARIABLE_KLYO)) {
			pluginVariables.add(PLUGIN_VARIABLE_KLYO);
			builder.append("Added Model Plugin Variables = ");
			builder.append(PLUGIN_VARIABLE_KLYO);
			builder.append("\n");
		}

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFolder modelFolder = root.getFolder(new Path(genModel.getModelDirectory()));
		IProject modelProject = modelFolder.getProject();
		if (!modelProject.exists()) {
			try {
				modelProject.create(new NullProgressMonitor());
				builder.append("Created target model project" + "\n"); //$NON-NLS-2$
			} catch (CoreException ex) {
				throw new WrappedException(ex);
			}
		}

		if (!modelProject.isOpen()) {
			try {
				modelProject.open(new NullProgressMonitor());
				builder.append("Opened target model project" + "\n"); //$NON-NLS-2$
			} catch (CoreException ex) {
				throw new WrappedException(ex);
			}
		}

		return builder.length() == 0 ? null : builder.toString();
	}
}
