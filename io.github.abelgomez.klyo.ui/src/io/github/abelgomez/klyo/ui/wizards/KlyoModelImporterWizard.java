/*******************************************************************************
 * Copyright (c) 2018 Abel G�mez.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Abel G�mez - initial API and implementation
 *******************************************************************************/
package io.github.abelgomez.klyo.ui.wizards;

import org.eclipse.emf.converter.ModelConverter;
import org.eclipse.emf.importer.ui.contribution.base.ModelImporterDetailPage;
import org.eclipse.emf.importer.ui.contribution.base.ModelImporterPackagePage;
import org.eclipse.emf.importer.ui.contribution.base.ModelImporterWizard;

import io.github.abelgomez.klyo.ui.migrator.KlyoImporter;

/**
 * 
 * @author abelgomez
 *
 */
public class KlyoModelImporterWizard extends ModelImporterWizard {
	public KlyoModelImporterWizard() {
	}

	@Override
	protected ModelConverter createModelConverter() {
		return new KlyoImporter();
	}

	@Override
	public void addPages() {
		ModelImporterDetailPage detailPage = new ModelImporterDetailPage(getModelImporter(), "EcoreModel");
		detailPage.setTitle("Ecore &Import");
		detailPage.setDescription(detailPage.showGenModel()
				? "Specify one or more '.ecore' or '.emof' URIs, try to load them, and choose a file name for the generator model"
				: "Specify one or more '.ecore' or '.emof' URIs and try to load them");
		addPage(detailPage);

		ModelImporterPackagePage packagePage = new ModelImporterPackagePage(getModelImporter(), "EcorePackages");
		packagePage.setShowReferencedGenModels(true);
		addPage(packagePage);
	}
}
