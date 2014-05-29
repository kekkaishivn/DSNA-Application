package com.dsna.entity;

import java.io.Serializable;

abstract public class BaseEntity implements Serializable {

	static final long serialVersionUID = 5098466384372731320L;
	
	long timeStamp;
	protected String ownerId;
	String ownerDisplayName;
	protected String ownerUsername;
	boolean preferEncrypted;
	boolean isPrivateMessage;
	
	protected BaseEntity(String ownerId, long timeStamp)	{
		this.ownerId = ownerId;
		this.timeStamp = timeStamp;
		preferEncrypted = false;
		isPrivateMessage = false;
	}	
	
	abstract public boolean isHigherPriority(BaseEntity other);
	abstract public int getType();

	public String getOwnerDisplayName() {
	    return this.ownerDisplayName;
	}

	public String getOwnerUsername() {
	    return this.ownerUsername;
	}

	public long getTimeStamp() {
	    return this.timeStamp;
	}

	public void setOwnerUsername(String ownerUsername) {
		this.ownerUsername = ownerUsername;
	}
	
	public boolean getPreferEncrypted()	{
		return preferEncrypted;
	}
	
	public boolean isPrivateMessage()	{
		return isPrivateMessage;
	}
	
	public void setPreferEncrypted(boolean preferEncrypted)	{
		this.preferEncrypted = preferEncrypted;
	}
	
	public void setIsPrivateMessage(boolean isPrivateMessage)	{
		this.isPrivateMessage = isPrivateMessage;
	}
}
