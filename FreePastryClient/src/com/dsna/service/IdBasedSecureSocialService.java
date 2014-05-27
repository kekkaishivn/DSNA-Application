package com.dsna.service;

import java.io.IOException;

import org.bouncycastle.crypto.CipherParameters;

public interface IdBasedSecureSocialService extends SocialService {

	public byte[] changeSessionKey(CipherParameters publicKey, String[] ids) throws IOException;
	
	
}
