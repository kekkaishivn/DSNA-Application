package com.dsna.entity.encrypted;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Set;

import com.dsna.entity.BaseEntity;

import javax.crypto.BadPaddingException;
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
	private String keyId;
	private HashMap<String,String> keyEncapsulationFileIdsMap;
	SealedObject sealedObj;
	
	public EncryptedEntity(String ownerId, long timeStamp, BaseEntity entity, String keyId, byte[] key, String algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, IOException	{
		super(ownerId, timeStamp);
		this.keyId = keyId;
		keyEncapsulationFileIdsMap = new HashMap<String,String>();
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
	
	public BaseEntity getEntity(byte[] key, String algorithm) throws InvalidKeyException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchProviderException, IOException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException	{
		SecretKey k = new SecretKeySpec(key, algorithm);
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, k);
		return (BaseEntity) sealedObj.getObject(cipher);
	}
	
	public void setFileId(String location, String id)	{
		keyEncapsulationFileIdsMap.put(location, id);
	}
	
	public String getFileId(String location)	{
		return keyEncapsulationFileIdsMap.get(location);
	}
	
	public Set<String> getLocationSet()	{
		return keyEncapsulationFileIdsMap.keySet();
	}
	
	public String getKeyId()	{
		return keyId;
	}

}
