package com.dsna.Entity;

public class Message extends BaseEntity {

	static final long serialVersionUID = 987234019735861344L;	
	public static final int TYPE = 2;
	private String content;
	private String in_conversation;
	
	Message(String ownerId, String ownerName, long timeStamp, String content)	{
		super(ownerId, timeStamp);
		this.content = content;
		this.ownerUsername = ownerName;
	}
	
	public String getContent()	{
		return content;
	}
	
	public String getConversation()	{
		return in_conversation;
	}
	
	public void setConversation(String in_conversation)	{
		this.in_conversation = in_conversation;
	}
	
	public String toString()	{
		return "Message from ["+ownerUsername+"] :"+content;
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
