package org.eclipse.ecf.internal.ssl;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Simple implementation to sort a list of certificates and get a certificate
 * chain. Some servers send the certificates unordered. Until TLSv1.2 this was
 * not allowed. TLSv1.3 allows it (see RFC 8446 section 4.4.2).
 *
 * This is not at all fully complaint with the RFC 8446 as it does not
 * (currently) handles some cases like multiple chain paths or certificates with
 * the same CN (Common Name) that are in a specific order (see
 * {@link org.eclipse.ecf.tests.ssl.ECFTrustManagerTest#secondCertSharingCNWithIntermediateCA_AfterProperOne()}
 */
public class CertificateChainSorter {

	/**
	 * Sorts the certificates to obtain the certificate chain in the proper
	 * order. Certificates that are not part of the chain are not considered
	 * (i.e. not included in the result).
	 *
	 * @param certificates The original list of certificates in the order
	 * provided by the server.
	 *
	 * @return a new X509Certificate array with the certificate chain in proper
	 * order. If no certificates are provided or just one, then it returns the
	 * same array provided.
	 */
	public static X509Certificate[] sortCertificates(X509Certificate[] certificates) {
		if (certificates == null || certificates.length <= 1) {
			return certificates;
		}

		Map<Principal, X509Certificate> subjects = createIndexBySubject(certificates);

		List<X509Certificate> sortedChain = buildSortedChain(certificates[0], subjects);

		return sortedChain.toArray(new X509Certificate[] {});
	}

	/**
	 * Builds an index from the provided certificates using the Subject DN as
	 * the key.
	 *
	 * @param certificates The original certificate list provided by the server.
	 *
	 * @return a new map where the Subject DN is the key and the certificate the
	 * value.
	 */
	private static Map<Principal, X509Certificate> createIndexBySubject(X509Certificate[] certificates) {
		Map<Principal, X509Certificate> subjects = new HashMap<Principal, X509Certificate>();

		for (int i = 0; i < certificates.length; i++) {
			X509Certificate currentCert = certificates[i];

			subjects.put(currentCert.getSubjectDN(), currentCert);
		}

		return subjects;
	}

	/**
	 * Builds a certificate chain from the ones provided by the server starting
	 * with the first certificate from the original list (which should be the
	 * end-entity certificate per RFC 8446 section 4.4.2).
	 *
	 * @param endEntityCertificate The end-entity certificate which MUST be the
	 * first one of the provided by the server.
	 * @param subjects An SubjectDN based index of the certificates provided by
	 * the server.
	 *
	 * @return A new list with the certificate chain calculated.
	 */
	private static List<X509Certificate> buildSortedChain(X509Certificate endEntityCertificate,
			Map<Principal, X509Certificate> subjects) {
		List<X509Certificate> sortedChain = new ArrayList<X509Certificate>();

		X509Certificate currentCert = endEntityCertificate;
		X509Certificate issuer;

		sortedChain.add(currentCert);

		do {
			issuer = subjects.get(currentCert.getIssuerDN());

			if (issuer != null) {
				sortedChain.add(issuer);
				currentCert = issuer;
			}
		} while (issuer != null && !isRootCertificate(issuer));

		return sortedChain;
	}

	private static boolean isRootCertificate(X509Certificate certificate) {
		return certificate != null && certificate.getSubjectDN().equals(certificate.getIssuerDN());
	}

}