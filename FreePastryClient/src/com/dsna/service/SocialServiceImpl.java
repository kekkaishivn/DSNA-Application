package com.dsna.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import com.dsna.Entity.BaseEntity;
import com.dsna.Entity.Message;
import com.dsna.Entity.Notification;
import com.dsna.Entity.NotificationType;
import com.dsna.Entity.SocialProfile;
import com.dsna.Entity.Status;
import com.dsna.dht.past.DSNAPastClient;
import com.dsna.dht.past.DSNAPastContent;
import com.dsna.dht.past.DSNAPastFactory;
import com.dsna.dht.past.PastEventListener;
import com.dsna.dht.scribe.DSNAScribeClient;
import com.dsna.dht.scribe.DSNAScribeCollectionContent;
import com.dsna.dht.scribe.DSNAScribeContent;
import com.dsna.dht.scribe.DSNAScribeFactory;
import com.dsna.dht.scribe.ScribeEventListener;
import com.dsna.storage.cloud.CloudStorageService;
import com.dsna.util.DateTimeUtil;
import com.dsna.util.FileUtil;

import rice.Continuation;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.past.PastContent;
import rice.p2p.past.gc.GCPast;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.ScribeMultiClient;
import rice.p2p.scribe.Topic;
import rice.pastry.JoinFailedException;
import rice.pastry.PastryNode;
import rice.pastry.commonapi.PastryIdFactory;

public class SocialServiceImpl implements SocialService, ScribeEventListener, PastEventListener {

	private PastryNode pastryNode;
	private DSNAPastClient storageHandler;
	private CloudStorageService cloudHandler;
	private DSNAScribeClient broadcaster;
	private SocialEventListener eventListener;
	private PastryIdFactory idf;
	private SocialProfile userProfile;
	private HashMap<String,Long> topicsLastSeq = new HashMap<String,Long>();
	
	protected SocialServiceImpl(PastryNode pastryNode, SocialEventListener eventListener, CloudStorageService cloudHandler) throws IOException, InterruptedException, JoinFailedException	{
		this.pastryNode = pastryNode;
		this.eventListener = eventListener;
		this.cloudHandler = cloudHandler;

		// Create storage service from pastry node
		DSNAPastFactory pastFactory = new DSNAPastFactory(pastryNode.getEnvironment());
		storageHandler = pastFactory.newClient(pastryNode, this);
		
		// Create publish/subscribe service from pastry node
		DSNAScribeFactory scribeFactory = new DSNAScribeFactory(pastryNode.getEnvironment());
		broadcaster = scribeFactory.newClient(pastryNode, this);
		
		idf = new rice.pastry.commonapi.PastryIdFactory(pastryNode.getEnvironment());
	}	
	
	public SocialServiceImpl(SocialProfile user, PastryNode pastryNode, SocialEventListener uiUpdater, CloudStorageService cloudHandler) throws IOException, InterruptedException, JoinFailedException	{
		this(pastryNode, uiUpdater, cloudHandler);
		setSocialProfile(user);
	}
	
	public SocialServiceImpl(SocialProfile user, HashMap<String,Long> lastSeqs, PastryNode pastryNode, SocialEventListener uiUpdater, CloudStorageService cloudHandler) throws IOException, InterruptedException, JoinFailedException	{
		this(pastryNode, uiUpdater, cloudHandler);
		setSocialProfile(user);
		setTopicsLastSeq(lastSeqs);
	}
	
	public SocialServiceImpl(String username, PastryNode pastryNode, SocialEventListener uiUpdater, CloudStorageService cloudHandler) throws IOException, InterruptedException, JoinFailedException	{
		this(pastryNode, uiUpdater, cloudHandler);
		SocialProfile profile = null;
		profile = SocialProfile.getSocialProfile(username, idf);
		setSocialProfile(profile);
	}
	
	@Override
	public void postStatus(String status) throws IOException {
		Id statusId = idf.buildId(status + userProfile.getOwnerUsername());
		postStatus(statusId, status);
	}
	
	@Override
	public void postStatus(String id, String status) throws IOException {
		Id statusId = idf.buildIdFromToString(id);
		postStatus(statusId, status);
	}
	
	protected void postStatus(final Id statusId, final String status) throws IOException {
		String myUserName = userProfile.getOwnerUsername();
		Status theStatus = userProfile.createStatus(status);
		
		if (cloudHandler!=null)	{
			InputStream content = createInputStreamFromObject(theStatus);
			String id = cloudHandler.uploadContentToFriendOnlyFolder(statusId.toStringFull()+".txt", "text/plain", "DSNA status", content);
  		Notification notification = userProfile.createNotification(NotificationType.NEWFEEDS);
  		notification.setDescription(statusId.toStringFull());
  		notification.setArgument("cloudId", id);
  		broadcaster.publish(userProfile.getToFollowNotificationTopic(), notification, true);			
			return;
		}		
		
		storageHandler.insert(new DSNAPastContent(statusId, theStatus, GCPast.INFINITY_EXPIRATION), new Continuation<Boolean[], Exception>() {
      // the result is an Array of Booleans for each insert
      public void receiveResult(Boolean[] results) {          
    		Notification notification = userProfile.createNotification(NotificationType.NEWFEEDS);
    		notification.setDescription(statusId.toStringFull());
    		System.out.println(statusId.toStringFull());
    		notification.setArgument("dhtId", statusId.toStringFull());
    		broadcaster.publish(userProfile.getToFollowNotificationTopic(), notification, true);
      }

      public void receiveException(Exception result) {
      	eventListener.receiveInsertException(result);
      }
    });
		
	}
	
	private InputStream createInputStreamFromObject(Serializable obj) throws IOException	{
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(obj);
    oos.flush();
    oos.close();
    InputStream is = new ByteArrayInputStream(baos.toByteArray());	
    return is;
	}

	@Override
	public Message sendMessage(String friendId, String msg) {
		// TODO Auto-generated method stub
		String topicId = userProfile.getFriendsToDeliverMessageTopic(friendId);
		Message message = userProfile.createMessage(msg);
		message.setConversation(userProfile.getOwnerUsername());
		if (topicId!=null)	{
			broadcaster.publish(topicId, message, true);
			return message;
		}
		return null;
	}
	
	@Override
	public Message sendMessageToConversation(String conversationName, String msg) {
		// TODO Auto-generated method stub
		String topicId = userProfile.getConversationTopicId(conversationName);
		Message message = userProfile.createMessage(msg);
		message.setConversation(userProfile.getOwnerUsername());
		if (topicId!=null)	{
			broadcaster.publish(topicId, message, true);
			return message;
		}
		return null;
	}

	@Override
	public void subscribe(String topic) {
		// TODO Auto-generated method stub
		this.broadcaster.subscribe(topic, 2);
	}

	@Override
	public void unsubscribe(String topic) {
		// TODO Auto-generated method stub
		this.broadcaster.unsubscribe(topic);
	}

	@Override
	public void publish(String topic, String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void lookupById(final String id, Continuation<PastContent, Exception> resultHandler) {
    storageHandler.lookup(idf.buildIdFromToString(id), resultHandler);
	}
	
	@Override
	public void lookupByName(String name, Continuation<PastContent, Exception> resultHandler) {
		storageHandler.lookup(idf.buildId(name), resultHandler);
	}
	
	public SocialProfile defaultSocialProfile()	{
		return SocialProfile.getSocialProfile("Anonymous", idf);
	}
	
	private void setSocialProfile(SocialProfile user)	{
		if (user==null) user = defaultSocialProfile();
		this.userProfile = user;
	}
	
	public void initSubscribe()	{
		System.out.println("init subscribe");
		Collection<String> cachingTopics = userProfile.getFriendsToFollowNotificationTopics();
		Collection<String> noncachingTopics = userProfile.getFriendsToFollowRealIpTopics();
		broadcaster.subscribeToNoncacheTopics(noncachingTopics);		
		cachingTopics.add(userProfile.getToDeliverMessageTopic());
		broadcaster.subscribeToCacheTopics(cachingTopics, topicsLastSeq);
	}
	
	public void destroy()	{
		pastryNode.destroy();
		userProfile = null;
	}

	@Override
	public void logout() {
		// TODO Auto-generated method stub
		destroy();
	}
	
	public SocialProfile getUserProfile()	{
		return userProfile;
	}

	@Override
	public void updateProfile(SocialProfile edittedProfile) {
		// TODO Auto-generated method stub
		if (userProfile.isCompatibleProfile(edittedProfile))	{
			userProfile = edittedProfile;
		}
		pushProfileToDHT();
	}

	@Override
	public void pushProfileToDHT() {
		storageHandler.insert(new DSNAPastContent(idf.buildIdFromToString(userProfile.getUserId()), userProfile, GCPast.INFINITY_EXPIRATION));		
	}

	@Override
	public boolean addFriend(SocialProfile friend) {
		// TODO Auto-generated method stub
		if (userProfile.addFriend(friend))	{
			broadcaster.subscribe(friend.getToFollowRealIpTopic(), Topic.SEQ_IGNORE);
			broadcaster.subscribe(friend.getToFollowNotificationTopic(), Topic.SEQ_IGNORE);
			return true;
		}
		return false;
	}

	@Override
	public void receiveLookupResult(PastContent result) {
		
		if (eventListener == null) return;
		
		if (result == null)	{
			eventListener.receiveLookupNull();
			return;
		}
		
		if (result instanceof DSNAPastContent)	{
			BaseEntity entity = ((DSNAPastContent)result).getContent();
			switch(entity.getType())	{
			case SocialProfile.TYPE:
				eventListener.receiveSocialProfile((SocialProfile)entity);
				break;
			case Status.TYPE:
				eventListener.receiveStatus((Status)entity);
				break;
			}
			
		}
	}

	@Override
	public void receiveLookupException(Exception result) {
		if (eventListener == null) return;
		eventListener.receiveLookupException(result);
	}

	@Override
	public void receiveInsertResult(Boolean[] results) {
		// TODO Auto-generated method stub
	}

	@Override
	public void receiveInsertException(Exception result) {
		if (eventListener == null) return;
		eventListener.receiveInsertException(result);
	}

	@Override
	public boolean anycast(Topic arg0, ScribeContent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void childAdded(Topic arg0, NodeHandle arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void childRemoved(Topic arg0, NodeHandle arg1) {
		// TODO Auto-generated method stub
		
	}
	
	long getTopicLastSeq(String topicId)	{
		if (topicsLastSeq.get(topicId)==null) return -1;
		else return topicsLastSeq.get(topicId);
	}

	@Override
	public void deliver(Topic topic, ScribeContent content) {
		if (content instanceof DSNAScribeContent)	{
			DSNAScribeContent sc = (DSNAScribeContent)content;
			
			// Update lastSeq of the correspond topic
			if (topic.isCaching() && getTopicLastSeq(topic.getId().toStringFull()) < sc.getSeq())	{
				topicsLastSeq.put(topic.getId().toStringFull(), sc.getSeq());
				System.out.println(topicsLastSeq);
				System.out.println(topic.getId().toStringFull());
			} else return;
			
			// Pass entity to event listener
			BaseEntity msg = sc.getMessage();
			if (eventListener != null) 
				switch (msg.getType())	{
					case Message.TYPE:
						eventListener.receiveMessage((Message)msg);
						break;
					case Notification.TYPE:
						eventListener.receiveNotification((Notification)msg);
						break;
					default:
				}
		}
		
		if (content instanceof DSNAScribeCollectionContent)	{
			DSNAScribeCollectionContent scc = (DSNAScribeCollectionContent)content;
			for (ScribeContent theContent : scc.getContents())	{
				deliver(topic, theContent);
			}
		}
		
	}

	@Override
	public void subscribeFailed(Topic arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subscribeFailed(Collection<Topic> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subscribeSuccess(Collection<Topic> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HashMap<String, String> getFriendsContacts() {
		// TODO Auto-generated method stub
		return userProfile.getFriendsContacts();
	}

	public HashMap<String, Long> getTopicsLastSeq() {
		// TODO Auto-generated method stub
		return (HashMap<String, Long>)topicsLastSeq.clone();
	}
	
	public void setTopicsLastSeq(HashMap<String, Long> topicsLastSeq) {
		// TODO Auto-generated method stub
		if (topicsLastSeq!=null)
			this.topicsLastSeq = new HashMap<String, Long>(topicsLastSeq);
	}

	@Override
	public void setCloudHandler(CloudStorageService cloudHandler) {
		// TODO Auto-generated method stub
		this.cloudHandler = cloudHandler;
	}



}
