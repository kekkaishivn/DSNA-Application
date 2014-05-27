package com.dsna.entity;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import rice.pastry.commonapi.PastryIdFactory;

import com.dsna.entity.encrypted.EncryptedEntity;
import com.dsna.util.DateTimeUtil;

public class SocialProfile extends BaseEntity {
	
	static final long serialVersionUID = 176342932409324L;	
	public static final int TYPE = 1;
	
	private String birthDay;
	private String about;
	private String wallObjectId;
	private String ToDeliverMessageTopic;
	private String ToFollowNotificationTopic;
	private String ToFollowRealIpTopic;
	private String profileImgUrl;
	
	/*
	 * Mapping key (user id) to friends profile
	 */
	private HashMap<String, SocialProfile> friendProfiles;
	
	/*
	 * Mapping conversation names to topics ids
	 */
	private HashMap<String, String> conversationMaps;

	public SocialProfile(String ownerId, long timeStamp) {
		super(ownerId, timeStamp);
		setToDeliverMessageTopic(ownerId+"_TODELIVERMESSAGE");
		setToFollowNotificationTopic(ownerId+"_TOFOLLOWNOTIFICATION");
		setToFollowRealIpTopic(ownerId+"_TOFOLLOWREALIP");
		ownerUsername = "USER1";
		friendProfiles = new HashMap<String, SocialProfile>();
		conversationMaps = new HashMap<String, String>();
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
			if (!friendProfiles.containsKey(newFriend.ownerId) && !ownerId.equalsIgnoreCase(newFriend.ownerId))	{
				friendProfiles.put(newFriend.ownerId, newFriend);
				addConversation(newFriend.ownerUsername, newFriend.ToDeliverMessageTopic);
				return true;
			}
			return false;
	}
	
	public void addConversation(String conversationName, String topicId)	{
		conversationMaps.put(conversationName, topicId);
	}
	
	public String getConversationTopicId(String conversationName)	{
		return conversationMaps.get(conversationName);
	}
	
	public void removeFriend(SocialProfile friend)	{
		friendProfiles.remove(friend.ownerId);
	}
	
	public String getFriendsUsername(String friendId)	{
		if (!friendProfiles.containsKey(friendId))
			return friendProfiles.get(friendId).ownerUsername;
		else 
			return null;
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
			SocialProfile friendProfile = friendProfiles.get(friendId);
			contacts.put(friendProfile.ownerUsername, friendId);
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
					return new Notification(ownerId, DateTimeUtil.getCurrentTimeStamp(),
							"Newfeed from "+ownerUsername, type);
				case PROFILEUPDATE:
					return new Notification(ownerId, DateTimeUtil.getCurrentTimeStamp(),
							"Profile update from "+ownerUsername, type);
				case IPUPDATE:
					return new Notification(ownerId, DateTimeUtil.getCurrentTimeStamp(),
							"Ip update from "+ownerUsername, type);
				case NEWCOMMENT:
					return new Notification(ownerId, DateTimeUtil.getCurrentTimeStamp(),
							"New comment in "+ownerId, type);
				case NEWLIKE:
					return new Notification(ownerId, DateTimeUtil.getCurrentTimeStamp(),
							"New like in "+ownerId, type);
				case SESSION_KEY_CHANGE:
					return new Notification(ownerId, DateTimeUtil.getCurrentTimeStamp(),
							"Session Key change in "+ownerId, type);
				default:
					return null;
			}
	}
	
	public Message createMessage(String content)	{
		Message msg = new Message(ownerId, ownerUsername, DateTimeUtil.getCurrentTimeStamp(), content);
		msg.ownerDisplayName = ownerDisplayName;
		return msg;
	}
	
	public Status createStatus(String content)	{
		Status status = new Status(ownerId, DateTimeUtil.getCurrentTimeStamp(), content);
		status.ownerDisplayName = ownerDisplayName;
		status.ownerUsername = ownerUsername;
		return status;
	}
	
	public EncryptedEntity createEncryptedEntity(BaseEntity entity, byte[] key, String algorithm) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, IOException	{
		return new EncryptedEntity(ownerId, DateTimeUtil.getCurrentTimeStamp(), entity, key, algorithm);
	}
	
	public Comment createComment(String content, String toObjectId)	{
		Comment comment = new Comment(ownerId, DateTimeUtil.getCurrentTimeStamp(), content, toObjectId);
		comment.ownerDisplayName = ownerDisplayName;
		comment.ownerUsername = ownerUsername;
		return comment;
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
	public int getType() {
		return TYPE;
	}
	
	public String toString()	{
		return this.friendProfiles.toString();
	}
	
	public static SocialProfile getSocialProfile(String username, PastryIdFactory idf)	{
		SocialProfile dsp = new SocialProfile(idf.buildId(username).toStringFull(), DateTimeUtil.getCurrentTimeStamp());
		dsp.ownerUsername = username;
		dsp.ownerDisplayName = "Daniel";
		dsp.setToDeliverMessageTopic(idf.buildId(username+"_MESSAGE").toStringFull());
		dsp.setToFollowNotificationTopic(idf.buildId(username+"_GETNOTIFY").toStringFull());
		dsp.setToFollowRealIpTopic(idf.buildId(username+"_GETIP").toStringFull());
		dsp.setWallObjectId(idf.buildId(dsp.ownerUsername+"_WALL").toStringFull());
		return dsp;
	}

}
