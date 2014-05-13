package com.dsna.entity;

import java.io.Serializable;

abstract public class BaseEntity implements Serializable {

	static final long serialVersionUID = 5098466384372731320L;
	
	long timeStamp;
	String ownerId;
	String ownerDisplayName;
	String ownerUsername;
	
	protected BaseEntity(String ownerId, long timeStamp)	{
		this.ownerId = ownerId;
		this.timeStamp = timeStamp;
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

	public void setOwnerDisplayName(String ownerDisplayName) {
	    this.ownerDisplayName = ownerDisplayName;
	}
}
