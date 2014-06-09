package com.dsna.net.ssl;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.dsna.util.FileUtil;

public class TrustStoreGenerator {

	public static void main(String[] args) throws CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException, KeyManagementException {
		// TODO Auto-generated method stub
		KeyStore localTrustStore = KeyStore.getInstance("BKS");
		CertificateFactory cf = CertificateFactory.getInstance("X509");
		
		InputStream is = new BufferedInputStream(new FileInputStream("dsnaserver.cer"));
		X509Certificate cert = (X509Certificate) cf.generateCertificate(is);

		localTrustStore.setCertificateEntry("dsnamblab", cert);
		localTrustStore.store(new FileOutputStream("androidtruststore.bks"), "kthdsna".toCharArray());

		DSNATrustManager myTrustManager = new DSNATrustManager(localTrustStore);
		TrustManager[] tms = new TrustManager[] { myTrustManager };
		SSLContext sslCtx = SSLContext.getInstance("TLS");
		sslCtx.init(null, tms, null);
		
		URL url = new URL("https://130.237.20.200:8080/DSNA_privatekeygenerator/SystemPublic.txt");
		HttpsURLConnection urlConnection = (HttpsURLConnection) url 
        .openConnection();
		urlConnection.setSSLSocketFactory(sslCtx.getSocketFactory());
		System.out.println(FileUtil.readString(urlConnection.getInputStream()));
		urlConnection.disconnect();

	}

}
