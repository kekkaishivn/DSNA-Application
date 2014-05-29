package com.dsna.p2p.scribe.message;

import java.io.IOException;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.Topic;
import rice.p2p.scribe.messaging.ScribeMessage;
import rice.p2p.scribe.rawserialization.ScribeContentDeserializer;

/**
 * @(#) ReplicateReqMessage.java
 *
 * The message contains topic for which the master demand a replicate request with replication node
 *
 * @version 
 *
 * @author Tien Dat Le
 */
public class ReplicatePushACKMessage extends ScribeMessage {
  public static final short TYPE = 21;

  /**
   * Constructor which takes a unique integer Id
   *
   * @param id The unique id
   * @param source The source address
   * @param dest The destination address
   */  
  public ReplicatePushACKMessage(NodeHandle source, Topic topic) {
    super(source, topic);
  }

  public String toString() {
    return "ReplicateACKMessage: "+topic+" - seq#"+topic.getSeq();
  }

  @Override
  public int getPriority() {
    return super.getPriority()+10;
  }

  /***************** Raw Serialization ***************************************/
  public short getType() {
    return TYPE;
  }

  public void serialize(OutputBuffer buf) throws IOException {
    buf.writeByte((byte)0); // version
    super.serialize(buf);     
  }
   
  public static ReplicatePushACKMessage build(InputBuffer buf, Endpoint endpoint, ScribeContentDeserializer scd) throws IOException {
    byte version = buf.readByte();
    switch(version) {
      case 0:
        return new ReplicatePushACKMessage(buf, endpoint, scd);
      default:
        throw new IOException("Unknown Version: "+version);
    }
  }
  
  /**
   * Private because it should only be called from build(), if you need to extend this,
   * make sure to build a serializeHelper() like in AnycastMessage/SubscribeMessage, and properly handle the 
   * version number.
   */
  private ReplicatePushACKMessage(InputBuffer buf, Endpoint endpoint, ScribeContentDeserializer cd) throws IOException {
    super(buf, endpoint);
  }
}
