package com.dsna.Entity;

public class Message extends BaseEntity {

	static final long serialVersionUID = 987234019735861344L;		
	private String content;
	private String ownerName;
	
	Message(String ownerId, String ownerName, String timeStamp, String content)	{
		super(ownerId, timeStamp);
		this.content = content;
		this.ownerName = ownerName;
	}
	
	public String getContent()	{
		return content;
	}
	
	public String getOwnerName()	{
		return ownerName;
	}
	
	public String toString()	{
		return "Message from ["+ownerName+"] :"+content;
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
		return "Message";
	}
}
