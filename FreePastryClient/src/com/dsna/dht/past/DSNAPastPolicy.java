package com.dsna.dht.past;

import rice.Continuation;
import rice.Continuation.StandardContinuation;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.past.Past;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastContentHandle;
import rice.p2p.past.PastPolicy;
import rice.persistence.Cache;

public class DSNAPastPolicy implements PastPolicy {
	
	public DSNAPastPolicy()	{
		
	}
    /**
     * This method fetches the object via a lookup() call.
     *
     * @param id The id to fetch
     * @param hint A hint as to where the key might be
     * @param backup The backup cache, where the object *might* be located
     * @param past The local past instance 
     * @param command The command to call with the replica to store
     */
    public void fetch(final Id id, final NodeHandle hint, final Cache backup, final Past past, Continuation command) {
      if ((backup != null) && backup.exists(id)) {
        backup.getObject(id, command);
      } else {
        past.lookup(id, false, new StandardContinuation(command) {
          public void receiveResult(Object o) {
            if (o != null) 
              parent.receiveResult(o);
            else 
              past.lookupHandle(id, hint, new StandardContinuation(parent) {
                public void receiveResult(Object o) {
                  if (o != null) 
                    past.fetch((PastContentHandle) o, parent);
                  else
                    parent.receiveResult(null);
                }
              });
          }
        });
      }
    }
    
    /**
     * This method always return true;
     *
     * @param content The content about to be stored
     * @return Whether the insert should be allowed
     */
    public boolean allowInsert(PastContent content) {
    	if (content instanceof DSNAPastContent)	{
    		return true;
    	} 
    	return false;
    }



}
