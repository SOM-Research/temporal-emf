package edu.uoc.som.temf.testmodel.tests;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.junit.jupiter.api.Test;

import edu.uoc.som.temf.core.TObject;
import edu.uoc.som.temf.testmodel.TestmodelFactory;
import edu.uoc.som.temf.testmodel.TestmodelPackage;
import edu.uoc.som.temf.testmodel.TestmodelPlugin;

class TestModelTest {

	private static final String TEST_MODEL_PLUGIN_ID = TestmodelPlugin.getPlugin().getBundle().getSymbolicName();
	private static final String TEST_MODEL_ECORE_PATH = "/model/testmodel.ecore";

	@Test
	void testGeneratedModelIsUpToDate() throws Exception {
		Resource resource = new XMIResourceImpl(URI.createPlatformPluginURI(TEST_MODEL_PLUGIN_ID + TEST_MODEL_ECORE_PATH, true));
		resource.load(Collections.emptyMap());
		EPackage ePackage = (EPackage) resource.getContents().get(0);
		
		// @formatter:off
		assertAll("Check EPackage data on Test Model is right",
				() -> assertEquals(ePackage.getName(), TestmodelPackage.eNAME),
				() -> assertEquals(ePackage.getNsPrefix(), TestmodelPackage.eNS_PREFIX),
				() -> assertEquals(ePackage.getNsURI(), TestmodelPackage.eNS_URI),
				() -> assertEquals(ePackage.getEClassifiers().size(), TestmodelPackage.eINSTANCE.getEClassifiers().size())
		);
		assertTrue(TObject.class.isAssignableFrom(TestmodelFactory.eINSTANCE.createNode().getClass()), 
				"Check that Test Model objects extend TObject");
		// @formatter:on
		
		ePackage.getEClassifiers().forEach(ec -> checkEquals(ec, TestmodelPackage.eINSTANCE.getEClassifier(ec.getName())));
	}

	private static void checkEquals(EClassifier expected, EClassifier actual) {
		// @formatter:off
		assertAll("Check EClassifiers have same name and are of the same type (EClass / EDataTypes) ",
				() -> assertEquals(expected.getName(), actual.getName()),
				() -> assertEquals(expected.getClass(), actual.getClass())
		);
		// @formatter:on
		
		if (expected instanceof EClass) {
			checkEquals((EClass) expected, (EClass) actual);
		}
	}

	private static void checkEquals(EClass expected, EClass actual) {
		// @formatter:off
		assertAll("Check EClasses have the same basic characteristics",
				() -> assertEquals(expected.isAbstract(), expected.isAbstract()),
				() -> assertEquals(expected.isInterface(), expected.isInterface()),
				() -> assertEquals(expected.getEAttributes().size(), expected.getEAttributes().size()),
				() -> assertEquals(
						expected.getESuperTypes().stream().map(st -> st.getName()).collect(Collectors.toSet()), 
						actual.getESuperTypes().stream().map(st -> st.getName()).collect(Collectors.toSet()),
						"Check ESuperTypes are the same"
				)
		);
		// @formatter:on
		
		expected.getEStructuralFeatures().forEach(esf -> checkEquals(esf, actual.getEStructuralFeature(esf.getName())));
	}

	private static void checkEquals(EStructuralFeature expected, EStructuralFeature actual) {
		// @formatter:off
		assertAll("Check EStructuralFeatures have the same basic characteristics",
				() -> assertEquals(expected.getName(), actual.getName()),
				() -> assertEquals(expected.getLowerBound(), actual.getLowerBound()),
				() -> assertEquals(expected.getUpperBound(), actual.getUpperBound()),
				() -> assertEquals(expected.getClass(), actual.getClass()),
				() -> assertEquals(expected.getEType().getName(), actual.getEType().getName())
		);
		// @formatter:off
	}
}
