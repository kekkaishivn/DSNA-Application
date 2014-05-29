package com.dsna.entity.encrypted;

import java.util.Arrays;

import com.dsna.entity.BaseEntity;

public class KeyInfo extends BaseEntity {

	private static final long serialVersionUID = -4324693496844521885L;
	public static final int TYPE = 7;	
	private String keyId;
	public byte[] values;
	
	public KeyInfo(String ownerUsername, long timeStamp, String keyId, byte[] values)	{
		super(ownerUsername, timeStamp);
		this.ownerUsername = ownerUsername;
		this.keyId = keyId;
		this.values = Arrays.copyOf(values, 16);
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
	
}
