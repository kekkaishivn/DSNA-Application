package com.dsna.service;

import java.util.Collection;

import com.dsna.entity.BaseEntity;
import com.dsna.entity.Message;
import com.dsna.entity.Notification;
import com.dsna.entity.SocialProfile;
import com.dsna.entity.Status;

import rice.p2p.past.PastContent;
import rice.p2p.scribe.ScribeMultiClient;
import rice.p2p.scribe.Topic;


public interface SocialEventListener {
	public void receiveMessage(Message msg);
	public void receiveNotification(Notification notification);
	public void receiveStatus(Status status);
	public void receiveSocialProfile(SocialProfile profile);
	public void receiveBaseEntity(BaseEntity entity);
	public void receiveInsertException(Exception e);
	public void receiveLookupException(Exception e);
	public void receiveLookupNull();
	public void subscribeFailed(Topic topic);
	public void subscribeFailed(Collection<Topic> topics);
	public void subscribeSuccess(Collection<Topic> topics);
}
