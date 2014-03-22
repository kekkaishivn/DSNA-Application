package com.dsna.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

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

}
