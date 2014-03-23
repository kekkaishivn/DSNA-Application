package com.dsna.service;

import java.io.IOException;
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
	private DSNAScribeClient broadcaster;
	private SocialEventListener eventListener;
	private PastryIdFactory idf;
	private SocialProfile user;
	private HashMap<String,Long> topicsLastSeq = new HashMap<String,Long>();
	
	protected SocialServiceImpl(PastryNode pastryNode, SocialEventListener eventListener) throws IOException, InterruptedException, JoinFailedException	{
		this.pastryNode = pastryNode;
		this.eventListener = eventListener;

		// Create storage service from pastry node
		DSNAPastFactory pastFactory = new DSNAPastFactory(pastryNode.getEnvironment());
		storageHandler = pastFactory.newClient(pastryNode, this);
		
		// Create publish/subscribe service from pastry node
		DSNAScribeFactory scribeFactory = new DSNAScribeFactory(pastryNode.getEnvironment());
		broadcaster = scribeFactory.newClient(pastryNode, this);
		
		idf = new rice.pastry.commonapi.PastryIdFactory(pastryNode.getEnvironment());
	}	
	
	public SocialServiceImpl(SocialProfile user, PastryNode pastryNode, SocialEventListener uiUpdater) throws IOException, InterruptedException, JoinFailedException	{
		this(pastryNode, uiUpdater);
		setSocialProfile(user);
	}
	
	public SocialServiceImpl(SocialProfile user, HashMap<String,Long> lastSeqs, PastryNode pastryNode, SocialEventListener uiUpdater) throws IOException, InterruptedException, JoinFailedException	{
		this(pastryNode, uiUpdater);
		setSocialProfile(user);
		setTopicsLastSeq(lastSeqs);
	}
	
	public SocialServiceImpl(String username, PastryNode pastryNode, SocialEventListener uiUpdater) throws IOException, InterruptedException, JoinFailedException	{
		this(pastryNode, uiUpdater);
		SocialProfile profile = null;
		profile = SocialProfile.getSocialProfile(username, idf);
		setSocialProfile(profile);
	}
	
	@Override
	public void postStatus(String status) {
		Id statusId = idf.buildId(status + user.getUsername());
		postStatus(statusId, status);
	}
	
	@Override
	public void postStatus(String id, String status) {
		Id statusId = idf.buildIdFromToString(id);
		postStatus(statusId, status);
	}
	
	protected void postStatus(final Id statusId, final String status) {
		String myUserName = user.getUsername();
		Status theStatus = new Status(myUserName, DateTimeUtil.getCurrentDateTime(), status);
		storageHandler.insert(new DSNAPastContent(statusId, theStatus, GCPast.INFINITY_EXPIRATION), new Continuation<Boolean[], Exception>() {
      // the result is an Array of Booleans for each insert
      public void receiveResult(Boolean[] results) {          
    		Notification notification = user.createNotification(NotificationType.NEWFEEDS);
    		notification.setDescription(statusId.toStringFull());
    		System.out.println(statusId.toStringFull());
    		notification.setArgument("objectId", statusId.toStringFull());
    		broadcaster.publish(user.getToFollowNotificationTopic(), notification, true);
      }

      public void receiveException(Exception result) {
      	eventListener.receiveInsertException(result);
      }
    });
		
	}

	@Override
	public boolean sendMessage(String friendId, String msg) {
		// TODO Auto-generated method stub
		String topicId = user.getFriendsToDeliverMessageTopic(friendId);
		Message message = user.createMessage(msg);
		if (topicId!=null)	{
			broadcaster.publish(topicId, message, true);
			return true;
		}
		return false;
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
	public void lookupById(final String id) {
		new Thread( new Runnable() {
        public void run() {
      		storageHandler.lookup(idf.buildIdFromToString(id));
            //Handler.postDelayed(Delayed, DisabledAfter);              
        }
    }).start();
	}
	
	@Override
	public void lookupByName(String name) {
		storageHandler.lookup(idf.buildId(name));
	}
	
	public SocialProfile defaultSocialProfile()	{
		return SocialProfile.getSocialProfile("Anonymous", idf);
	}
	
	private void setSocialProfile(SocialProfile user)	{
		if (user==null) user = defaultSocialProfile();
		this.user = user;
	}
	
	public void initSubscribe()	{
		System.out.println("init subscribe");
		Collection<String> cachingTopics = user.getFriendsToFollowNotificationTopics();
		Collection<String> noncachingTopics = user.getFriendsToFollowRealIpTopics();
		broadcaster.subscribeToNoncacheTopics(noncachingTopics);		
		cachingTopics.add(user.getToDeliverMessageTopic());
		broadcaster.subscribeToCacheTopics(cachingTopics, topicsLastSeq);
	}
	
	public void destroy()	{
		pastryNode.destroy();
		user = null;
	}

	@Override
	public void logout() {
		// TODO Auto-generated method stub
		destroy();
	}
	
	public SocialProfile getUserProfile()	{
		return user;
	}

	@Override
	public void updateProfile(SocialProfile edittedProfile) {
		// TODO Auto-generated method stub
		if (user.isCompatibleProfile(edittedProfile))	{
			user = edittedProfile;
		}
		pushProfileToDHT();
	}

	@Override
	public void pushProfileToDHT() {
		storageHandler.insert(new DSNAPastContent(idf.buildIdFromToString(user.getUserId()), user, GCPast.INFINITY_EXPIRATION));		
	}

	@Override
	public boolean addFriend(SocialProfile friend) {
		// TODO Auto-generated method stub
		if (user.addFriend(friend))	{
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
			switch(entity.getTypeName())	{
			case "SocialProfile":
				eventListener.receiveSocialProfile((SocialProfile)entity);
				break;
			case "Status":
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

	@Override
	public void deliver(Topic topic, ScribeContent content) {
		if (content instanceof DSNAScribeContent)	{
			DSNAScribeContent sc = (DSNAScribeContent)content;

			// Update lastSeq of the correspond topic
			if (topic.isCaching())	{
				topicsLastSeq.put( topic.getId().toStringFull(), sc.getSeq());
				System.out.println(topicsLastSeq);
				System.out.println(topic.getId().toStringFull());
			}
			
			// Pass entity to event listener
			BaseEntity msg = sc.getMessage();
			if (eventListener != null) 
				switch (msg.getTypeName())	{
					case "Message":
						eventListener.receiveMessage((Message)msg);
						break;
					case "Notification":
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
		return user.getFriendsContacts();
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

}
