package org.sead.nds.repository.util;

public class FileUtils {

	/**
	 * Adapted from org/apache/commons/io/FileUtils.java change to SI - add 2
	 * digits of precision
	 */
	/**
	 * The number of bytes in a kilobyte.
	 */
	public static final long ONE_KB = 1000;

	/**
	 * The number of bytes in a megabyte.
	 */
	public static final long ONE_MB = ONE_KB * ONE_KB;

	/**
	 * The number of bytes in a gigabyte.
	 */
	public static final long ONE_GB = ONE_KB * ONE_MB;

	/**
	 * Returns a human-readable version of the file size, where the input
	 * represents a specific number of bytes.
	 *
	 * @param size
	 *            the number of bytes
	 * @return a human-readable display value (includes units)
	 */
	public static String byteCountToDisplaySize(long size) {
		String displaySize;

		if (size / ONE_GB > 0) {
			displaySize = String
					.valueOf(Math.round(size / (ONE_GB / 100.0d)) / 100.0)
					+ " GB";
		} else if (size / ONE_MB > 0) {
			displaySize = String
					.valueOf(Math.round(size / (ONE_MB / 100.0d)) / 100.0)
					+ " MB";
		} else if (size / ONE_KB > 0) {
			displaySize = String
					.valueOf(Math.round(size / (ONE_KB / 100.0d)) / 100.0)
					+ " KB";
		} else {
			displaySize = String.valueOf(size) + " bytes";
		}
		return displaySize;
	}
}
