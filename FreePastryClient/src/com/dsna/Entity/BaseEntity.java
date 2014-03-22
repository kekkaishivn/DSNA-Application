package com.dsna.Entity;

import java.io.Serializable;

abstract public class BaseEntity implements Serializable {

	static final long serialVersionUID = 5098466384372731320L;	
	
	String timeStamp;
	String ownerId;
	
	protected BaseEntity(String ownerId, String timeStamp)	{
		this.ownerId = ownerId;
		this.timeStamp = timeStamp;
	}	
	
	abstract public boolean isHigherPriority(BaseEntity other);
	abstract public String getTypeName();

}
