/*******************************************************************************
 * Copyright (c) 2008, 2013 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 *    Abel Gómez - Adapted to TemporalEMF models
 ******************************************************************************/

package edu.uoc.som.temf.generator.migrator;

import java.util.Arrays;

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

import edu.uoc.som.temf.TEmfPlugin;
import edu.uoc.som.temf.core.TObject;
import edu.uoc.som.temf.core.impl.TObjectImpl;
import edu.uoc.som.temf.generator.TEmfGeneratorPlugin;

/**
 * @author abelgomez
 */
public abstract class TEmfMigratorUtil {

	private static final String ROOT_EXTENDS_CLASS = TObjectImpl.class.getName();

	private static final String ROOT_EXTENDS_INTERFACE = TObject.class.getName();

	public static final String PLUGIN_VARIABLE_TEMF = "TEMF=" + TEmfPlugin.PLUGIN_ID;

	// @formatter:off
	public static final String[] PLUGIN_DEPENDENCIES = { "org.apache.commons.io",
															"org.apache.commons.lang3",
															"net.bytebuddy.byte-buddy",
															"com.google.guava",
															"com.h2database"
														};
	// @formatter:on

	public static final String TEMPLATES_PATH = "platform:/plugin/" + TEmfGeneratorPlugin.PLUGIN_ID + "/templates";

	private TEmfMigratorUtil() {
	}

	public static String adjustGenModel(GenModel genModel) {
		StringBuilder builder = new StringBuilder();

		if (genModel.getFeatureDelegation() != GenDelegationKind.REFLECTIVE_LITERAL) {
			genModel.setFeatureDelegation(GenDelegationKind.REFLECTIVE_LITERAL);
			builder.append("Set Feature Delegation = ");
			builder.append(GenDelegationKind.REFLECTIVE_LITERAL);
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

		if (!genModel.isDynamicTemplates()) {
			genModel.setDynamicTemplates(true);
			builder.append("Set dynamic templates = ");
			builder.append("true");
			builder.append("\n");
		}

		if (!TEMPLATES_PATH.equals(genModel.getTemplateDirectory())) {
			genModel.setTemplateDirectory(TEMPLATES_PATH);
			builder.append("Set template directory = ");
			builder.append(TEMPLATES_PATH);
			builder.append("\n");
		}

		EList<String> pluginVariables = genModel.getModelPluginVariables();
		if (!pluginVariables.contains(PLUGIN_VARIABLE_TEMF)) {
			pluginVariables.add(PLUGIN_VARIABLE_TEMF);
			pluginVariables.addAll(Arrays.asList(PLUGIN_DEPENDENCIES));
			builder.append("Added Model Plugin Variables = ");
			builder.append(PLUGIN_VARIABLE_TEMF);
			Arrays.asList(PLUGIN_DEPENDENCIES).forEach(dep -> {
				builder.append(", ");
				builder.append(dep);
			});
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
