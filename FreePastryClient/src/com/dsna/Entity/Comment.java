package com.dsna.Entity;

import java.util.Set;

public class Comment extends BaseEntity {

	private String content;
	private String toObjectId;
	
	Comment(String ownerId, long timeStamp, String content, String toObjectId)	{
		super(ownerId, timeStamp);
		this.content = content;
		this.toObjectId = toObjectId;
	}
	
	public String getContent()	{
		return content;
	}
	
	public String getToObjectId()	{
		return toObjectId;
	}
	
	public String toString()	{
		return "Comment on <"+toObjectId+"> :"+content;
	}

	@Override
	public boolean isHigherPriority(BaseEntity other) {
		if (other instanceof SocialProfile)	{
			return false;
		} else if (ownerId.equalsIgnoreCase(other.ownerId))
							return false;
					 else return true;
	}

	@Override
	public String getTypeName() {
		return "Comment";
	}


}
