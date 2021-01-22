package edu.uoc.som.temf.tests;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.jupiter.api.Test;

import edu.uoc.som.temf.tests.util.TestUtils;

class TestMVStore {

	static class MyKey implements Serializable {

		private static final long serialVersionUID = 1L;

		String id;
		String feature;

		private MyKey(String id, String feature) {
			this.id = id;
			this.feature = feature;
		}

		public static MyKey from(String id, String feature) {
			return new MyKey(id, feature);
		}
	}

	@Test
	void testCreateMVStore() throws IOException {
		File file = TestUtils.createNonExistingTempFile();
		createMVStore(file.getAbsolutePath());

		assertTrue(file.exists(), "Check MVStore exists on disk");
	}

	@Test
	void testReadMVStore() throws IOException {
		File file = TestUtils.createNonExistingTempFile();
		createMVStore(file.getAbsolutePath());

		MVStore store = MVStore.open(file.getAbsolutePath());
		MVMap<MyKey, String> map = store.openMap("MAP");

		// @formatter:off
		assertAll("Check MVStore contents are correct",
				() -> assertEquals("value 0", map.get(MyKey.from("id0", "feature0"))),
				() -> assertEquals("value 1", map.get(MyKey.from("id1", "feature0")))
		);
		// @formatter:on

		store.close();
	}

	private static void createMVStore(String path) {
		MVStore store = MVStore.open(path);
		MVMap<MyKey, String> map = store.openMap("MAP");
		map.put(MyKey.from("id0", "feature0"), "value 0");
		map.put(MyKey.from("id1", "feature0"), "value 1");
		store.commit();
		store.close();
	}
}
