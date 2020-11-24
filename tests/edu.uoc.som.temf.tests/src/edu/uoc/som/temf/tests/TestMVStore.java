package edu.uoc.som.temf.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.jupiter.api.Test;

class TestMVStore {

	static class MyKey implements Serializable {

		private static final long serialVersionUID = 1L;

		Instant instant;
		String id;
		String feature;

		private MyKey(Instant instant, String id, String feature) {
			this.instant = instant;
			this.id = id;
			this.feature = feature;
		}

		public static MyKey from(Instant instant, String id, String feature) {
			return new MyKey(instant, id, feature);
		}
	}

	@Test
	void testCreateMVStore() throws IOException {
		File file = TestUtils.createNonExistingTempFile();
		MVStore store = MVStore.open(file.getAbsolutePath());
		MVMap<MyKey, String> map = store.openMap("MAP");
		
		Instant now = Instant.now();
		map.put(MyKey.from(now, "id0", "feature0"), "value 0");
		map.put(MyKey.from(now, "id1", "feature0"), "value 1");
		
		store.commit();
		store.close();
		
		assertTrue(file.exists());
	}

	@Test
	void testReadMVStore() throws IOException {
		File file = TestUtils.createNonExistingTempFile();
		MVStore store = MVStore.open(file.getAbsolutePath());
		MVMap<MyKey, String> map = store.openMap("MAP");
		
		Instant now = Instant.now();
		map.put(MyKey.from(now, "id0", "feature0"), "value 0");
		map.put(MyKey.from(now, "id1", "feature0"), "value 1");
		
		store.commit();
		store.close();

		store = MVStore.open(file.getAbsolutePath());
		map = store.openMap("MAP");
		assertEquals("value 0", map.get(MyKey.from(now, "id0", "feature0")));
		assertEquals("value 1", map.get(MyKey.from(now, "id1", "feature0")));
		
		store.close();
	}

}
