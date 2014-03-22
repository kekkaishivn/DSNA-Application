package com.dsna.dht.past;

import java.io.IOException;

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.past.ContentHashPastContentHandle;
import rice.p2p.past.gc.GCPastContentHandle;

public class DSNAPastContentHandle extends ContentHashPastContentHandle implements GCPastContentHandle {

	public DSNAPastContentHandle(InputBuffer buf, Endpoint endpoint)
			throws IOException {
		super(buf, endpoint);
	}
	
	  /**
	   * Constructor
	   *
	   * @param nh The handle of the node which holds the object
	   * @param id key identifying the object to be inserted
	   */
	  public DSNAPastContentHandle(NodeHandle nh, Id id, long version, long expiration) {
	    super(nh,id);
	    this.version = version;
	    this.expiration = expiration;
	  }

	/**
	 * 
	 */
	private static final long serialVersionUID = 1527844012282305214L;
	private long version;
	private long expiration;

	@Override
	public long getExpiration() {
		// TODO Auto-generated method stub
		return expiration;
	}

	@Override
	public long getVersion() {
		// TODO Auto-generated method stub
		return version;
	}



}
