package com.dsna.dht.scribe;

import java.util.Collection;
import java.util.Vector;

import com.dsna.desktop.client.ui.UIUpdater;

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
public class DSNAScribeClient implements ScribeMultiClient, Application {

  /**
   * The message sequence number.  Will be incremented after each send.
   */
  int seqNum = 0;
  
  /**
   * This interface used for informing UI to update
   * We pass the important result from scribe client to the UI to handle update
   */
  UIUpdater uiUpdater;
  
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

  /**
   * The constructor for this scribe client.  It will construct the ScribeApplication.
   * 
   * @param node the PastryNode
   */
  public DSNAScribeClient(Node node, UIUpdater uiUpdater) {
    // you should recognize this from lesson 3
    this.endpoint = node.buildEndpoint(this, "myinstance");

    // construct Scribe
    myScribe = new ScribeImpl(node,"DSNAScribeInstance");
    
    subscribedTopics = new Vector<Topic>();
    
    this.uiUpdater = uiUpdater;
    
    // now we can receive messages
    endpoint.register();
  }
  
  /**
   * Subscribes to the topic.
   */
  public void subscribe(String topic) {
	Topic theTopic = this.getTopicFromName(topic);
    myScribe.subscribe(theTopic, this);
  }
  
  /**
   * Unsubscribes to the topic.
   */
  public void unsubscribe(String topic) {
	Topic theTopic = this.getTopicFromName(topic);
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
   * Sends the multicast message.
   */
  public void sendMulticast(String topic, String msg) {
    System.out.println("Node "+endpoint.getLocalNodeHandle()+" broadcasting "+seqNum + " in topic: " + topic );
    DSNAScribeContent myMessage = new DSNAScribeContent(endpoint.getLocalNodeHandle(), seqNum, msg);
    myScribe.publish(getTopicFromName(topic), myMessage); 
    seqNum++;
  }

  private Topic getTopicFromName(String topicName)	{
	return new Topic(new PastryIdFactory(this.endpoint.getEnvironment()),topicName);
  }

  /**
   * Called whenever we receive a published message.
   */
  public void deliver(Topic topic, ScribeContent content) {
    System.out.println("Just got a message from "+topic+": "+content);
    this.uiUpdater.deliver(topic, content);
    if (((DSNAScribeContent)content).from == null) {
      new Exception("Stack Trace").printStackTrace();
    }
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
	  this.uiUpdater.childAdded(topic, child);
    System.out.println("MyScribeClient.childAdded("+topic+","+child+")");
  }

  public void childRemoved(Topic topic, NodeHandle child) {
	  this.uiUpdater.childRemoved(topic, child);
    System.out.println("MyScribeClient.childRemoved("+topic+","+child+")");
  }

  public void subscribeFailed(Topic topic) {
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
    return ((ScribeImpl)myScribe).getParent(topic); 
    //return myScribe.getParent(myTopic); 
  }
  
  public NodeHandle[] getChildren(Topic topic) {
    return myScribe.getChildren(topic); 
  }

	@Override
	public void subscribeFailed(Collection<Topic> topics) {
		// TODO Auto-generated method stub
		System.out.println("Subscribed Failed to " + topics);
		this.uiUpdater.subscribeFailed(topics);
	}
	
	@Override
	public void subscribeSuccess(Collection<Topic> topics) {
		// TODO Auto-generated method stub
		System.out.println("Subscribed success to " + topics);
		this.uiUpdater.subscribeSuccess(topics);
	}
	

  
}
