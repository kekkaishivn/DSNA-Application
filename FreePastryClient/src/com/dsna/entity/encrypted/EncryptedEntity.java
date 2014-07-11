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

	private static final long serialVersionUID = -1502144610375189829L;
	public static final int TYPE = 6;	
	private String keyId;
	private HashMap<String,String> keyEncapsulationFileIdsMap;
	SealedObject sealedObj;
	
	public EncryptedEntity(String ownerId, long timeStamp, BaseEntity entity, String keyId, Cipher cipher) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, IOException	{
		super(ownerId, timeStamp);
		this.keyId = keyId;
		keyEncapsulationFileIdsMap = new HashMap<String,String>();
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
	
	public void setKeyHeaderFileId(String cloud, String id)	{
		keyEncapsulationFileIdsMap.put(cloud, id);
	}
	
	public String getKeyHeaderFileId(String cloud)	{
		return keyEncapsulationFileIdsMap.get(cloud);
	}
	
	public Set<String> getLocationSet()	{
		return keyEncapsulationFileIdsMap.keySet();
	}
	
	public String getKeyId()	{
		return keyId;
	}

}
