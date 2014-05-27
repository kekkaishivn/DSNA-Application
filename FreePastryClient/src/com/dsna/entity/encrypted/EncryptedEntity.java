package com.dsna.entity.encrypted;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;

import com.dsna.entity.BaseEntity;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptedEntity extends BaseEntity {

	public static final String AESAlgorithm = "AES";
	public static final String DESAlgorithm = "DES";
	private static final long serialVersionUID = -1502144610375189829L;
	public static final int TYPE = 6;	
	private HashMap<String,String> keyEncapsulationFileIdsMap;
	SealedObject sealedObj;
	
	public EncryptedEntity(String ownerId, long timeStamp, BaseEntity entity, byte[] key, String algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, IOException	{
		super(ownerId, timeStamp);
		SecretKey k = new SecretKeySpec(key, algorithm);
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, k);
		sealedObj = new SealedObject(entity, cipher);
	}	
	
	@Override
	public boolean isHigherPriority(BaseEntity other) {
		return false;
	}

	@Override
	public int getType() {
		return TYPE;
	}
	
	public BaseEntity getEntity(byte[] key, String algorithm) throws InvalidKeyException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchProviderException, IOException	{
		SecretKey k = new SecretKeySpec(key, algorithm);
		return (BaseEntity) sealedObj.getObject(k, algorithm);
	}
	
	public void setFileId(String location, String id)	{
		keyEncapsulationFileIdsMap.put(location, id);
	}
	
	public String getGetId(String location)	{
		return keyEncapsulationFileIdsMap.get(location);
	}

}
