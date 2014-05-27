package com.dsna.entity;

import java.util.HashMap;

public class Notification extends BaseEntity {

	public static final int TYPE = 4;
	public static final String DHTID = "DHTID";
	public static final String GOOGLEID = "GOOGLEID";
	private NotificationType type;
	private String description;
	private HashMap<String,String> arguments;

	Notification(String ownerId, long timeStamp, String description, NotificationType type)	{
		super(ownerId, timeStamp);
		this.description = description;
		this.type = type;
		arguments = new HashMap<String,String>();
	}

	public NotificationType getNotificationType() {
	    return this.type;
	}

	public String getDescription() {
	    return this.description;
	}

	public void setType(NotificationType type) {
	    this.type = type;
	}

	public void setDescription(String description) {
	    this.description = description;
	}
	
/*	public Set<String> getToIds()	{
		return toIds;
	}*/
	
	public String toString()	{
		return "Notification from ["+ownerId+"] :"+description;
	}
	
	@Override
	public boolean isHigherPriority(BaseEntity other) {
		if (other instanceof SocialProfile)	{
			return false;
		} else if (ownerId.equalsIgnoreCase(other.ownerId))
							return false;
					 else return true;
	}
	
	public void setArgument(String argument, String value)	{
		arguments.put(argument, value);
	}
	
	public String getArgument(String argument)	{
		return arguments.get(argument);
	}
	
	@Override
	public int getType() {
		return TYPE;
	}
}
