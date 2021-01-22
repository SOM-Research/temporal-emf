package edu.uoc.som.temf.tests.util;

import java.io.File;
import java.io.IOException;

public class TestUtils {
	
	public static File createNonExistingTempFile() throws IOException {
		File file = File.createTempFile("temf-", null);
		file.delete();
		file.deleteOnExit();
		return file;
	}
}
