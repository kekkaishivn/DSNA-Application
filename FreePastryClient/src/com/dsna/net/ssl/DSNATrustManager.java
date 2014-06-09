package com.dsna.net.ssl;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class DSNATrustManager implements X509TrustManager {

  private X509TrustManager defaultTrustManager;
  private X509TrustManager localTrustManager;

  private X509Certificate[] acceptedIssuers;

  public DSNATrustManager(KeyStore localKeyStore) { 
    // init defaultTrustManager using the system defaults
  	try {
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init((KeyStore)null);
			
			for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {  		  
		    if (trustManager instanceof X509TrustManager) {  
		    		defaultTrustManager = (X509TrustManager)trustManager;  
		    } 
			}
		  
			trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());		
	    
	    trustManagerFactory.init(localKeyStore); 
			
			for (TrustManager localTM : trustManagerFactory.getTrustManagers()) {
				if (localTM instanceof X509TrustManager) {  
		    	localTrustManager = (X509TrustManager)localTM;  
		    	System.out.println(localTrustManager);
		    }
			}
		}	catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  }

  public void checkServerTrusted(X509Certificate[] chain, String authType)
          throws CertificateException {
      try {
          defaultTrustManager.checkServerTrusted(chain, authType);
      } catch (Exception ce) {
      		localTrustManager.checkClientTrusted(chain, authType);
      }
  }

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {

	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		// TODO Auto-generated method stub
		return acceptedIssuers;
	}
  
  //...
}