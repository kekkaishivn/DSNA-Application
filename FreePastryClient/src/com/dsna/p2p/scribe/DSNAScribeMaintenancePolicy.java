package com.dsna.p2p.scribe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import rice.environment.Environment;
import rice.environment.logging.Logger;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.scribe.Scribe;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.Topic;
import rice.p2p.scribe.maintenance.MaintainableScribe;
import rice.p2p.scribe.maintenance.ScribeMaintenancePolicy;
import rice.p2p.scribe.messaging.SubscribeMessage;
import rice.p2p.scribe.rawserialization.RawScribeContent;

public interface DSNAScribeMaintenancePolicy extends ScribeMaintenancePolicy {
  /**
   * Called periodically.  
   * 
   * Can be specified in millis by by the parameter: 
   *   p2p_scribe_maintenance_interval (default 180000) // 3 minutes
   */
  public void doMaintenance(MaintainableScribe scribe);
  
  /**
   * Called when membership changes "near" the local node, in overlay space.  
   * 
   * The typical use of this function would be to detect if the root has 
   * changed and subscribe to the new root, like this:
   * <pre>
   * for (Topic topic : topics) {
   *   scribe.subscribe(topic);
   * }
   * </pre>
   * 
   * Note however that this approach can cause a long tail at the head of the tree.
   * 
   * @param handle
   * @param membership
   */
  public void noLongerRoot(MaintainableScribe scribe, List<Topic> topics); 
  
  /**
   * When anyone in any Topic (child or parent) is detected faulty.
   * 
   * <pre>
   * for (Topic topic : nodeWasParent) {
   *   if (!isRoot(topic)) {
   *     scribe.subscribe(topic);
   *   }
   * }
   * </pre>
   * 
   */
  public void nodeFaulty(MaintainableScribe scribe, NodeHandle node, List<Topic> nodeWasParent, List<Topic> nodeWasChild); // when liveness changes

  /**
   * The subscription failed.  This is called if no particular client requested the Subscription.
   * 
   * TODO: Does this belong in the normal policy instead?
   * 
   * @param failedTopics
   */
  public void subscribeFailed(MaintainableScribe scribe, List<Topic> failedTopics);

  /**
   * Called when subscribing for maintenance or tree rearrangement (such as parent death).
   * 
   * This gives the MaintenancePolicy a chance to set the ScribeContent in these messages.
   * 
   * To convert a ScribeContent (java serialized) to a RawScribeContent use new JavaSerializedScribeContent(content)
   * 
   * @param topics the topics we are implicitly subscribing to
   * @return the ScribeContent to put into the SubscribeMessage (null is ok)
   */
  public RawScribeContent implicitSubscribe(List<Topic> topics);
    
  public class DSNAReliableScribeMaintenancePolicy implements
  	DSNAScribeMaintenancePolicy {

    Logger logger;
    
    public DSNAReliableScribeMaintenancePolicy(Environment environment) {
      logger = environment.getLogManager().getLogger(DefaultScribeMaintenancePolicy.class, null);
    }

    public void doMaintenance(MaintainableScribe scribe) {
      HashMap<NodeHandle, List<Topic>> manifest = new HashMap<NodeHandle, List<Topic>>();
      
      // for each topic, make sure our parent is still alive      
      for (Topic topic : scribe.getTopics()) {
        NodeHandle parent = scribe.getParent(topic);
        
        // also send an upward heartbeat message, which should make sure we are still subscribed
        if (parent != null) {
          List<Topic> topics = manifest.get(parent);
          if (topics == null) {
            topics = new ArrayList<Topic>();
            manifest.put(parent,topics);
          }
          topics.add(topic);
          
        } else {
          // If the parent is null, then we have either very recently sent out a
          // SubscribeMessage in which case we are fine. The way the tree
          // recovery works when my parent is null is that in the
          // sendSubscribe() method, a local mesage SubscribeLost message is
          // sceduled after message_timeout which in turn triggers the
          // subscribeFailed() method. For a node that is no longer the root,
          // the update method should send out the subscribe message
          // note, this shouldn't be necessary, because the leafset changes should fix this
          // this is in update()
//          if (!scribe.isRoot(topic)) {
//            if (logger.level <= Logger.WARNING)
//              logger.log("has null parent for " + manager.getTopic()
//                  + " inspite of NOT being root, root should be "+getRoot(manager.getTopic()));
//            scribe.subscribe(topic, null);
////            endpoint.route(manager.getTopic().getId(), new SubscribeMessage(handle, manager.getTopic(), handle.getId(), -1, null), null);
//          }        
        }
      }
      
      for (NodeHandle parent : manifest.keySet()) {
        List<Topic> topics = manifest.get(parent);
        scribe.getEndpoint().route(topics.get(0).getId(), 
            new SubscribeMessage(
              scribe.getEndpoint().getLocalNodeHandle(), 
              topics, MaintainableScribe.MAINTENANCE_ID, 
              implicitSubscribe(topics)), parent);
        parent.checkLiveness();
      }      
      
      // Periodically replicate the event cache
      if (scribe instanceof ScribeReliableImpl)	{
      	ScribeReliableImpl reliableScribe = (ScribeReliableImpl)scribe;
      	Set<Topic> topics = reliableScribe.publishedCaches.keySet();
      	for (Topic theTopic : topics)	
      		if (reliableScribe.isRoot(theTopic))
      			reliableScribe.asynchronousReplicateCache(theTopic);
      }
    }    
    
    public void noLongerRoot(MaintainableScribe scribe, List<Topic> topics) {
      scribe.subscribe(topics,null,implicitSubscribe(Collections.unmodifiableList(topics)),null);
    }

    public void nodeFaulty(MaintainableScribe scribe, NodeHandle handle,
        List<Topic> nodeWasParent, List<Topic> nodeWasChild) {      
//      if (wasParentOfTopics.size() > 1) logger.log(o+" declared dead "+wasParentOfTopics.size());
      scribe.subscribe(nodeWasParent,null,implicitSubscribe(Collections.unmodifiableList(nodeWasParent)),null);

    }

    public void subscribeFailed(MaintainableScribe scribe, List<Topic> failedTopics) {
//      logger.log("subscribeFailed("+failedTopics.iterator().next()+")");          
      scribe.subscribe(failedTopics, null, implicitSubscribe(Collections.unmodifiableList(failedTopics)), null);
    }
    
    /**
     * Called when subscribing for maintenance or tree rearrangement (such as parent death).
     * 
     * This gives the MaintenancePolicy a chance to set the ScribeContent in these messages.
     * 
     * @param topics the topics we are implicitly subscribing to
     * @return the ScribeContent to put into the SubscribeMessage (null is ok)
     */
    public RawScribeContent implicitSubscribe(List<Topic> topics) {
      return null;
    }
  }

}
