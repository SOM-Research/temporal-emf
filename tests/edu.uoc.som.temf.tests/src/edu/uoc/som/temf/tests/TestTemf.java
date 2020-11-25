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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import edu.uoc.som.temf.TURI;
import edu.uoc.som.temf.core.TResource;
import edu.uoc.som.temf.estores.TStore;
import edu.uoc.som.temf.testmodel.Node;
import edu.uoc.som.temf.testmodel.TestmodelFactory;

class TestTemf {

	static class PopulationInfo {
		Instant instant;
		int count;
		String lastElt;

		private PopulationInfo(Instant instant, int count, String lastElt) {
			this.instant = instant;
			this.count = count;
			this.lastElt = lastElt;
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
		List<PopulationInfo> populationInfo = populateResource(resource);

		assertTrue(resource.isLoaded(), "Check a newly created TResource is loaded when contents are added");
		assertFalse(resourceFile.exists(), "Check a newly created unsaved TResource does not produce a file when contents are added");
		assertEquals(last(populationInfo).count, countResourceContents(resource), "Check TResource contents are correct");
	}

	@Test
	void testUnloadPopulatedUnsavedTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = createTResource(resourceFile);
		populateResource(resource);
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
		List<PopulationInfo> populationInfo = populateResource(resource);

		assertTrue(resource.isLoaded(), "Check the TResource is loaded");
		assertTrue(resourceFile.exists(), "Check the TResource exists");
		assertEquals(last(populationInfo).count, countResourceContents(resource), "Check TResource contents are correct when populating it after saving to disk");

		resource.unload();
	}

	@Test
	void testCreateTResourceSavingAfterPopulating() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = createTResource(resourceFile);
		List<PopulationInfo> populationInfo = populateResource(resource);
		resource.save(Collections.emptyMap());
		
		assertTrue(resource.isLoaded(), "Check the TResource is loaded");
		assertTrue(resourceFile.exists(), "Check the TResource exists");
		assertEquals(last(populationInfo).count, countResourceContents(resource), "Check TResource contents are correct when populating it after saving to disk");
		
		resource.unload();
	}

	@Test
	void testUnloadPopulatedSavedTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = createTResource(resourceFile);
		populateResource(resource);
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
		List<PopulationInfo> populationInfo = populateResource(resource);
		resource.save(Collections.emptyMap());
		resource.unload();

		TResource newResource = getTResource(resourceFile);

		// @formatter:off
		assertAll("Check TResource contents",
				() -> assertEquals(populationInfo.get(0).lastElt, ((Node) newResource.getContents().get(0)).getName(), "Check name of last created element"),
				() -> assertEquals(last(populationInfo).count, countResourceContents(newResource), "Check number of elements in TResource")
		);
		// @formatter:on

		newResource.unload();
	}

	@Test
	void testUnloadPreexistingTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = createTResource(resourceFile);
		populateResource(resource);
		resource.save(Collections.emptyMap());
		resource.unload();

		TResource newResource = getTResource(resourceFile);
		newResource.unload();

		assertFalse(newResource.isLoaded(), "Check the newly loaded TResource is now unloaded");
		assertTrue(resourceFile.exists(), "Check the TResource still exists");
		assertEquals(0, countResourceContents(newResource), "Check newly loaded TResource is now empty");


	}

	private static int countResourceContents(Resource resource) {
		AtomicInteger count = new AtomicInteger();
		resource.getAllContents().forEachRemaining((e) -> count.getAndIncrement());
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
	 * Randomly populate a TResource returning when each element of the
	 * {@link TResource} was added. The root {@link Node} element is added to the
	 * {@link TResource} at the beginning, so that all the additions are done
	 * directly in the {@link TStore} of the {@link TResource}
	 * 
	 * @param resource
	 * @return
	 */
	private static List<PopulationInfo> populateResource(TResource resource) {

		Node root = TestmodelFactory.eINSTANCE.createNode();
		List<PopulationInfo> result = populateNode(resource.getClock(), root);
		resource.getContents().add(root);

		return result;
	}

	/**
	 * Randomly populate a {@link Node} up to 3 levels of depth saving info on how
	 * when the children are being added
	 * 
	 * @param resource
	 * @return
	 */
	private static List<PopulationInfo> populateNode(Clock clock, Node root) {
		List<PopulationInfo> result = new ArrayList<>();
		Random random = new Random();

		root.setName("ROOT");
		result.add(new PopulationInfo(clock.instant(), 1, root.getName()));

		for (int i = 0; i < random.nextInt(10); i++) {
			Node nodeI = TestmodelFactory.eINSTANCE.createNode();
			root.getChildren().add(nodeI);
			nodeI.setName(UUID.randomUUID().toString());

			result.add(new PopulationInfo(clock.instant(), last(result).count + 1, nodeI.getName()));

			for (int j = 0; j < random.nextInt(10); j++) {
				Node nodeJ = TestmodelFactory.eINSTANCE.createNode();
				nodeI.getChildren().add(nodeJ);
				nodeJ.setName(UUID.randomUUID().toString());

				result.add(new PopulationInfo(clock.instant(), last(result).count + 1, nodeJ.getName()));

				for (int k = 0; k < random.nextInt(10); k++) {
					Node nodeK = TestmodelFactory.eINSTANCE.createNode();
					nodeI.getChildren().add(nodeK);
					nodeK.setName(UUID.randomUUID().toString());

					result.add(new PopulationInfo(clock.instant(), last(result).count + 1, nodeJ.getName()));
				}
			}
		}
		return result;
	}

	private static <T> T last(List<T> list) {
		return list.get(list.size() - 1);
	}
}
