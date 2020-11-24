package edu.uoc.som.temf.tests;

import java.io.File;
import java.io.IOException;

class TestUtils {
	
	static File createNonExistingTempFile() throws IOException {
		File file = File.createTempFile("temf-", null);
		file.delete();
		file.deleteOnExit();
		return file;
	}
}
