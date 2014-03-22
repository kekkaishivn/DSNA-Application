package com.dsna.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.dsna.dht.past.DSNAPastClient;
import com.dsna.dht.past.DSNAPastFactory;
import com.dsna.dht.scribe.DSNAScribeClient;
import com.dsna.dht.scribe.DSNAScribeFactory;

import rice.environment.Environment;
import rice.pastry.JoinFailedException;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.socket.internet.InternetPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;

public class SocialServiceFactory {

	private Environment env;
	
	public SocialServiceFactory( Environment env )	{
		this.env = env;
	}	
	
	public SocialService newDSNASocialService(int bindPort, int bootPort, String bootIP, SocialEventListener uiUpdater, String username) throws IOException, InterruptedException, JoinFailedException	{
		DSNAScribeFactory scribeFactory = new DSNAScribeFactory(env);
		DSNAPastFactory pastFactory = new DSNAPastFactory(env);
		
	    // Factory generate the NodeIds Randomly
	    NodeIdFactory nidFactory = new RandomNodeIdFactory(env);

	    // construct the PastryNodeFactory, this is how we use rice.pastry.socket
	    InternetPastryNodeFactory factory = new InternetPastryNodeFactory(nidFactory,
	    		bindPort, env);
	    
	    // Contruct pastry node
	    PastryNode pastryNode = factory.newNode();
	    
	    // Create social service node
	    SocialServiceImpl theService = new SocialServiceImpl(username, pastryNode, uiUpdater);
	    
	    InetSocketAddress bootAddress = new InetSocketAddress(InetAddress.getByName(bootIP), bootPort);
	    
	    pastryNode.boot(bootAddress);

		// the node may require sending several messages to fully boot into the ring
		synchronized(pastryNode) {
			while(!pastryNode.isReady() && !pastryNode.joinFailed()) {
				// delay so we don't busy-wait
				pastryNode.wait(200);
	
				// abort if can't join
				if (pastryNode.joinFailed()) {
					throw pastryNode.joinFailedReason();
				}
			}       
		}

		System.out.println("Finished creating new DSNA social service: " + theService);
		return theService;
		
	}

}
