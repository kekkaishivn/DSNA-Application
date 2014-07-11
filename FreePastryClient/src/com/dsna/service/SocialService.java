package com.dsna.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import rice.Continuation;
import rice.p2p.past.PastContent;

import com.dsna.entity.BaseEntity;
import com.dsna.entity.Message;
import com.dsna.entity.SocialProfile;
import com.dsna.storage.cloud.CloudStorageService;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

public interface SocialService {
	
	//public String lookupFriendInfo(Id id);
	//deletePost();
	//sendMessage();
	//viewWall();
	
	public void postStatus(String status) throws UserRecoverableAuthIOException, IOException;
	public void postStatus(String id, String status) throws UserRecoverableAuthIOException, IOException;
	public void lookupDHTById(String id, Continuation<PastContent, Exception> action);
	public void lookupDHTByName(String name, Continuation<PastContent, Exception> action);
	public void lookupCloudsById(String cloudLocation, String id, Continuation<InputStream, Exception> action);
	public boolean addFriend(SocialProfile friend);
	public Message sendMessage(String friendId, String msg);
	public Message sendMessageToConversation(String conversationName, String msg);
	public void subscribe(String topic);
	public void unsubscribe(String topic);
	public void initSubscribe();
	public void broadcast(String topicId, BaseEntity msg, boolean isCaching);
	public HashMap<String,String> getFriendsContacts();
	public void updateProfile(SocialProfile edittedProfile);
	public SocialProfile getUserProfile();
	public void pushProfileToDHT();
	public void addCloudHandler(String cloudLocation, CloudStorageService cloudHandler) throws UserRecoverableAuthIOException, IOException;
	public void logout();

}
