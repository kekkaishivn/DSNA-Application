package com.dsna.util;

import org.bouncycastle.crypto.digests.SHA1Digest;

public class HashUtil {
	
	  public static byte[] doSHA1Hash(String s)	{
	    byte[] bytes = s.getBytes();
	    SHA1Digest digest = new SHA1Digest();
	    digest.update(bytes, 0, bytes.length);
	    byte[] result = new byte[digest.getByteLength()];
	    digest.doFinal(result, 0);
	    return result;
	  }  

}
