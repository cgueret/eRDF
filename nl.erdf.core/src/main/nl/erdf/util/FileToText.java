package nl.erdf.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileToText {
	/**
	 * Loads a file into a string and returns it
	 * 
	 * @param file
	 * @return
	 */
	public static String convert(File file) {
		byte[] buffer = new byte[(int) file.length()];
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(file));
			f.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		} finally {
			if (f != null)
				try {
					f.close();
				} catch (IOException ignored) {
				}
		}
		return new String(buffer);
	}
}
