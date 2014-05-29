package com.dsna.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;

import com.dsna.dht.past.DSNAPastClient;
import com.dsna.dht.past.DSNAPastFactory;
import com.dsna.entity.SocialProfile;
import com.dsna.entity.encrypted.IdbasedSecureSocialProfile;
import com.dsna.p2p.scribe.DSNAScribeClient;
import com.dsna.p2p.scribe.DSNAScribeFactory;
import com.dsna.storage.cloud.CloudStorageService;

import rice.environment.Environment;
import rice.pastry.JoinFailedException;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.socket.internet.InternetPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;

public class SocialServiceFactory {

	private Environment env;
	
	public SocialServiceFactory( Environment env )	{
		this.env = env;
	}	
	
	public SocialService newDSNASocialService(int bindPort, int bootPort, String bootIP, SocialEventListener eventListener, String username) throws IOException, InterruptedException, JoinFailedException	{
		PastryIdFactory idf = new rice.pastry.commonapi.PastryIdFactory(env);
		SocialProfile user = SocialProfile.getSocialProfile(username, idf);
		return newDSNASocialService(bindPort, bootPort, bootIP, eventListener, user, new HashMap<String,Long>());
	}
	
	public IdBasedSecureSocialService newDSNAIdBasedSecureSocialService(int bindPort, int bootPort, String bootIP, SocialEventListener eventListener, String username) throws IOException, InterruptedException, JoinFailedException	{
		PastryIdFactory idf = new rice.pastry.commonapi.PastryIdFactory(env);
		SocialProfile user = SocialProfile.getSocialProfile(username, idf);
		return newDSNAIdBasedSecureSocialService(bindPort, bootPort, bootIP, eventListener, user, new HashMap<String,Long>());
	}
	
	public IdBasedSecureSocialService newDSNAIdBasedSecureSocialService(int bindPort, int bootPort, String bootIP, SocialEventListener eventListener, SocialProfile user, HashMap<String,Long> lastSeqs) throws IOException, InterruptedException, JoinFailedException	{
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
	    IdBasedSecureSocialServiceImpl theService = new IdBasedSecureSocialServiceImpl(new IdbasedSecureSocialProfile(user), lastSeqs, pastryNode, eventListener);
	    
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
	
	public SocialService newDSNASocialService(int bindPort, int bootPort, String bootIP, SocialEventListener eventListener, SocialProfile user, HashMap<String,Long> lastSeqs) throws IOException, InterruptedException, JoinFailedException	{
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
	    SocialServiceImpl theService = new SocialServiceImpl(user, lastSeqs, pastryNode, eventListener);
	    
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
