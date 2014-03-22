package com.dsna.service;

import java.util.HashMap;

import com.dsna.Entity.SocialProfile;

public interface SocialService {
	
	//public String lookupFriendInfo(Id id);
	//deletePost();
	//sendMessage();
	//viewWall();
	
	public void postStatus(String status);
	public void postStatus(String id, String status);
	public void lookupById(String id);
	public void lookupByName(String name);
	public boolean addFriend(SocialProfile friend);
	public boolean sendMessage(String friendId, String msg);
	public void subscribe(String topic);
	public void unsubscribe(String topic);
	public void initSubscribe();
	public HashMap<String,String> getFriendsContacts();
	public void publish(String topic, String msg);
	public void updateProfile(SocialProfile edittedProfile);
	public SocialProfile getUserProfile();
	public void pushProfileToDHT();
	public void logout();
	//public boolean viewOfflineMessage();
	//public boolean sendFile();

}
