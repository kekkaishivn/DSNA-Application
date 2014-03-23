package com.dsna.Entity;

import java.io.Serializable;

abstract public class BaseEntity implements Serializable {

	static final long serialVersionUID = 5098466384372731320L;	
	
	String timeStamp;
	String ownerId;
	String ownerDisplayName;
	String ownerUsername;
	
	protected BaseEntity(String ownerId, String timeStamp)	{
		this.ownerId = ownerId;
		this.timeStamp = timeStamp;
	}	
	
	abstract public boolean isHigherPriority(BaseEntity other);
	abstract public String getTypeName();

	public String getOwnerDisplayName() {
	    return this.ownerDisplayName;
	}

	public String getOwnerUsername() {
	    return this.ownerUsername;
	}

	public String getTimeStamp() {
	    return this.timeStamp;
	}

	public void setOwnerDisplayName(String ownerDisplayName) {
	    this.ownerDisplayName = ownerDisplayName;
	}
}
