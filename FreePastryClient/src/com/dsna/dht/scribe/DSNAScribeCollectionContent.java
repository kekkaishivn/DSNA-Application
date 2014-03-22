package com.dsna.dht.scribe;


import java.util.Collection;

import rice.p2p.commonapi.NodeHandle;
import rice.p2p.scribe.ScribeContent;

/**
 * @author Tien Dat le
 */
public class DSNAScribeCollectionContent implements ScribeContent {
  /**
   * The source of this content.
   */
  NodeHandle from;
  
  /**
   * The contents.
   */
  Collection<ScribeContent> contents;
  
  /**
   * Version of the contents 
   */
  long version;
  
  /**
   * Simple constructor.  Typically, you would also like some
   * interesting payload for your application.
   * 
   * @param from Who sent the message.
   * @param seq the sequence number of this content.
   */
  public DSNAScribeCollectionContent(NodeHandle from, Collection<ScribeContent> contents) {
    this.from = from;
    this.contents = contents;
//    System.out.println(this+".ctor");
  }

  /**
   * Ye ol' toString() 
   */
  public String toString() {
    return "DSNAScribeCollectionContent # from "+from+" version: "+version+" - contents :"+contents;
  }  
  
  public int getSize()	{
  	return contents.size();
  }
  
  public long getVersion()	{
  	return version;
  }
  
  public Collection<ScribeContent> getContents()	{
  	return contents;
  }
  
  public boolean isNewerThan(DSNAScribeCollectionContent other)	{
  	return this.version > other.version;
  }
}
