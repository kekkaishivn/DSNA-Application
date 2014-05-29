package com.dsna.entity.encrypted;

import java.util.Arrays;

import com.dsna.entity.BaseEntity;

public class KeyInfo extends BaseEntity {

	private static final long serialVersionUID = -4324693496844521885L;
	public static final int TYPE = 7;	
	private String keyId;
	private byte[] values;
	private String algorithm;
	
	public KeyInfo(String ownerUsername, long timeStamp, String keyId, byte[] values, String algorithm)	{
		super(ownerUsername, timeStamp);
		this.ownerUsername = ownerUsername;
		this.keyId = keyId;
		this.values = Arrays.copyOf(values, 16);
		this.algorithm = algorithm;
	}

	@Override
	public boolean isHigherPriority(BaseEntity other) {
		return false;
	}

	@Override
	public int getType() {
		return 7;
	}
	
	public String getKeyId()	{
		return keyId;
	}
	
	public String getAlgorithm()	{
		return algorithm;
	}
	
	public byte[] getValues()	{
		return values;
	}
	
}
