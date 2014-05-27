package com.dsna.entity.encrypted;

import java.util.Arrays;

public class KeyInfo {
	public String location;
	public String fileId;
	public byte[] values;
	
	public KeyInfo(String location, String fileId, byte[] values)	{
		this.location = location;
		this.fileId = fileId;
		this.values = Arrays.copyOf(values, values.length);
	}
	
}
