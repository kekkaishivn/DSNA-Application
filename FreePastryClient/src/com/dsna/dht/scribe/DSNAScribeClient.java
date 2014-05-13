package com.dsna.dht.scribe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import com.dsna.entity.BaseEntity;
import com.dsna.service.SocialEventListener;

import rice.p2p.commonapi.*;
import rice.p2p.scribe.Scribe;
import rice.p2p.scribe.ScribeMultiClient;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.ScribeImpl;
import rice.p2p.scribe.Topic;
import rice.pastry.commonapi.PastryIdFactory;

/**
 * We implement the Application interface to receive regular timed messages (see lesson5).
 * We implement the ScribeClient interface to receive scribe messages (called ScribeContent).
 * 
 * @author Jeff Hoye
 */
public class DSNAScribeClient implements ScribeReliableMultiClient, Application {

  /**
   * The message sequence number.  Will be incremented after each send.
   */
  int seqNum = 0;
  
  /**
   * This interface used for informing social service to update
   * We pass the important result from scribe client to the social service layer to handle update
   */
  ScribeEventListener uiUpdater;
  
  /** 
   * My handle to a scribe impl.
   */
  Scribe myScribe;
  
  /**
   * The only topic this appl is subscribing to.
   */
  Vector<Topic> subscribedTopics;

  /**
   * The Endpoint represents the underlieing node.  By making calls on the 
   * Endpoint, it assures that the message will be delivered to a MyApp on whichever
   * node the message is intended for.
   */
  protected Endpoint endpoint;
  
	PastryIdFactory idf; 

  /**
   * The constructor for this scribe client.  It will construct the ScribeApplication.
   * 
   * @param node the PastryNode
   */
  public DSNAScribeClient(Node node, ScribeEventListener uiUpdater) {
    // you should recognize this from lesson 3
    this.endpoint = node.buildEndpoint(this, "myinstance");
 
    // construct Scribe
    myScribe = new ScribeReliableImpl(node,"DSNAScribeInstance");
    
    idf = new PastryIdFactory(endpoint.getEnvironment());
    
    subscribedTopics = new Vector<Topic>();
    
    this.uiUpdater = uiUpdater;
    
    // now we can receive messages
    endpoint.register();
  }
  
  /**
   * Subscribes to the topic, attach the localLatestSeq to retrieve missing event
   */
  public void subscribe(String topicId, long localLastestSeq) {
  	Topic theTopic = this.getTopicFromId(topicId);
  	theTopic.setSeq(localLastestSeq);
  	theTopic.setCaching(true);
    myScribe.subscribe(theTopic, this);
  }
  
  /**
   * Subscribes to the noncache topics, attach the localLatestSeq to retrieve missing event
   */
  public void subscribeToNoncacheTopics(Collection<String> topicIds) {
  	Collection<Topic> theTopics = getTopicFromIds(topicIds);
    myScribe.subscribe(theTopics, this, null, null);
  }
  
  /**
   * Subscribes to the noncache topics, attach the localLatestSeq to retrieve missing event
   */
  public void subscribeToCacheTopics(Collection<String> topicIds, HashMap<String,Long> seqs) {
  	Collection<Topic> theTopics = getCachingTopicFromIds(topicIds, seqs);
    myScribe.subscribe(theTopics, this, null, null);
  }
  
  /**
   * Unsubscribes to the topic.
   */
  public void unsubscribe(String topicId) {
		Topic theTopic = this.getTopicFromId(topicId);
	  myScribe.unsubscribe(theTopic, this);
  }
  
  /**
   * Part of the Application interface.  Will receive PublishContent every so often.
   */
  public void deliver(Id id, Message message) {
	System.out.println("Got a message from "+id+" - Message: "+message);
    if (message instanceof PublishContent) {
      //sendMulticast();
    }
  }
  
  /**
   * Publish the message to topic have topicId
   */
  public void publish(String topicId, BaseEntity msg, boolean isCaching) {
    System.out.println("Node "+endpoint.getLocalNodeHandle()+" broadcasting "+seqNum + " in topic: " + topicId );
    DSNAScribeContent myMessage = new DSNAScribeContent(endpoint.getLocalNodeHandle(), 0, msg);
    Topic theTopic = getTopicFromId(topicId);
    theTopic.setCaching(isCaching);
    myScribe.publish(theTopic, myMessage); 
  }

  private Topic getTopicFromId(String topicId)	{
  	PastryIdFactory idfactory = new PastryIdFactory(this.endpoint.getEnvironment());
  	Id id = idfactory.buildIdFromToString(topicId);
  	return new Topic(id);
  }
  
  private Collection<Topic> getTopicFromIds(Collection<String> topicIds)	{
  	ArrayList<Topic> theTopics = new ArrayList<Topic>();
  	for (String topicId : topicIds)	{
  		Id idObject = idf.buildIdFromToString(topicId);
  		theTopics.add(new Topic(idObject));
  	}
  	return theTopics;
  }
  
  private Collection<Topic> getCachingTopicFromIds(Collection<String> topicIds, HashMap<String,Long> lastSeqs)	{
  	ArrayList<Topic> theTopics = new ArrayList<Topic>();
  	for (String topicId : topicIds)	{
  		Id idObject = idf.buildIdFromToString(topicId);
  		Topic topic = new Topic(idObject);
  		Long topicLastSeq = lastSeqs.get(topicId);
  		if (topicLastSeq!=null && topicLastSeq>0)
  			topic.setSeq(topicLastSeq);
  		else
  			topic.setSeq(Topic.SEQ_IGNORE);
  		topic.setCaching(true);
  		theTopics.add(topic);
  	}
  	return theTopics;
  }

  /**
   * Called whenever we receive a published message.
   */
  public void deliver(Topic topic, ScribeContent content) {
    System.out.println("Just got a message from "+topic+": "+content);
    if(uiUpdater!=null) 
    	this.uiUpdater.deliver(topic, content);
  }
  
  /**
   * Called when we receive an anycast.  If we return
   * false, it will be delivered elsewhere.  Returning true
   * stops the message here.
   */
  public boolean anycast(Topic topic, ScribeContent content) {
    //boolean returnValue = myScribe.getEnvironment().getRandomSource().nextInt(3) == 0;
    System.out.println("MyScribeClient.anycast("+topic+","+content+"):"+true);
    return true;
  }

  public void childAdded(Topic topic, NodeHandle child) {
	if(uiUpdater!=null) 
	  this.uiUpdater.childAdded(topic, child);
    System.out.println("MyScribeClient.childAdded("+topic+","+child+")");
  }

  public void childRemoved(Topic topic, NodeHandle child) {
	if(uiUpdater!=null) 
	  this.uiUpdater.childRemoved(topic, child);
    System.out.println("MyScribeClient.childRemoved("+topic+","+child+")");
  }

  public void subscribeFailed(Topic topic) {
	  if(uiUpdater!=null) 
	  	this.uiUpdater.subscribeFailed(topic);
      System.out.println("MyScribeClient.subscribeFailed("+topic.toString()+")");
  }

  public boolean forward(RouteMessage message) {
    return true;
  }


  public void update(NodeHandle handle, boolean joined) {
    
  }

  class PublishContent implements Message {
    public int getPriority() {
      return MAX_PRIORITY;
    }
  }

  
  /************ Some passthrough accessors for the myScribe *************/
  public boolean isRoot(Topic topic) {
    //return myScribe.isRoot(myTopic);
	  return myScribe.isRoot(topic);
  }
  
  public NodeHandle getParent(Topic topic) {
    // NOTE: Was just added to the Scribe interface.  May need to cast myScribe to a
    // ScribeImpl if using 1.4.1_01 or older.
    return ((ScribeReliableImpl)myScribe).getParent(topic); 
    //return myScribe.getParent(myTopic); 
  }
  
  public NodeHandle[] getChildren(Topic topic) {
    return myScribe.getChildren(topic); 
  }

	@Override
	public void subscribeFailed(Collection<Topic> topics) {
		// TODO Auto-generated method stub
		System.out.println("Subscribed Failed to " + topics);
		if(uiUpdater!=null) 
			this.uiUpdater.subscribeFailed(topics);
	}
	
	@Override
	public void subscribeSuccess(Collection<Topic> topics) {
		// TODO Auto-generated method stub
		System.out.println("Subscribed success to " + topics);
		if(uiUpdater!=null) 
			this.uiUpdater.subscribeSuccess(topics);
	}

	@Override
	public void contentsReceived(Collection<ScribeContent> contents, Topic topic) {
		// TODO Auto-generated method stub
		for(ScribeContent theContent:contents)	{
	    System.out.println("Just got a message from "+topic+": "+theContent);
	    if(uiUpdater!=null) 
	    	this.uiUpdater.deliver(topic, theContent);
	    if (((DSNAScribeContent)theContent).from == null) {
	      new Exception("Stack Trace").printStackTrace();
	    }
		}
	}
	
}
