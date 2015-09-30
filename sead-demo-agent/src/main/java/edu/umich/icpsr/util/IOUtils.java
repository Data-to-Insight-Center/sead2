package edu.umich.icpsr.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {
	private static final int BLOCK_SZ_MB = 1024 * 10000;

	public static void streamOut(InputStream is, OutputStream os) throws IOException {
		BufferedInputStream bfis = new BufferedInputStream(is);
		BufferedOutputStream bfos = new BufferedOutputStream(os);
		byte[] b = new byte[BLOCK_SZ_MB];
		int read = 0;
		while ((read = bfis.read(b, 0, BLOCK_SZ_MB)) > -1) {
			os.write(b, 0, read);
		}
		bfos.flush();
		bfos.close();
		bfis.close();
	}

}
