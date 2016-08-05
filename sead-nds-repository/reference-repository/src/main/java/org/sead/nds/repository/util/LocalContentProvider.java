package org.sead.nds.repository.util;

import java.io.InputStream;

import org.apache.commons.compress.parallel.InputStreamSupplier;

public abstract class LocalContentProvider {

	public abstract InputStream getInputStreamMatchingHash(String algorithm,
			String hash);

	public abstract boolean exists(String algorithm, final String hash);

	public InputStreamSupplier getSupplierFor(final String algorithm,
			final String hash) {
		//Only return a supplier if an appropriate stream source has been found
		if (exists(algorithm, hash)) {
			return new InputStreamSupplier() {
				public InputStream get() {
					InputStream is = getInputStreamMatchingHash(algorithm, hash);
					return is;
				}
			};
		}
		return null;
	}
}