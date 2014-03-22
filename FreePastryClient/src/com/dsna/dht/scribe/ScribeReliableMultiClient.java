package com.dsna.dht.scribe;

import java.util.Collection;

import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.ScribeMultiClient;
import rice.p2p.scribe.Topic;

public interface ScribeReliableMultiClient extends ScribeMultiClient {
	
  /**
   * Passing to the client the contents he missed when he is offline
   * - The client should take appropriate actions with the content
   *
   *
   * @param contents The missing contents, topic The correspond topic
   */
  public void contentsReceived(Collection<ScribeContent> contents, Topic topic);
  
}
