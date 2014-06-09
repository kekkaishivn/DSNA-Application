package com.dsna.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import com.dsna.net.ssl.DSNATrustManager;

public class NetworkUtil {

	public static String getLocalAddress() throws SocketException	{
		Enumeration<NetworkInterface> e=NetworkInterface.getNetworkInterfaces();
        String myIp = "127.0.0.1";
		while(e.hasMoreElements())
        {
            NetworkInterface n=(NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while(ee.hasMoreElements())
            {
                InetAddress i= (InetAddress) ee.nextElement();
                if(!i.getHostAddress().contains(":")&&!i.getHostAddress().equalsIgnoreCase("127.0.0.1"))
                {
                	return i.getHostAddress();
                }
            }
        }
		return myIp;
	}
	
	public static HttpsURLConnection establishHttpsConnection(String url, KeyStore localTrustStore) throws NoSuchAlgorithmException, KeyManagementException, IOException	{
		DSNATrustManager myTrustManager = new DSNATrustManager(localTrustStore);
		TrustManager[] tms = new TrustManager[] { myTrustManager };
		SSLContext sslCtx = SSLContext.getInstance("TLS");
		sslCtx.init(null, tms, null);
		
		URL urlResource = new URL(url);
		HttpsURLConnection urlConnection = (HttpsURLConnection) urlResource 
        .openConnection();
		urlConnection.setSSLSocketFactory(sslCtx.getSocketFactory());
		return urlConnection;
	}

}
