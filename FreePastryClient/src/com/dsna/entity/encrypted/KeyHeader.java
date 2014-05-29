package com.dsna.entity.encrypted;

import java.util.Arrays;

import com.dsna.entity.BaseEntity;

public class KeyHeader extends BaseEntity {
	
	public static final int TYPE = 8;	
	public byte[] header;
	private String keyId;
	private String[] ids;
	
	public KeyHeader(String ownerUsername, long timeStamp, byte[] headers, String[] ids, String keyId)	{
		super(ownerUsername, timeStamp);
		this.ownerUsername = ownerUsername;
		this.header = Arrays.copyOf(headers, headers.length);
		this.ids = ids;
		this.keyId = keyId;
	}

	@Override
	public boolean isHigherPriority(BaseEntity other) {
		return false;
	}

	@Override
	public int getType() {
		return 8;
	}
	
	public String[] getIds()	{
		return ids;
	}
	
	public String getKeyId()	{
		return keyId;
	}

}
