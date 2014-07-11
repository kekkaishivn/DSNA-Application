package com.dsna.entity.encrypted;

import rice.pastry.commonapi.PastryIdFactory;

import com.dsna.entity.Message;
import com.dsna.entity.Notification;
import com.dsna.entity.NotificationType;
import com.dsna.entity.SocialProfile;
import com.dsna.entity.Status;
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
		Notification notification;
		switch (type)	{
		case SESSION_KEY_CHANGE:
			notification = new Notification(ownerId, DateTimeUtil.getCurrentTimeStamp(),
					"Session Key change in "+ownerId, type);
			notification.setPreferEncrypted(false);
			return notification;
		default:
			notification = super.createNotification(type);
			notification.setPreferEncrypted(getPreferEncrypted());
			return notification;
		}		
	}
	
	@Override
	public Message createMessage(String content, boolean isPrivate)	{
		Message msg = super.createMessage(content, isPrivate);
		msg.setPreferEncrypted(getPreferEncrypted());
		return msg;
	}
	
	@Override
	public Message createMessage(String content)	{
		return createMessage(content, getPreferEncrypted());
	}
	
	@Override
	public Status createStatus(String content)	{
		Status status = super.createStatus(content);
		status.setPreferEncrypted(getPreferEncrypted());
		return status;
	}

	public static IdbasedSecureSocialProfile getSocialProfile(String username, PastryIdFactory idf)	{
		SocialProfile s = SocialProfile.getSocialProfile(username, idf);
		IdbasedSecureSocialProfile ibs = new IdbasedSecureSocialProfile(s);
		return ibs;
	}

}
