package org.eclipse.ecf.tests.ssl;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.ecf.internal.ssl.ECFTrustManager;
import org.eclipse.ecf.internal.tests.ssl.TestsSSLActivator;
import org.eclipse.osgi.internal.service.security.KeyStoreTrustEngine;
import org.eclipse.osgi.service.security.TrustEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

@SuppressWarnings("restriction")
public class ECFTrustManagerTest {

	private ECFTrustManager trustManager = new ECFTrustManager();
	private ServiceRegistration<?> testTrustEngineService;

	// Index of certificate positions in the mock certificates array
	private static final int END_ENTITY = 0;
	private static final int INTERMEDIATE_CA_2 = 1;
	private static final int INTERMEDIATE_CA_1 = 2;
	private static final int ROOT_CA = 3;

	@Before
	public void setup() throws IOException {
		System.setProperty(ECFTrustManager.SORT_CERTS_EXPERIMENTAL_FLAG, Boolean.TRUE.toString());

		registerTestKeyStoreTrustEngine();
	}

	@After
	public void tearDown() {
		System.setProperty(ECFTrustManager.SORT_CERTS_EXPERIMENTAL_FLAG, Boolean.FALSE.toString());

		unregisterTestKeyStoreTrustEngine();
	}

	@Test
	public void chainAlreadySorted() throws Exception {
		X509Certificate[] certs = CertificateChainMock.createCertChain();

		checkServerTrusted(certs);
	}

	@Test
	public void chainAlreadySortedWithoutRootCertificate() throws Exception {
		X509Certificate[] certs = CertificateChainMock.createCertChain();

		certs = Arrays.copyOfRange(certs, 0, certs.length - 1);

		checkServerTrusted(certs);
	}

	@Test
	public void secondCertSharingCNWithIntermediateCA_BeforeProperOne() throws Exception {
		X509Certificate[] certs = CertificateChainMock.createCertChain();
		X509Certificate certBis = CertificateChainMock.createCert("intermediate-ca-2-bis.pem");

		certs = new X509Certificate[] { certs[END_ENTITY], certBis, certs[INTERMEDIATE_CA_2], certs[INTERMEDIATE_CA_1],
				certs[ROOT_CA] };

		checkServerTrusted(certs);
	}

	@Ignore("Current simple implementation doesn't handle this case")
	@Test
	public void secondCertSharingCNWithIntermediateCA_AfterProperOne() throws Exception {
		X509Certificate[] certs = CertificateChainMock.createCertChain();
		X509Certificate certBis = CertificateChainMock.createCert("intermediate-ca-2-bis.pem");

		certs = new X509Certificate[] { certs[END_ENTITY], certs[INTERMEDIATE_CA_2], certBis, certs[INTERMEDIATE_CA_1],
				certs[ROOT_CA] };

		checkServerTrusted(certs);
	}

	@Test
	public void extraneousCertUnrelatedToChain() throws Exception {
		X509Certificate extraneousCert = CertificateChainMock.createCert("intermediate-ca-3.pem");
		X509Certificate[] certs = CertificateChainMock.createCertChain();

		certs = new X509Certificate[] { certs[END_ENTITY], certs[INTERMEDIATE_CA_1], extraneousCert, certs[ROOT_CA],
				certs[INTERMEDIATE_CA_2] };

		checkServerTrusted(certs);
	}

	@Test
	public void certificatesPositionSwitched() throws Exception {
		X509Certificate[] certs = CertificateChainMock.createCertChain();

		X509Certificate certToSwitch = certs[INTERMEDIATE_CA_2];
		certs[INTERMEDIATE_CA_2] = certs[INTERMEDIATE_CA_1];
		certs[INTERMEDIATE_CA_1] = certToSwitch;

		checkServerTrusted(certs);
	}

	@Test
	public void certificatesNotSorted() throws Exception {
		X509Certificate[] certs = CertificateChainMock.createCertChain();

		certs = new X509Certificate[] { certs[END_ENTITY], certs[INTERMEDIATE_CA_1], certs[ROOT_CA],
				certs[INTERMEDIATE_CA_2] };

		checkServerTrusted(certs);
	}

	@Test
	public void positionPermutations() throws Exception {
		X509Certificate[] certs = CertificateChainMock.createCertChain();

		// First certificate is always the end-entity certificate
		List<List<X509Certificate>> permutations = CertificateChainMock.getPermutations(Arrays.asList(certs));

		for (List<X509Certificate> permutation : permutations) {
			X509Certificate[] certificates = permutation.toArray(new X509Certificate[] {});

			checkServerTrusted(certificates);
		}
	}

	private void checkServerTrusted(X509Certificate[] certs) {
		try {
			trustManager.checkServerTrusted(certs, "an_auth_type");
		} catch (Exception e) {
			e.printStackTrace();
			fail("should verify ok but got: " + e.getMessage() + " for original certificate list: \n"
					+ getDebugCertsInfo(certs));
		}
	}

	/**
	 * Registers a TrustEngine based on a test KeyStore bundled in this plugin to
	 * avoid having to add the Root CA test certificate used in tests to verify that
	 * they are valid.
	 */
	private void registerTestKeyStoreTrustEngine() throws IOException {
		TestsSSLActivator activator = TestsSSLActivator.getDefault();

		File keystore = activator.getTestEntryFile("test-keystore.jks");

		Dictionary<String, Object> trustEngineProps = new Hashtable<>(7);
		trustEngineProps.put(Constants.SERVICE_RANKING, Integer.valueOf(Integer.MAX_VALUE));

		KeyStoreTrustEngine systemTrustEngine = new KeyStoreTrustEngine(keystore.getAbsolutePath(), "JKS",
				"changeit".toCharArray(), "Test", null);

		testTrustEngineService = activator.getContext().registerService(TrustEngine.class.getName(), systemTrustEngine,
				trustEngineProps);
	}

	private void unregisterTestKeyStoreTrustEngine() {
		if (testTrustEngineService != null) {
			testTrustEngineService.unregister();
			testTrustEngineService = null;
		}
	}

	// Helper to print basic info of a list of certificates
	private String getDebugCertsInfo(X509Certificate[] certificates) {
		StringBuilder sb = new StringBuilder();

		for (X509Certificate cert : certificates) {
			sb.append("Subject = ").append(cert.getSubjectDN().getName()).append("\n");
			sb.append(" Issuer = ").append(cert.getIssuerDN().getName()).append("\n");
			sb.append("\n");
		}

		return sb.toString();
	}

}
