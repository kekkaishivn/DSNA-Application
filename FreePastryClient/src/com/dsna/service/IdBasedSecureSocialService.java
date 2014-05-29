package com.dsna.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;

import rice.Continuation;

import com.dsna.entity.encrypted.KeyInfo;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

public interface IdBasedSecureSocialService extends SocialService {

	public void distributeSessionKey(CipherParameters publicKey, String[] ids, Continuation<KeyInfo, Exception> action);
	public void distributeSessionKey(CipherParameters publicKey, Continuation<KeyInfo, Exception> action);
	public void postStatus(String status, String keyId, byte[] key, String algorithm) throws UserRecoverableAuthIOException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException;
	public void postStatus(String id, String status, String keyId, byte[] key, String algorithm) throws UserRecoverableAuthIOException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException;		
	
}
