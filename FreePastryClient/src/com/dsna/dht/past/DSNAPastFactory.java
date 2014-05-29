package com.dsna.dht.past;

import java.io.IOException;

import com.dsna.p2p.scribe.DSNAScribeClient;
import com.dsna.service.SocialEventListener;

import rice.environment.Environment;
import rice.p2p.past.Past;
import rice.p2p.past.gc.GCPast;
import rice.pastry.JoinFailedException;
import rice.pastry.PastryNode;

public class DSNAPastFactory {

	private Environment env;
	
	public DSNAPastFactory(Environment env){
		this.env = env;
	}
	
	public DSNAPastClient newClient(GCPast storageHandler , PastEventListener uiUpdater) throws IOException, InterruptedException, JoinFailedException	{	
		return new DSNAPastClient(storageHandler,uiUpdater);
	}	
	
	public DSNAPastClient newClient(PastryNode node , PastEventListener uiUpdater) throws IOException, InterruptedException, JoinFailedException	{	
		return new DSNAPastClient(node,uiUpdater);
	}

}
