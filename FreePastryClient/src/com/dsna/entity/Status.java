package com.dsna.entity;

public class Status extends BaseEntity {
	
	public static final int TYPE = 5;
	private String content;
	
	Status(String ownerId, long timeStamp, String content)	{
		super(ownerId, timeStamp);
		this.content = content;
	}
	
	public String getContent()	{
		return content;
	}
	
	public String toString()	{
		return "Status of ["+ownerId+"] :"+content;
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
	public int getType() {
		return TYPE;
	}
}
