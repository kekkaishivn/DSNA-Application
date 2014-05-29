package com.dsna.entity.encrypted;

import rice.pastry.commonapi.PastryIdFactory;

import com.dsna.entity.Notification;
import com.dsna.entity.NotificationType;
import com.dsna.entity.SocialProfile;
import com.dsna.entity.exception.UnsupportedNotificationTypeException;
import com.dsna.util.DateTimeUtil;

public class IdbasedSecureSocialProfile extends SocialProfile {
	
	public IdbasedSecureSocialProfile(String ownerId, long timeStamp)	{
		super(ownerId, timeStamp);
	}
	
	public IdbasedSecureSocialProfile(SocialProfile s)	{
		super(s);
	}
	
	@Override
	public Notification createNotification(NotificationType type) throws UnsupportedNotificationTypeException	{
		switch (type)	{
		case SESSION_KEY_CHANGE:
			return new Notification(ownerId, DateTimeUtil.getCurrentTimeStamp(),
					"Session Key change in "+ownerId, type);
		default:
			return super.createNotification(type);
		}		
	}

	public static IdbasedSecureSocialProfile getSocialProfile(String username, PastryIdFactory idf)	{
		SocialProfile s = SocialProfile.getSocialProfile(username, idf);
		IdbasedSecureSocialProfile ibs = new IdbasedSecureSocialProfile(s);
		return ibs;
	}

}
