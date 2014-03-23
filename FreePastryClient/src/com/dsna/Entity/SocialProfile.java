package com.dsna.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import rice.pastry.commonapi.PastryIdFactory;

import com.dsna.util.DateTimeUtil;

public class SocialProfile extends BaseEntity {
	
	private String displayName;
	private String username;
	private String birthDay;
	private String about;
	private String wallObjectId;
	private String ToDeliverMessageTopic;
	private String ToFollowNotificationTopic;
	private String ToFollowRealIpTopic;
	private String profileImgUrl;
	private HashMap<String, SocialProfile> friendProfiles;

	public SocialProfile(String ownerId, String timeStamp) {
		super(ownerId, timeStamp);
		setToDeliverMessageTopic(ownerId+"_TODELIVERMESSAGE");
		setToFollowNotificationTopic(ownerId+"_TOFOLLOWNOTIFICATION");
		setToFollowRealIpTopic(ownerId+"_TOFOLLOWREALIP");
		setDisplayName("USER1");
		friendProfiles = new HashMap<String, SocialProfile>();
	}

	public String getDisplayName() {
	    return this.displayName;
	}

	public int getAge() {
	    return 20;
	}

	public String getBirthDay() {
	    return this.birthDay;
	}

	public String getAbout() {
	    return this.about;
	}

	public String getWallObjectId() {
	    return this.wallObjectId;
	}

	public String getToDeliverMessageTopic() {
	    return this.ToDeliverMessageTopic;
	}

	public String getToFollowNotificationTopic() {
	    return this.ToFollowNotificationTopic;
	}

	public String getToFollowRealIpTopic() {
	    return this.ToFollowRealIpTopic;
	}

	public String getProfileImgUrl() {
	    return this.profileImgUrl;
	}

	public String getUsername() {
	    return this.username;
	}

	public void setUsername(String username) {
	    this.username = username;
	}

	public void setDisplayName(String displayName) {
	    this.displayName = displayName;
	}

	public void setBirthDay(String birthDay) {
	    this.birthDay = birthDay;
	}

	public void setAbout(String about) {
	    this.about = about;
	}

	public void setWallObjectId(String wallObjectId) {
	    this.wallObjectId = wallObjectId;
	}

	public void setToDeliverMessageTopic(String ToDeliverMessageTopic) {
	    this.ToDeliverMessageTopic = ToDeliverMessageTopic;
	}

	public void setToFollowNotificationTopic(String ToFollowNotificationTopic) {
	    this.ToFollowNotificationTopic = ToFollowNotificationTopic;
	}

	public void setToFollowRealIpTopic(String ToFollowRealIpTopic) {
	    this.ToFollowRealIpTopic = ToFollowRealIpTopic;
	}

	public void setProfileImgUrl(String profileImgUrl) {
	    this.profileImgUrl = profileImgUrl;
	}
	
	public boolean addFriend(SocialProfile newFriend)	{
			if (!friendProfiles.containsKey(newFriend.ownerId))	{
				friendProfiles.put(newFriend.ownerId, newFriend);
				return true;
			}
			return false;
	}
	
	public Collection<String> getFriendsToFollowNotificationTopics()	{
		ArrayList<String> topics = new ArrayList<String>();
		for (String key:friendProfiles.keySet())	{
			SocialProfile friend = friendProfiles.get(key);
			topics.add(friend.ToFollowNotificationTopic);
		}
		return topics;
	}
	
	public Collection<String> getFriendsToFollowRealIpTopics()	{
		ArrayList<String> topics = new ArrayList<String>();
		for (String key:friendProfiles.keySet())	{
			SocialProfile friend = friendProfiles.get(key);
			topics.add(friend.ToFollowRealIpTopic);
		}
		return topics;
	}
	
	public Collection<String> getFriendsToDeliverMessageTopics(Collection<String> ids)	{
		ArrayList<String> topics = new ArrayList<String>();
		for (String key:ids)	{
			SocialProfile friend = friendProfiles.get(key);
			if (friend!=null)	
				topics.add(friend.ToDeliverMessageTopic);
		}
		return topics;
	}
	
	public HashMap<String, String> getFriendsContacts()	{
		HashMap<String, String> contacts = new HashMap<String, String>();
		for (String friendId:friendProfiles.keySet())	{
			SocialProfile friend = friendProfiles.get(friendId);
			contacts.put(friend.username, friendId);
		}		
		return contacts;
	}
	
	public String getFriendsToDeliverMessageTopic(String id)	{
		SocialProfile friend = friendProfiles.get(id);
		if (friend!=null)	
			return friend.ToDeliverMessageTopic;
		else 
			return null;
	}
	
	public Notification createNotification(NotificationType type)	{
			switch (type)	{
				case NEWFEEDS: 
					return new Notification(ownerId, DateTimeUtil.getCurrentDateTime(),
							"Newfeed from "+username, type);
				case PROFILEUPDATE:
					return new Notification(ownerId, DateTimeUtil.getCurrentDateTime(),
							"Profile update from "+username, type);
				case IPUPDATE:
					return new Notification(ownerId, DateTimeUtil.getCurrentDateTime(),
							"Ip update from "+username, type);
				case NEWCOMMENT:
					return new Notification(ownerId, DateTimeUtil.getCurrentDateTime(),
							"New comment in "+ownerId, type);
				case NEWLIKE:
					return new Notification(ownerId, DateTimeUtil.getCurrentDateTime(),
							"New like in "+ownerId, type);
				default:
					return null;
			}
	}
	
	public Message createMessage(String content)	{
		return new Message(ownerId, username, DateTimeUtil.getCurrentDateTime(), content);
	}
	
	public boolean isCompatibleProfile(SocialProfile other)	{
		return ownerId.equalsIgnoreCase(other.ownerId);
	}
	
	@Override
	public boolean isHigherPriority(BaseEntity other) {
		if (other instanceof SocialProfile)	{
			if (ownerId.equalsIgnoreCase(other.ownerId))
				return false;
			else return true;
		} else  return true;
	}
	
	public String getUserId()	{
		return ownerId;
	}
	
	@Override
	public String getTypeName() {
		return "SocialProfile";
	}
	
	public String toString()	{
		return this.friendProfiles.toString();
	}
	
	public static SocialProfile getSocialProfile(String username, PastryIdFactory idf)	{
		SocialProfile dsp = new SocialProfile(idf.buildId(username).toStringFull(), DateTimeUtil.getCurrentDateTime());
		dsp.setUsername(username);
		dsp.setDisplayName("Daniel");
		dsp.setToDeliverMessageTopic(idf.buildId(username+"_MESSAGE").toStringFull());
		dsp.setToFollowNotificationTopic(idf.buildId(username+"_GETNOTIFY").toStringFull());
		dsp.setToFollowRealIpTopic(idf.buildId(username+"_GETIP").toStringFull());
		dsp.setWallObjectId(idf.buildId(dsp.getUsername()+"_WALL").toStringFull());
		return dsp;
	}

}
