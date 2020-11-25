package edu.uoc.som.temf.generator.tests;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.codegen.ecore.generator.Generator;
import org.eclipse.emf.codegen.ecore.genmodel.GenDelegationKind;
import org.eclipse.emf.codegen.ecore.genmodel.GenJDKLevel;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.GenModelFactory;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.codegen.ecore.genmodel.generator.GenBaseGeneratorAdapter;
import org.eclipse.emf.codegen.ecore.genmodel.util.GenModelUtil;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.junit.jupiter.api.Test;

import edu.uoc.som.temf.core.TObject;
import edu.uoc.som.temf.core.impl.TObjectImpl;
import edu.uoc.som.temf.generator.TEmfGeneratorPlugin;
import edu.uoc.som.temf.generator.migrator.TEmfMigratorUtil;
import edu.uoc.som.temf.testmodel.TestmodelPlugin;

class GenerateModelTest {

	private static final String TEST_MODEL_PLUGIN_ID = TestmodelPlugin.getPlugin().getSymbolicName();
	private static final String TEST_MODEL_PATH = "/model/testmodel.ecore";

	@Test
	void testMigrateGenmodel() throws IOException {
		GenModel genModel = createGenModel();

		// @formatter:off
		assertAll("Check TEMF-related properties of the genmodel file",
				() -> assertEquals(GenJDKLevel.JDK80_LITERAL, genModel.getComplianceLevel()),
				() -> assertEquals(GenDelegationKind.REFLECTIVE_LITERAL, genModel.getFeatureDelegation()),
				() -> assertEquals(TObjectImpl.class.getName(), genModel.getRootExtendsClass()),
				() -> assertEquals(TObject.class.getName(), genModel.getRootExtendsInterface()),
				() -> assertTrue(genModel.isDynamicTemplates()),
				() -> assertEquals("platform:/plugin/" + TEmfGeneratorPlugin.PLUGIN_ID + "/templates", genModel.getTemplateDirectory()),
				() -> assertTrue(genModel.getModelPluginVariables().contains("org.apache.commons.io")),
				() -> assertTrue(genModel.getModelPluginVariables().contains("org.apache.commons.lang3")),
				() -> assertTrue(genModel.getModelPluginVariables().contains("net.bytebuddy.byte-buddy")),
				() -> assertTrue(genModel.getModelPluginVariables().contains("org.apache.commons.lang3")),
				() -> assertTrue(genModel.getModelPluginVariables().contains("com.google.guava"))
		);
		// @formatter:on
	}

	@Test
	void testGenerateModelCode() throws Exception {
		GenModel genModel = createGenModel();
		Resource genModelResource = new XMIResourceImpl(
				URI.createPlatformResourceURI(TEST_MODEL_PLUGIN_ID + TEST_MODEL_PATH, true).trimFileExtension().appendFileExtension("genmodel"));
		genModelResource.getContents().add(genModel);

		Generator generator = GenModelUtil.createGenerator(genModel);
		// In case of error, new BasicMonitor.Printing(System.out) maybe used instead of
		// new BasicMonitor()
		generator.generate(genModel, GenBaseGeneratorAdapter.MODEL_PROJECT_TYPE, new BasicMonitor());

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(TEST_MODEL_PLUGIN_ID);

		// @formatter:off
		assertAll("Check all files have been generated",
				() -> assertTrue(project.findMember("/.classpath").exists()),
				() -> assertTrue(project.findMember("/.project").exists()),
				() -> assertTrue(project.findMember("/build.properties").exists()),
				() -> assertTrue(project.findMember("/plugin.properties").exists()),
				() -> assertTrue(project.findMember("/plugin.xml").exists()),
				() -> assertTrue(project.findMember("/META-INF/MANIFEST.MF").exists()),
				() -> assertTrue(project.findMember("/src/edu/uoc/som/temf/testmodel/impl/NodeImpl.java").exists()),
				() -> assertTrue(project.findMember("/src/edu/uoc/som/temf/testmodel/impl/TestmodelFactoryImpl.java").exists()),
				() -> assertTrue(project.findMember("/src/edu/uoc/som/temf/testmodel/impl/TestmodelPackageImpl.java").exists()),
				() -> assertTrue(project.findMember("/src/edu/uoc/som/temf/testmodel/Node.java").exists()),
				() -> assertTrue(project.findMember("/src/edu/uoc/som/temf/testmodel/TestmodelFactory.java").exists()),
				() -> assertTrue(project.findMember("/src/edu/uoc/som/temf/testmodel/TestmodelPackage.java").exists()),
				() -> assertTrue(project.findMember("/src/edu/uoc/som/temf/testmodel/TestmodelPlugin.java").exists()),
				() -> assertTrue(project.findMember("/src/edu/uoc/som/temf/testmodel/util/TestmodelAdapterFactory.java").exists()),
				() -> assertTrue(project.findMember("/src/edu/uoc/som/temf/testmodel/util/TestmodelSwitch.java").exists())
		);
		// @formatter:on

		genModelResource.save(Collections.emptyMap());
	}

	private static GenModel createGenModel() throws IOException {
		Resource resource = new XMIResourceImpl(URI.createPlatformPluginURI(TEST_MODEL_PLUGIN_ID + TEST_MODEL_PATH, true));
		resource.load(Collections.emptyMap());

		GenModel genModel = GenModelFactory.eINSTANCE.createGenModel();
		genModel.initialize(Arrays.asList((EPackage) resource.getContents().get(0)));
		TEmfMigratorUtil.adjustGenModel(genModel);

		GenPackage genPackage = (GenPackage) genModel.getGenPackages().get(0);

		genModel.setModelPluginClass("TestmodelPlugin");
		genModel.setModelName(genPackage.getPrefix());
		genModel.setModelPluginID(TEST_MODEL_PLUGIN_ID);
		genModel.setModelDirectory("/" + TEST_MODEL_PLUGIN_ID + "/src");
		genModel.setCanGenerate(true);

		genPackage.setBasePackage(TEST_MODEL_PLUGIN_ID.substring(0, TEST_MODEL_PLUGIN_ID.lastIndexOf(".")));
		return genModel;
	}

}
