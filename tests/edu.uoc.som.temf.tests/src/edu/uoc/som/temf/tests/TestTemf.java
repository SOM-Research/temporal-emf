package edu.uoc.som.temf.tests;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

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
import edu.uoc.som.temf.tstores.TStore;

class TestTemf {

	static class PopulationInfo {
		Instant instant;
		String eltName;

		private PopulationInfo(Instant instant, String lastElt) {
			this.instant = instant;
			this.eltName = lastElt;
		}
	}

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
		TResource resource = createTResource(resourceFile);

		assertFalse(resource.isLoaded(), "Check a newly created TResource is unloaded");
		assertFalse(resourceFile.exists(), "Check a newly created unsaved TResource does not produce a file");
	}

	@Test
	void testSaveUnloadedTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = createTResource(resourceFile);
		resource.save(Collections.emptyMap());

		assertFalse(resource.isLoaded(), "Check that saving an unloaded TResource does not mark it as loaded");
		assertFalse(resourceFile.exists(), "Check that saving an unloaded TResource does not produce a file");
		assertEquals(0, countResourceContents(resource), "Check that TResource is empty");

	}

	@Test
	void testLoadEmptyTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = createTResource(resourceFile);
		resource.getContents().clear();

		assertTrue(resource.isLoaded(), "Check a newly created TResource is loaded when its contents are cleared");
		assertFalse(resourceFile.exists(), "Check a newly created unsaved TResource does not produce a file when contents are cleared");
		assertEquals(0, countResourceContents(resource), "Check that TResource is empty");
	}

	@Test
	void testSaveLoadedEmptyTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = createTResource(resourceFile);
		resource.getContents().clear();
		resource.save(Collections.emptyMap());

		assertEquals(0, countResourceContents(resource), "Check that TResource is still empty after saving");
		assertTrue(resource.isLoaded(), "Check saved TResource is still loaded after saving");
		assertTrue(resourceFile.isDirectory(), "Check TResource has produced a directory on disk");
		assertArrayEquals(new String[] { "mvstore" }, resourceFile.list(), "Check TResource has produced a mvstore file on disk");
	}

	@Test
	void testCreateAndPopulateUnsavedTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = createTResource(resourceFile);
		List<PopulationInfo> populationInfo = populateResourceDepthFirst(resource);

		assertTrue(resource.isLoaded(), "Check a newly created TResource is loaded when contents are added");
		assertFalse(resourceFile.exists(), "Check a newly created unsaved TResource does not produce a file when contents are added");
		assertEquals(populationInfo.size(), countResourceContents(resource), "Check TResource contents are correct");
	}

	@Test
	void testUnloadPopulatedUnsavedTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = createTResource(resourceFile);
		populateResourceDepthFirst(resource);
		resource.unload();

		assertFalse(resource.isLoaded(), "Check the TResource is unloaded");
		assertFalse(resourceFile.exists(), "Check the unsaved TResource does not produce a file when unloaded");
		assertEquals(0, countResourceContents(resource), "Check TResource is empty");
	}

	@Test
	void testCreateTResourceSavingBeforePopulating() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = createTResource(resourceFile);
		resource.getContents().clear();
		resource.save(Collections.emptyMap());
		List<PopulationInfo> populationInfo = populateResourceDepthFirst(resource);

		assertTrue(resource.isLoaded(), "Check the TResource is loaded");
		assertTrue(resourceFile.exists(), "Check the TResource exists");
		assertEquals(populationInfo.size(), countResourceContents(resource), "Check TResource contents are correct when populating it after saving to disk");

		resource.unload();
	}

	@Test
	void testCreateTResourceSavingAfterPopulating() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = createTResource(resourceFile);
		List<PopulationInfo> populationInfo = populateResourceDepthFirst(resource);
		resource.save(Collections.emptyMap());

		assertTrue(resource.isLoaded(), "Check the TResource is loaded");
		assertTrue(resourceFile.exists(), "Check the TResource exists");
		assertEquals(populationInfo.size(), countResourceContents(resource), "Check TResource contents are correct when populating it after saving to disk");

		resource.unload();
	}

	@Test
	void testUnloadPopulatedSavedTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = createTResource(resourceFile);
		populateResourceDepthFirst(resource);
		resource.save(Collections.emptyMap());
		resource.unload();

		assertFalse(resource.isLoaded(), "Check the TResource is usloaded");
		assertTrue(resourceFile.exists(), "Check the TResource exists");
		assertEquals(0, countResourceContents(resource), "Check TResource is empty");
	}

	@Test
	void testReadTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = createTResource(resourceFile);
		List<PopulationInfo> populationInfo = populateResourceDepthFirst(resource);
		resource.save(Collections.emptyMap());
		resource.unload();

		TResource newResource = getTResource(resourceFile);

		// @formatter:off
		assertAll("Check TResource contents",
				() -> assertEquals(populationInfo.get(0).eltName, ((Node) newResource.getContents().get(0)).getName(), "Check name of last created element"),
				() -> assertEquals(populationInfo.size(), countResourceContents(newResource), "Check number of elements in TResource")
		);
		// @formatter:on

		newResource.unload();
	}

	@Test
	void testUnloadPreexistingTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = createTResource(resourceFile);
		populateResourceDepthFirst(resource);
		resource.save(Collections.emptyMap());
		resource.unload();

		TResource newResource = getTResource(resourceFile);
		newResource.unload();

		assertFalse(newResource.isLoaded(), "Check the newly loaded TResource is now unloaded");
		assertTrue(resourceFile.exists(), "Check the TResource still exists");
		assertEquals(0, countResourceContents(newResource), "Check newly loaded TResource is now empty");
	}

	@Test
	void testCountTResourceContents() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = createTResource(resourceFile);
		resource.save(Collections.emptyMap());
		List<PopulationInfo> populationInfo = populateResourceDepthFirst(resource);

		assertEquals(populationInfo.size(), countResourceContents(resource), "Check TResource has all the created elements");
	}

	@Test
	void testCountTResourceContentsAt() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = createTResource(resourceFile);
		resource.save(Collections.emptyMap());
		List<PopulationInfo> populationInfo = populateResourceRandomly(resource);

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
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = createTResource(resourceFile);
		resource.save(Collections.emptyMap());
		List<PopulationInfo> populationInfo = populateResourceRandomly(resource);
		
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
	
//	@Test
//	void testTResourceAt() throws Exception {
//		File resourceFile = TestUtils.createNonExistingTempFile();
//		TResource resource = createTResource(resourceFile);
//		resource.save(Collections.emptyMap());
//		List<PopulationInfo> populationInfo = populateResourceRandomly(resource);
//
//		printResourceContents(resource);
//		
//		// Pick a position and an instant
//		int position = (int) (populationInfo.size() * 0.75);
//		Instant instant = populationInfo.get(position).instant;
//		Logger.log(Logger.SEVERITY_INFO, "TResource had " + (position + 1) + " elements at " + instant);
//
//		AtomicInteger count = new AtomicInteger();
//
//		// Count using getAllContentsAt
//		count.set(0);
//		resource.at(instant).getAllContents().forEachRemaining((e) -> count.incrementAndGet());
//		assertEquals(position + 1, count.get(), "Check TResource has all the created elements using getAllContentsAt");
//
//		// Count (manually) using getContentsAt
//		count.set(0);
//		Stack<EObject> remaining = new Stack<>();
//		remaining.addAll(resource.at(instant).getContents());
//		do {
//			EObject elt = remaining.pop();
//			count.incrementAndGet();
//			remaining.addAll(((Node) elt).getChildren());
//		} while (!remaining.empty());
//		assertEquals(position + 1, count.get(), "Check TResource has all the created elements using getContentsAt");
//	}

	
	/**
	 * Utility method to print the resource contents in a formatted way
	 * 
	 * @param resource
	 */
	@SuppressWarnings("unused")
	private static void printResourceContents(Resource resource) {
		resource.getContents().forEach(e -> printNodeContents((Node) e, 0));
	}

	private static void printNodeContents(Node node, int depth) {
		IntStream.range(0, depth).forEach(i -> System.out.print("  "));
		System.out.println("Node [" + node.getName() + "]");
		node.getChildren().forEach(c -> printNodeContents(c, depth + 1));
	}

	/**
	 * Count the resource contents recursively using
	 * {@link Resource#getAllContents()}
	 * 
	 * @param resource
	 * @return
	 */
	private static int countResourceContents(Resource resource) {
		AtomicInteger count = new AtomicInteger();
		resource.getAllContents().forEachRemaining((e) -> count.incrementAndGet());
		return count.get();
	}

	/**
	 * Create a {@link TResource} using its own {@link ResourceSet}
	 * 
	 * @param resourceFile
	 * @return
	 */
	private static TResource createTResource(File resourceFile) {
		return (TResource) new ResourceSetImpl().createResource(TURI.createTMapURI(resourceFile));
	}

	/**
	 * Get and load a {@link TResource} using its own {@link ResourceSet}
	 * 
	 * @param resourceFile
	 * @return
	 */
	private static TResource getTResource(File resourceFile) {
		return (TResource) new ResourceSetImpl().getResource(TURI.createTMapURI(resourceFile), true);
	}
	
	/**
	 * Randomly populate a TResource returning when
	 * each element of the {@link TResource} was added. The root {@link Node}
	 * element is added to the {@link TResource} at the beginning, so that all the
	 * additions are done directly in the {@link TStore} of the {@link TResource}
	 * 
	 * @param resource
	 * @return
	 */
	private static List<PopulationInfo> populateResourceRandomly(TResource resource) {
		List<PopulationInfo> populationInfo = new ArrayList<>();
		Clock clock = resource.getClock();
		
		int count = 0;
		
		Node root = TestmodelFactory.eINSTANCE.createNode();
		root.setName(String.valueOf(count++));
		resource.getContents().add(root);
		populationInfo.add(new PopulationInfo(clock.instant(), root.getName()));
		List<Node> elements = new ArrayList<>();
		Node selection = root;
		Random random = new Random();
		do {
			Node child = TestmodelFactory.eINSTANCE.createNode();
			child.setName(String.valueOf(count++));
			selection.getChildren().add(child);
			populationInfo.add(new PopulationInfo(clock.instant(), child.getName()));
			elements.add(child);
			selection = elements.get(random.nextInt(elements.size()));
		} while (elements.size() < 50 || random.nextInt(50) != 0);
		
		for (int i = 1; i < populationInfo.size(); i++) {
			if (populationInfo.get(i - 1).instant.isAfter(populationInfo.get(i).instant)) {
				assertEquals(populationInfo.size(), countResourceContents(resource), "Check that PopulationInfo elements are ordered by creation date");
			}
		}
		
		return populationInfo;
	}

	/**
	 * Randomly populate a TResource using a depth first algorithm returning when
	 * each element of the {@link TResource} was added. The root {@link Node}
	 * element is added to the {@link TResource} at the beginning, so that all the
	 * additions are done directly in the {@link TStore} of the {@link TResource}
	 * 
	 * @param resource
	 * @return
	 */
	private static List<PopulationInfo> populateResourceDepthFirst(TResource resource) {
		List<PopulationInfo> populationInfo = new ArrayList<>();
		Clock clock = resource.getClock();

		Node root = TestmodelFactory.eINSTANCE.createNode();
		root.setName("ROOT");
		resource.getContents().add(root);
		populationInfo.add(new PopulationInfo(clock.instant(), root.getName()));
		do {
			populateNodeDepthFirst(clock, root, populationInfo, 3);
		} while (populationInfo.size() < 3);

		return populationInfo;
	}

	/**
	 * Randomly populate a {@link Node} up to <code>depth</code> levels of depth
	 * saving info on how when the children are being added
	 * 
	 * @param resource
	 * @return
	 */
	private static void populateNodeDepthFirst(Clock clock, Node node, List<PopulationInfo> populationInfo, int depth) {
		if (depth == 0) {
			return;
		}

		Random random = new Random();

		for (int i = 0; i < random.nextInt(10); i++) {
			Node child = TestmodelFactory.eINSTANCE.createNode();
			child.setName(UUID.randomUUID().toString());
			node.getChildren().add(child);
			populationInfo.add(new PopulationInfo(clock.instant(), child.getName()));
			populateNodeDepthFirst(clock, child, populationInfo, depth - 1);
		}
	}
}
