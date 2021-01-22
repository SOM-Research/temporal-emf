package edu.uoc.som.temf.tests;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import edu.uoc.som.temf.TURI;
import edu.uoc.som.temf.core.TResource;
import edu.uoc.som.temf.testmodel.Node;
import edu.uoc.som.temf.testmodel.TestmodelFactory;
import edu.uoc.som.temf.tests.util.TestUtils;
import edu.uoc.som.temf.tests.util.TestUtils.PopulationInfo;

class TestTemf {

	/**
	 * Test that the "org.eclipse.emf.ecore.protocol_parser" extension point is
	 * properly set
	 */
	@Test
	@Order(0)
	void testTResourceFactory() {
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.createResource(TURI.createTMapURI(new File(UUID.randomUUID().toString())));

		assertTrue(resource instanceof TResource, "Check that the TResourceFactory is correctly registered");
	}

	@Test
	void testCreateTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = TestUtils.createTResource(resourceFile);

		assertFalse(resource.isLoaded(), "Check a newly created TResource is unloaded");
		assertFalse(resourceFile.exists(), "Check a newly created unsaved TResource does not produce a file");
	}

	@Test
	void testSaveUnloadedTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = TestUtils.createTResource(resourceFile);
		resource.save(Collections.emptyMap());

		assertFalse(resource.isLoaded(), "Check that saving an unloaded TResource does not mark it as loaded");
		assertFalse(resourceFile.exists(), "Check that saving an unloaded TResource does not produce a file");
		assertEquals(0, TestUtils.countResourceContents(resource), "Check that TResource is empty");

	}

	@Test
	void testLoadEmptyTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = TestUtils.createTResource(resourceFile);
		resource.getContents().clear();

		assertTrue(resource.isLoaded(), "Check a newly created TResource is loaded when its contents are cleared");
		assertFalse(resourceFile.exists(), "Check a newly created unsaved TResource does not produce a file when contents are cleared");
		assertEquals(0, TestUtils.countResourceContents(resource), "Check that TResource is empty");
	}

	@Test
	void testSaveLoadedEmptyTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = TestUtils.createTResource(resourceFile);
		resource.getContents().clear();
		resource.save(Collections.emptyMap());

		assertEquals(0, TestUtils.countResourceContents(resource), "Check that TResource is still empty after saving");
		assertTrue(resource.isLoaded(), "Check saved TResource is still loaded after saving");
		assertTrue(resourceFile.isDirectory(), "Check TResource has produced a directory on disk");
		assertArrayEquals(new String[] { "mvstore" }, resourceFile.list(), "Check TResource has produced a mvstore file on disk");
	}

	@Test
	void testCreateAndPopulateUnsavedTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = TestUtils.createTResource(resourceFile);
		List<PopulationInfo> populationInfo = TestUtils.populateResourceDepthFirst(resource);

		assertTrue(resource.isLoaded(), "Check a newly created TResource is loaded when contents are added");
		assertFalse(resourceFile.exists(), "Check a newly created unsaved TResource does not produce a file when contents are added");
		assertEquals(populationInfo.size(), TestUtils.countResourceContents(resource), "Check TResource contents are correct");
	}

	@Test
	void testUnloadPopulatedUnsavedTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = TestUtils.createTResource(resourceFile);
		TestUtils.populateResourceDepthFirst(resource);
		resource.unload();

		assertFalse(resource.isLoaded(), "Check the TResource is unloaded");
		assertFalse(resourceFile.exists(), "Check the unsaved TResource does not produce a file when unloaded");
		assertEquals(0, TestUtils.countResourceContents(resource), "Check TResource is empty");
	}

	@Test
	void testCreateTResourceSavingBeforePopulating() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = TestUtils.createTResource(resourceFile);
		resource.getContents().clear();
		resource.save(Collections.emptyMap());
		List<PopulationInfo> populationInfo = TestUtils.populateResourceDepthFirst(resource);

		assertTrue(resource.isLoaded(), "Check the TResource is loaded");
		assertTrue(resourceFile.exists(), "Check the TResource exists");
		assertEquals(populationInfo.size(), TestUtils.countResourceContents(resource), "Check TResource contents are correct when populating it after saving to disk");

		resource.unload();
	}

	@Test
	void testCreateTResourceSavingAfterPopulating() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = TestUtils.createTResource(resourceFile);
		List<PopulationInfo> populationInfo = TestUtils.populateResourceDepthFirst(resource);
		resource.save(Collections.emptyMap());

		assertTrue(resource.isLoaded(), "Check the TResource is loaded");
		assertTrue(resourceFile.exists(), "Check the TResource exists");
		assertEquals(populationInfo.size(), TestUtils.countResourceContents(resource), "Check TResource contents are correct when populating it after saving to disk");

		resource.unload();
	}

	@Test
	void testUnloadPopulatedSavedTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = TestUtils.createTResource(resourceFile);
		TestUtils.populateResourceDepthFirst(resource);
		resource.save(Collections.emptyMap());
		resource.unload();

		assertFalse(resource.isLoaded(), "Check the TResource is usloaded");
		assertTrue(resourceFile.exists(), "Check the TResource exists");
		assertEquals(0, TestUtils.countResourceContents(resource), "Check TResource is empty");
	}

	@Test
	void testReadTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = TestUtils.createTResource(resourceFile);
		List<PopulationInfo> populationInfo = TestUtils.populateResourceDepthFirst(resource);
		resource.save(Collections.emptyMap());
		resource.unload();

		TResource newResource = TestUtils.getTResource(resourceFile);

		// @formatter:off
		assertAll("Check TResource contents",
				() -> assertEquals(populationInfo.get(0).eltName, ((Node) newResource.getContents().get(0)).getName(), "Check name of last created element"),
				() -> assertEquals(populationInfo.size(), TestUtils.countResourceContents(newResource), "Check number of elements in TResource")
		);
		// @formatter:on

		newResource.unload();
	}

	@Test
	void testUnloadPreexistingTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = TestUtils.createTResource(resourceFile);
		TestUtils.populateResourceDepthFirst(resource);
		resource.save(Collections.emptyMap());
		resource.unload();

		TResource newResource = TestUtils.getTResource(resourceFile);
		newResource.unload();

		assertFalse(newResource.isLoaded(), "Check the newly loaded TResource is now unloaded");
		assertTrue(resourceFile.exists(), "Check the TResource still exists");
		assertEquals(0, TestUtils.countResourceContents(newResource), "Check newly loaded TResource is now empty");
	}

	@Test
	void testCountTResourceContents() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = TestUtils.createTResource(resourceFile);
		resource.save(Collections.emptyMap());
		List<PopulationInfo> populationInfo = TestUtils.populateResourceDepthFirst(resource);

		assertEquals(populationInfo.size(), TestUtils.countResourceContents(resource), "Check TResource has all the created elements");
	}

	@Test
	void testCountTResourceContentsAt() throws Exception {
		TResource resource = TestUtils.createTResource(TestUtils.createNonExistingTempFile());
		resource.save(Collections.emptyMap());
		List<PopulationInfo> populationInfo = TestUtils.populateResourceRandomly(resource);

		int position = (int) (populationInfo.size() * 0.75);
		Instant instant = populationInfo.get(position).instant;

		AtomicInteger count = new AtomicInteger();
		Stack<EObject> remaining = new Stack<>();
		remaining.addAll(resource.getContentsAt(instant));
		do {
			EObject elt = remaining.pop();
			count.incrementAndGet();
			remaining.addAll(((Node) elt).getChildrenAt(instant));
		} while (!remaining.empty());
		assertEquals(position + 1, count.get(), "Check TResource has all the created elements using getContentsAt");
	}

	@Test
	void testCountTResourceAllContentsAt() throws Exception {
		TResource resource = TestUtils.createTResource(TestUtils.createNonExistingTempFile());
		resource.save(Collections.emptyMap());
		List<PopulationInfo> populationInfo = TestUtils.populateResourceRandomly(resource);
		
		int position = (int) (populationInfo.size() * 0.75);
		Instant instant = populationInfo.get(position).instant;
		
		AtomicInteger count = new AtomicInteger();
		Stack<EObject> remaining = new Stack<>();
		remaining.addAll(resource.getContentsAt(instant));
		do {
			EObject elt = remaining.pop();
			count.incrementAndGet();
			remaining.addAll(((Node) elt).getChildrenAt(instant));
		} while (!remaining.empty());
		assertEquals(position + 1, count.get(), "Check TResource has all the created elements using getContentsAt");
	}
	
	@Test
	void testTResourceAt() throws Exception {
		TResource resource = TestUtils.createTResource(TestUtils.createNonExistingTempFile());
		resource.save(Collections.emptyMap());
		List<PopulationInfo> populationInfo = TestUtils.populateResourceRandomly(resource);

		// Pick a position and an instant
		int position = (int) (populationInfo.size() * 0.75);
		Instant instant = populationInfo.get(position).instant;

		AtomicInteger count = new AtomicInteger();

		// Count using getAllContentsAt
		count.set(0);
		resource.at(instant).getAllContents().forEachRemaining((e) -> count.incrementAndGet());
		assertEquals(position + 1, count.get(), "Check TResource has all the created elements using getAllContentsAt");

		// Count (manually) using getContentsAt
		count.set(0);
		Stack<EObject> remaining = new Stack<>();
		remaining.addAll(resource.at(instant).getContents());
		do {
			EObject elt = remaining.pop();
			count.incrementAndGet();
			remaining.addAll(((Node) elt).getChildren());
		} while (!remaining.empty());
		assertEquals(position + 1, count.get(), "Check TResource has all the created elements using getContentsAt");
	}


	@Test
	void testDettachedTObjectModifications() throws Exception {
		Node root = TestmodelFactory.eINSTANCE.createNode();
		root.setName("Name 1");
		root.setName("Name 2");
		root.setName("Name 3");
		assertThrows(UnsupportedOperationException.class, () -> root.getNameAt(Instant.MAX), "Check dettached TObjects don't getAt");
		assertThrows(UnsupportedOperationException.class, () -> root.getNameAllBetween(Instant.MIN, Instant.MAX), "Check dettached TObjects don't getAllBetween");
	}

	@Test
	void testAttachedTObjectModifications() throws Exception {
		TResource resource = TestUtils.createTResource(TestUtils.createNonExistingTempFile());
		
		Node root = TestmodelFactory.eINSTANCE.createNode();
		resource.getContents().add(root);
		root.setName("Name 1");
		Instant setPropInstant1Exact = root.whenChangedName();
		Instant setPropInstant1Approx = resource.getClock().instant();
		root.setName("Name 2");
		Instant setPropInstant2Exact = root.whenChangedName();
		Instant setPropInstant2Approx = resource.getClock().instant();
		root.setName("Name 3");
		Instant setPropInstant3Exact = root.whenChangedName();
		Instant setPropInstant3Approx = resource.getClock().instant();
		
		Node child1 = TestmodelFactory.eINSTANCE.createNode();
		root.getChildren().add(child1);
		Instant addChildInstant1Exact = root.whenChangedChildren();

		child1.setName("Name 1");
		child1.setName("Name 2");
		child1.setName("Name 3");
		
		
		Node child2 = TestmodelFactory.eINSTANCE.createNode();
		root.getChildren().add(child2);
		Instant addChildInstant2Exact = root.whenChangedChildren();

		child2.setName("Name 1");
		child2.setName("Name 2");
		child2.setName("Name 3");
		child2.unsetName();
		
		root.unsetChildren();
		Instant clearChildInstant3Exact = root.whenChangedChildren();
		
		assertEquals(3, root.getNameAllBetween(Instant.MIN, Instant.MAX).size(), "Check attached TObjects keep temporal information");
		assertEquals("Name 1", root.getNameAt(setPropInstant1Exact), "Check 1st value in history");
		assertEquals("Name 1", root.getNameAt(setPropInstant1Approx), "Check 1st value in history");
		assertEquals("Name 2", root.getNameAt(setPropInstant2Exact), "Check 2nd value in history");
		assertEquals("Name 2", root.getNameAt(setPropInstant2Approx), "Check 2nd value in history");
		assertEquals("Name 3", root.getNameAt(setPropInstant3Exact), "Check 3rd value in history");
		assertEquals("Name 3", root.getNameAt(setPropInstant3Approx), "Check 3rd value in history");
		
		assertEquals(1, root.getChildrenAt(addChildInstant1Exact).size(), "Check add child in history");
		assertEquals(2, root.getChildrenAt(addChildInstant2Exact).size(), "Check add child in history");
		assertEquals(0, root.getChildrenAt(clearChildInstant3Exact).size(), "Check clear child in history");
		
		assertTrue(root.getChildren().isEmpty(), "Check many-valued reference is cleared");
		
		assertFalse(child2.isSetChildrenAt(Instant.MAX), "Check property unset");
	}


}
