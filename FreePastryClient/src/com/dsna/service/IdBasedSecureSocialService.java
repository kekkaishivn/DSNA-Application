package com.dsna.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;

import rice.Continuation;

import com.dsna.entity.encrypted.KeyInfo;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

public interface IdBasedSecureSocialService extends SocialService {

	public static final String AESAlgorithm = "AES";
	public static final String DESAlgorithm = "DES";	
	public void changeAndDistributeSessionKey(String symmetricAlgorithm, CipherParameters publicKey, String[] ids, Continuation<KeyInfo, Exception> action);
	public void changeAndDistributeSessionKey(String symmetricAlgorithm, CipherParameters publicKey, Continuation<KeyInfo, Exception> action);
	public void setSessionKeyParameter(KeyInfo key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException;
	public void setPreferEncrypted(boolean preferEncrypted);
}
