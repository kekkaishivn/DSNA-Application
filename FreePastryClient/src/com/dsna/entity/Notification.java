package com.dsna.entity;

import java.util.HashMap;

public class Notification extends BaseEntity {

	private static final long serialVersionUID = -1574287232308277685L;
	public static final int TYPE = 4;
	private NotificationType type;
	private String description;
	private HashMap<String,String> fileIdsMap;

	Notification(String ownerId, long timeStamp, String description, NotificationType type)	{
		super(ownerId, timeStamp);
		this.description = description;
		this.type = type;
		fileIdsMap = new HashMap<String,String>();
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
	
	public void setFileId(String location, String id)	{
		fileIdsMap.put(location, id);
	}
	
	public String getGetId(String location)	{
		return fileIdsMap.get(location);
	}
	
	@Override
	public int getType() {
		return TYPE;
	}
}
