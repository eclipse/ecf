package org.eclipse.ecf.tests.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ecf.internal.tests.ssl.TestsSSLActivator;

/**
 * Helper test class to handle Certificates used for tests.
 */
public class CertificateChainMock {

	/**
	 * Reads a files that contains a Certificate and creates and instance of
	 * {@link X509Certificate}.
	 *
	 * @param certFileName The name of the test certificate bundled in this plugin.
	 *
	 * @return A model of the certificate read from the supplied file name.
	 *
	 * @throws IllegalArgumentException If the file cannot be read.
	 * @throws CertificateException     If the file cannot be parsed as a valid
	 *                                  X.509 certificate.
	 */
	public static X509Certificate createCert(String certFileName) throws CertificateException {
		// Convert file to InputStream
		InputStream certInputStream;
		try {
			File entryFile = TestsSSLActivator.getDefault().getTestEntryFile(certFileName);
			certInputStream = new FileInputStream(entryFile);
		} catch (IOException e) {
			throw new IllegalArgumentException("Certificate file couldn't be found: " + certFileName, e);
		}

		// Create CertificateFactory
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

		// Parse certificate
		X509Certificate cert = (X509Certificate) certFactory.generateCertificate(certInputStream);

		return cert;
	}

	/**
	 * Creates a valid certificate chain starting with the end-entity certificate,
	 * followed by 2 intermediate CA certificates and ends with the Root CA
	 * certificate (the one that must be considered as Trusted Anchor by the
	 * underlying TrustEngine.
	 *
	 * @return An array of valid certificate chain (well sorted).
	 *
	 * @throws CertificateException In case that there is some error reading the
	 *                              certificate files.
	 */
	public static X509Certificate[] createCertChain() throws CertificateException {
		// Create certificate chain array
		X509Certificate[] certificateChain = new X509Certificate[4];
		certificateChain[0] = createCert("server.pem");
		certificateChain[1] = createCert("intermediate-ca-2.pem");
		certificateChain[2] = createCert("intermediate-ca-1.pem");
		certificateChain[3] = createCert("root-ca.pem");

		return certificateChain;
	}

	/**
	 * Gets all the possible permutations of the given list of certificates. All the
	 * results will have the end-entity certificate as first item (per RFC 8446
	 * request).
	 *
	 * @param certificates A list of certificates. First item MUST be the end-entity
	 *                     certificate.
	 *
	 * @return A list of all the possible position permutations were the end-entity
	 *         certificate is always in the first position.
	 */
	public static List<List<X509Certificate>> getPermutations(List<X509Certificate> certificates) {
		List<List<X509Certificate>> result = getPermutationsInternal(certificates);

		return filterPermutations(result, certificates.get(0));
	}

	private static List<List<X509Certificate>> getPermutationsInternal(List<X509Certificate> certificates) {
		if (certificates.size() == 0) {
			List<List<X509Certificate>> emptyResult = new ArrayList<>();
			emptyResult.add(new ArrayList<>());
			return emptyResult;
		}

		X509Certificate endEntityCertificate = certificates.get(0);

		List<List<X509Certificate>> permutationsWithoutFirst = getPermutationsInternal(
				certificates.subList(1, certificates.size()));

		List<List<X509Certificate>> result = new ArrayList<>();
		for (List<X509Certificate> permutation : permutationsWithoutFirst) {
			for (int i = 0; i <= permutation.size(); i++) {
				List<X509Certificate> newPermutation = new ArrayList<>(permutation);
				newPermutation.add(i, endEntityCertificate);
				result.add(newPermutation);
			}
		}

		return result;
	}

	private static List<List<X509Certificate>> filterPermutations(List<List<X509Certificate>> permutations,
			X509Certificate firstItem) {
		List<List<X509Certificate>> filteredPermutations = new ArrayList<>();
		for (List<X509Certificate> permutation : permutations) {
			if (permutation.get(0) == firstItem) {
				filteredPermutations.add(permutation);
			}
		}
		return filteredPermutations;
	}

}
