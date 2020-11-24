package edu.uoc.som.temf.tests;

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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
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
	void testCreateTResource() {
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.createResource(TURI.createTMapURI(new File(UUID.randomUUID().toString())));

		assertTrue(resource instanceof TResource);
	}

	/**
	 * Test that creating and populating a {@link TResource} without saving it does
	 * not produce a file
	 * 
	 * @throws Exception
	 */
	@Test
	void testCreateTResourceWithoutSaving() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();

		assertFalse(resourceFile.exists());

		TResource resource = createTResource(resourceFile);

		assertFalse(resource.isLoaded());

		List<PopulationInfo> populationInfo = populateResource(resource);

		assertTrue(resource.isLoaded());
		assertFalse(resourceFile.exists());
		assertEquals(1, resource.getContents().size());
		assertEquals(last(populationInfo).count, countResourceContents(resource));

		resource.unload();

		assertEquals(0, countResourceContents(resource));
		assertFalse(resource.isLoaded());
	}

	/**
	 * Test that loading an empty {@link Resource} does not produce a file
	 * 
	 * @throws Exception
	 */
	@Test
	void testLoadEmptyTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();

		assertFalse(resourceFile.exists());

		TResource resource = createTResource(resourceFile);

		assertFalse(resource.isLoaded());

		resource.getContents().clear();

		assertTrue(resource.isLoaded());
		assertFalse(resourceFile.exists());

		assertEquals(0, countResourceContents(resource));

		resource.unload();

		assertEquals(0, countResourceContents(resource));
		assertFalse(resource.isLoaded());
		assertFalse(resourceFile.exists());
	}

	/**
	 * Test that saving an unloaded resource does not produce a file
	 * 
	 * @throws Exception
	 */
	@Test
	void testSaveUnloadedTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();

		assertFalse(resourceFile.exists());

		TResource resource = createTResource(resourceFile);

		assertFalse(resource.isLoaded());
		assertFalse(resourceFile.exists());

		resource.save(Collections.emptyMap());

		assertFalse(resource.isLoaded());
		assertFalse(resourceFile.exists());
		assertEquals(0, countResourceContents(resource));

		resource.unload();

		assertEquals(0, countResourceContents(resource));
		assertFalse(resource.isLoaded());
		assertFalse(resourceFile.exists());
	}

	/**
	 * Test that saving an empty but loaded resource produces a file
	 * 
	 * @throws Exception
	 */
	@Test
	void testSaveEmptyLoadedTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();

		assertFalse(resourceFile.exists());

		TResource resource = createTResource(resourceFile);

		assertFalse(resource.isLoaded());

		resource.getContents().clear();

		assertTrue(resource.isLoaded());
		assertFalse(resourceFile.exists());

		resource.save(Collections.emptyMap());

		assertTrue(resource.isLoaded());
		assertTrue(resourceFile.exists());
		assertArrayEquals(new String[] { "mvstore" }, resourceFile.list());
		assertEquals(0, countResourceContents(resource));

		resource.unload();

		assertEquals(0, countResourceContents(resource));
		assertFalse(resource.isLoaded());
		assertTrue(resourceFile.exists());
		assertArrayEquals(new String[] { "mvstore" }, resourceFile.list());
	}

	/**
	 * Test to create a {@link TResource} populating it before saving it to disk
	 * (this will create an in-memery, off-heap {@link TResource})
	 * 
	 * @throws Exception
	 */
	@Test
	void testCreateTResourceSavingBeforePopulating() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();

		assertFalse(resourceFile.exists());

		TResource resource = createTResource(resourceFile);

		assertFalse(resource.isLoaded());

		resource.save(Collections.emptyMap());

		assertFalse(resource.isLoaded());
		assertFalse(resourceFile.exists());

		List<PopulationInfo> populationInfo = populateResource(resource);

		assertEquals(last(populationInfo).count, countResourceContents(resource));

		resource.unload();

		assertEquals(0, countResourceContents(resource));
		assertFalse(resource.isLoaded());
	}

	/**
	 * Test to create a {@link TResource} populating it after saving it to disk
	 * (this will create an on-disk {@link TResource})
	 * 
	 * @throws Exception
	 */
	@Test
	void testCreateTResourceSavingAfterPopulating() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();

		assertFalse(resourceFile.exists());

		TResource resource = createTResource(resourceFile);

		assertFalse(resource.isLoaded());

		List<PopulationInfo> populationInfo = populateResource(resource);

		assertEquals(last(populationInfo).count, countResourceContents(resource));

		assertTrue(resource.isLoaded());
		assertFalse(resourceFile.exists());

		resource.save(Collections.emptyMap());

		assertTrue(resource.isLoaded());
		assertTrue(resourceFile.exists());
		assertArrayEquals(new String[] { "mvstore" }, resourceFile.list());

		assertEquals(last(populationInfo).count, countResourceContents(resource));

		resource.unload();

		assertEquals(0, countResourceContents(resource));
		assertFalse(resource.isLoaded());
		assertTrue(resourceFile.exists());
		assertArrayEquals(new String[] { "mvstore" }, resourceFile.list());
	}

	/**
	 * Test to read a saved {@link TResource} after creating and saving it
	 * @throws Exception
	 */
	@Test
	void testReadTResource() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();
		TResource resource = createTResource(resourceFile);
		List<PopulationInfo> populationInfo = populateResource(resource);
		resource.save(Collections.emptyMap());
		resource.unload();

		resource = getTResource(resourceFile);
		
		assertEquals(populationInfo.get(0).lastElt, ((Node) resource.getContents().get(0)).getName());
		assertEquals(last(populationInfo).count, countResourceContents(resource));
		
		resource.unload();
		
		assertEquals(0, countResourceContents(resource));
	}

	/**
	 * This test is kept as a reference on what a standard EMF {@link Resource} must
	 * behave
	 * 
	 * @throws Exception
	 */
	@Test
	void testCreateXmiResourceSavingBeforePopulating() throws Exception {
		File resourceFile = TestUtils.createNonExistingTempFile();

		assertFalse(resourceFile.exists());

		Resource resource = new XMIResourceImpl(URI.createFileURI(resourceFile.getAbsolutePath()));

		assertFalse(resource.isLoaded());

		resource.save(Collections.emptyMap());

		assertFalse(resource.isLoaded());
		assertTrue(resourceFile.exists());

		resource.getContents().add(EcoreFactory.eINSTANCE.createEObject());

		assertEquals(1, countResourceContents(resource));

		resource.unload();

		assertEquals(0, countResourceContents(resource));
		assertFalse(resource.isLoaded());
	}

//		// Load model using a different ResourceSet
//		resource = getTResource(resourceFile);
//		assertEquals(1, resource.getContents().size());
//		assertTrue(resource.getContents().get(0) instanceof Node);
//		assertEquals("ROOT", ((Node) resource.getContents().get(0)).getName());
//		resource.unload();
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
