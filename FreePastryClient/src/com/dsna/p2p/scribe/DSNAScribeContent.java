package com.dsna.p2p.scribe;


import com.dsna.entity.BaseEntity;

import rice.p2p.commonapi.NodeHandle;
import rice.p2p.scribe.ScribeContent;

/**
 * @author Jeff Hoye
 */
public class DSNAScribeContent implements ScribeContent {
  /**
   * The source of this content.
   */
  NodeHandle from;
  
  /**
   * The sequence number of the content.
   */
  long seq;
  
  /**
   * The content.
   */
  BaseEntity message;
  
  /**
   * Simple constructor.  Typically, you would also like some
   * interesting payload for your application.
   * 
   * @param from Who sent the message.
   * @param seq the sequence number of this content.
   */
  public DSNAScribeContent(NodeHandle from, long seq, BaseEntity message) {
    this.from = from;
    this.seq = seq;
    this.message = message;
//    System.out.println(this+".ctor");
  }

  /**
   * Ye ol' toString() 
   */
  public String toString() {
    return "DSNAScribeContent #"+seq+" from "+from+" msg: "+message;
  }  
  
  public BaseEntity getMessage()	{
  	return message;
  }
  
  public long getSeq()	{
  	return seq;
  }
}
