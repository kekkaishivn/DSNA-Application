package com.dsna.service;

import java.util.Collection;

import com.dsna.Entity.Message;
import com.dsna.Entity.Notification;
import com.dsna.Entity.SocialProfile;
import com.dsna.Entity.Status;

import rice.p2p.past.PastContent;
import rice.p2p.scribe.ScribeMultiClient;
import rice.p2p.scribe.Topic;


public interface SocialEventListener {
	public void receiveMessage(Message msg);
	public void receiveNotification(Notification notification);
	public void receiveStatus(Status status);
	public void receiveSocialProfile(SocialProfile profile);
	public void receiveInsertException(Exception e);
	public void receiveLookupException(Exception e);
	public void receiveLookupNull();
	public void subscribeFailed(Topic topic);
	public void subscribeFailed(Collection<Topic> topics);
	public void subscribeSuccess(Collection<Topic> topics);
}
