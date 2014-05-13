package com.dsna.dht.past;

import com.dsna.entity.BaseEntity;
import com.dsna.entity.SocialProfile;

import rice.p2p.commonapi.Id;
import rice.p2p.past.ContentHashPastContent;
import rice.p2p.past.ContentHashPastContentHandle;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastException;
import rice.p2p.past.gc.GCPast;
import rice.p2p.past.gc.GCPastContent;
import rice.p2p.past.gc.GCPastContentHandle;
import rice.p2p.past.gc.GCPastMetadata;

public class DSNAPastContent extends ContentHashPastContent implements GCPastContent {
  
  /**
   * Store the content.
   * 
   * Note that this class is Serializable, so any non-transient field will 
   * automatically be stored to to disk.
   */
	BaseEntity content;
  
  /**
   * Store the version.
   * 
   * Note that this class is Serializable, so any non-transient field will 
   * automatically be stored to to disk.
   */
  private long version;
    
  public DSNAPastContent(Id id, BaseEntity content, long version, long expiration) {
    super(id);
    this.content = content;
    this.version = version;
  }
  
  public DSNAPastContent(Id id, BaseEntity content, long expiration) {
    super(id);
    this.content = content;
    this.version = 0;
  }
  
  /**
   * A descriptive toString()
   */
  public String toString() {
    return "MyPastContent ["+content+"]";
  }
  
  public BaseEntity getContent()	{
  	return content;
  }
  
/*  @Override
  public boolean isMutable() {
  	return true;
  }*/
  
  /**
   * Checks if a insert operation should be allowed.  Invoked when a
   * Past node receives an insert request and it is a replica root for
   * the id; invoked on the object to be inserted.  This method
   * determines the effect of an insert operation on an object that
   * already exists: it computes the new value of the stored object,
   * as a function of the new and the existing object.
   *
   * @param id the key identifying the object
   * @param existingObj the existing object stored on this node (null
   *        if no object associated with id is stored on this node)
   * @return null, if the operation is not allowed; else, the new
   *         object to be stored on the local node.
   */
   public PastContent checkInsert(Id id, PastContent existingContent) throws PastException {    
    // only allow correct content hash key
    if (!id.equals(getId())) {
      throw new PastException("DSNAPastContent: can't insert, content hash incorrect");
    }
    
    if (existingContent!=null)	{
	    try	{
	    	DSNAPastContent dpc = (DSNAPastContent)existingContent;
	    	if (isHigherPriority(dpc))
	    			throw new PastException("DSNAPastContent: can't insert, exist other file with higher priority here");
	    	version = dpc.version++;
	    } catch (Exception e)	{
	    	throw new PastException(e.toString());
	    }
    }
    
    return this;
  }
   
  private boolean isHigherPriority(DSNAPastContent other)	{
  	return content.isHigherPriority(other.content);
  }

@Override
public GCPastContentHandle getHandle(GCPast local, long expiration) {
	// TODO Auto-generated method stub
	return new DSNAPastContentHandle(local.getLocalNodeHandle(), getId(), version, expiration);
}

@Override
public GCPastMetadata getMetadata(long expiration) {
	// TODO Auto-generated method stub
	return new GCPastMetadata(expiration);
}

@Override
public long getVersion() {
	// TODO Auto-generated method stub
	return version;
}
  
  
}