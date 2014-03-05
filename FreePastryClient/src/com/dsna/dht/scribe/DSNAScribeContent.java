package com.dsna.dht.scribe;


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
  int seq;
  
  /**
   * The content.
   */
  String content;
  
  /**
   * Simple constructor.  Typically, you would also like some
   * interesting payload for your application.
   * 
   * @param from Who sent the message.
   * @param seq the sequence number of this content.
   */
  public DSNAScribeContent(NodeHandle from, int seq, String content) {
    this.from = from;
    this.seq = seq;
    this.content = content;
//    System.out.println(this+".ctor");
  }

  /**
   * Ye ol' toString() 
   */
  public String toString() {
    return "MyScribeContent #"+seq+" from "+from+" msg: "+content;
  }  
}
