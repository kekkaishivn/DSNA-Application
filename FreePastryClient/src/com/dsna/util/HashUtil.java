package com.dsna.util;

import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.GeneralDigest;

public class HashUtil {
	
	  public static byte[] doSHA1Hash(String s)	{
	    return doHash(s, new SHA1Digest());
	  } 
	  
	  public static byte[] doSHA256Hash(String s)	{
	    return doHash(s, new SHA256Digest());
	  } 
	  
	  private static byte[] doHash(String s, GeneralDigest digest)	{
	  	byte[] bytes = s.getBytes();
	  	digest.update(bytes, 0, bytes.length);
	    byte[] result = new byte[digest.getByteLength()];
	    digest.doFinal(result, 0);
	    return result;
	  }

}
