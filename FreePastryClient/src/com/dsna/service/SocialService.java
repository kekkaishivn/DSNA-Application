package com.dsna.service;

import java.io.IOException;
import java.util.HashMap;

import rice.Continuation;
import rice.p2p.past.PastContent;

import com.dsna.Entity.Message;
import com.dsna.Entity.SocialProfile;
import com.dsna.storage.cloud.CloudStorageService;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

public interface SocialService {
	
	//public String lookupFriendInfo(Id id);
	//deletePost();
	//sendMessage();
	//viewWall();
	
	public void postStatus(String status) throws UserRecoverableAuthIOException, IOException;
	public void postStatus(String id, String status) throws UserRecoverableAuthIOException, IOException;
	public void lookupById(String id, Continuation<PastContent, Exception> action);
	public void lookupByName(String name, Continuation<PastContent, Exception> action);
	public boolean addFriend(SocialProfile friend);
	public Message sendMessage(String friendId, String msg);
	public Message sendMessageToConversation(String conversationName, String msg);
	public void subscribe(String topic);
	public void unsubscribe(String topic);
	public void initSubscribe();
	public HashMap<String,String> getFriendsContacts();
	public void publish(String topic, String msg);
	public void updateProfile(SocialProfile edittedProfile);
	public SocialProfile getUserProfile();
	public void pushProfileToDHT();
	public void setCloudHandler(CloudStorageService cloudHandler);
	public void logout();
	//public boolean viewOfflineMessage();
	//public boolean sendFile();

}
