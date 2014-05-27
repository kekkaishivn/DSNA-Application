package com.dsna.service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.crypto.CipherParameters;

import com.dsna.entity.encrypted.KeyInfo;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

public interface IdBasedSecureSocialService extends SocialService {

	public KeyInfo distributeSessionKey(CipherParameters publicKey, String[] ids) throws IOException;
	public KeyInfo distributeSessionKey(CipherParameters publicKey) throws IOException;
	public void postStatus(String status, byte[] key, String algorithm) throws UserRecoverableAuthIOException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException;
	public void postStatus(String id, String status, byte[] key, String algorithm) throws UserRecoverableAuthIOException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException;		
	
}
