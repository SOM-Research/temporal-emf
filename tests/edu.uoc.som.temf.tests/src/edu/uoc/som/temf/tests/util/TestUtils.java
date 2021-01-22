package edu.uoc.som.temf.tests.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import edu.uoc.som.temf.TURI;
import edu.uoc.som.temf.core.TResource;
import edu.uoc.som.temf.testmodel.Node;
import edu.uoc.som.temf.testmodel.TestmodelFactory;
import edu.uoc.som.temf.tstores.TStore;

public class TestUtils {

	public static class PopulationInfo {
		public final Instant instant;
		public final String eltName;

		public PopulationInfo(Instant instant, String lastElt) {
			this.instant = instant;
			this.eltName = lastElt;
		}
	}

	/**
	 * Create a non-existing temporary file
	 * 
	 * @return
	 * @throws IOException
	 */
	public static File createNonExistingTempFile() throws IOException {
		File file = File.createTempFile("temf-", null);
		file.delete();
		file.deleteOnExit();
		return file;
	}

	/**
	 * Utility method to print the resource contents in a formatted way
	 * 
	 * @param resource
	 */
	public static void printResourceContents(Resource resource) {
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
	public static int countResourceContents(Resource resource) {
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
	public static TResource createTResource(File resourceFile) {
		return (TResource) new ResourceSetImpl().createResource(TURI.createTMapURI(resourceFile));
	}

	/**
	 * Get and load a {@link TResource} using its own {@link ResourceSet}
	 * 
	 * @param resourceFile
	 * @return
	 */
	public static TResource getTResource(File resourceFile) {
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
	public static List<PopulationInfo> populateResourceRandomly(TResource resource) {
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
	public static List<PopulationInfo> populateResourceDepthFirst(TResource resource) {
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
	public static void populateNodeDepthFirst(Clock clock, Node node, List<PopulationInfo> populationInfo, int depth) {
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
