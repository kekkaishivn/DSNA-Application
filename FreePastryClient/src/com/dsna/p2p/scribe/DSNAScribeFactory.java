package com.dsna.p2p.scribe;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;

import org.mpisws.p2p.transport.multiaddress.MultiInetSocketAddress;

import com.dsna.service.SocialEventListener;

import rice.environment.Environment;
import rice.pastry.JoinFailedException;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.socket.internet.InternetPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;
import rice.tutorial.scribe.MyScribeClient;

public class DSNAScribeFactory {

	private Environment env;
	
	public DSNAScribeFactory( Environment env )	{
		this.env = env;
	}
	
	public DSNAScribeClient newClient(int bindPort, int bootPort, String bootIP, ScribeEventListener uiUpdater) throws IOException, InterruptedException, JoinFailedException	{
		
		DSNAScribeClient client = null;
		
		// Generate the NodeIds Randomly
		NodeIdFactory nidFactory = new RandomNodeIdFactory(env);
		ArrayList<InetSocketAddress> probAddresses = new ArrayList<InetSocketAddress>();
				
		InternetPastryNodeFactory factory = new InternetPastryNodeFactory(nidFactory, bindPort, env);
		MultiInetSocketAddress pAddress = new MultiInetSocketAddress(new InetSocketAddress(InetAddress.getByName(bootIP), bindPort));
		
		// construct a new node
		PastryNode node = factory.newNode();
		  
		// construct a new scribe application
		client = new DSNAScribeClient(node, uiUpdater);

		InetSocketAddress bootAddress = new InetSocketAddress(InetAddress.getByName(bootIP), bootPort);

		node.boot(bootAddress);

		// the node may require sending several messages to fully boot into the ring
		synchronized(node) {
			while(!node.isReady() && !node.joinFailed()) {
				// delay so we don't busy-wait
				node.wait(200);
	
				// abort if can't join
				if (node.joinFailed()) {
					throw node.joinFailedReason();
				}
			}       
		}

		System.out.println("Finished creating new node: " + node);
	    
		return client;
	}
	
	public DSNAScribeClient newClient(PastryNode node , ScribeEventListener uiUpdater) throws IOException, InterruptedException, JoinFailedException	{	
		return new DSNAScribeClient(node,uiUpdater);
	}

}
