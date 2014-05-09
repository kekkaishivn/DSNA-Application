package com.dsna.dht.past;

import rice.Continuation;
import rice.p2p.commonapi.Id;
import rice.p2p.past.Past;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastImpl;
import rice.p2p.past.gc.GCPast;
import rice.p2p.past.gc.GCPastImpl;
import rice.p2p.scribe.Scribe;
import rice.pastry.PastryNode;
import rice.pastry.commonapi.PastryIdFactory;
import rice.persistence.LRUCache;
import rice.persistence.MemoryStorage;
import rice.persistence.Storage;
import rice.persistence.StorageManagerImpl;

import com.dsna.service.SocialEventListener;

public class DSNAPastClient {

	  /**
	   * This interface used for informing the social service layer about past event
	   * We pass the important result from storage service to social service to handle update
	   */
	  final PastEventListener uiUpdater;
	  
	  /** 
	   * My handle to past storage impl.
	   */
	  GCPast storageHandler;
	  
	  /** 
	   * Default past id to generate id
	   */
	  PastryIdFactory idf;
	  
	  DSNAPastClient(GCPast storageHandler, PastEventListener uiUpdater)	{
		  this.storageHandler = storageHandler;
		  this.uiUpdater = uiUpdater;
		 // storageHandler.i
	  }
	  
	  DSNAPastClient(PastryNode node, PastEventListener uiUpdater)	{
		  this.uiUpdater = uiUpdater;
		  
	      // used for generating PastContent object Ids.
	      // this implements the "hash function" for our DHT
	      PastryIdFactory idf = new rice.pastry.commonapi.PastryIdFactory(node.getEnvironment());
	      
	      // create a different storage root for each node
	      String storageDirectory = "./storage"+node.getId().hashCode();

	      // create the persistent part
	      Storage stor = new MemoryStorage(idf);
	      storageHandler = new GCPastImpl(node, new StorageManagerImpl(idf, stor, new LRUCache(
	          new MemoryStorage(idf), 512 * 1024, node.getEnvironment())), 3, "", new DSNAPastPolicy(), 100);
	      //System.out.println( "Factor: " + storageHandler.getReplicationFactor());
	  }
	  
	  public void insert(PastContent content)	{
		  if( content instanceof DSNAPastContent )	{
		      // insert the data
			  final DSNAPastContent theContent = (DSNAPastContent)content;

			  storageHandler.insert(theContent, GCPast.INFINITY_EXPIRATION, new Continuation<Boolean[], Exception>() {
		        // the result is an Array of Booleans for each insert
		        public void receiveResult(Boolean[] results) {          
		          int numSuccessfulStores = 0;
		          for (int ctr = 0; ctr < results.length; ctr++) {
		            if (results[ctr].booleanValue()) 
		              numSuccessfulStores++;
		          }
		          System.out.println(theContent.toString() + " successfully stored at " + 
		              numSuccessfulStores + " locations.");
		          if(uiUpdater!=null)
		        	  uiUpdater.receiveInsertResult(results);
		        }
		  
		        public void receiveException(Exception result) {
		          System.out.println("Error storing "+theContent);
		          if(uiUpdater!=null)
		          	uiUpdater.receiveInsertException(result);
		        }
		      });
		  }
	  }
	  
	  public void insert(final PastContent content, Continuation<Boolean[], Exception> action)	{
		  if( content instanceof DSNAPastContent )	{
			  storageHandler.insert(content, GCPast.INFINITY_EXPIRATION, action);
		  }
	  }
	  
	  public void lookup(final Id lookupKey, Continuation<PastContent, Exception> action)	{
			  storageHandler.lookup(lookupKey, action);
	  }
	  
	  public void lookup(final Id lookupKey)	{
		  storageHandler.lookup(lookupKey, new Continuation<PastContent, Exception>() {
	          public void receiveResult(PastContent result) {
		        		System.out.println(result);
			          if(uiUpdater!=null)
			        	  uiUpdater.receiveLookupResult(result);
	          }
	    
	          public void receiveException(Exception result) {
	            System.out.println("Error looking up "+lookupKey);
		          if(uiUpdater!=null)
		        	  uiUpdater.receiveLookupException(result);
	          }
	        });
	  }
}
