package com.dsna.dht.scribe;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Iterator;

import com.dsna.desktop.client.ui.UIUpdater;

import rice.environment.Environment;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;
import rice.tutorial.scribe.MyScribeClient;

public class DSNAScribeFactory {

	private Environment env;
	
	public DSNAScribeFactory( Environment env )	{
		this.env = env;
	}
	
	public DSNAScribeClient newClient(int bindPort, int bootPort, String bootIP, UIUpdater uiUpdater) throws IOException, InterruptedException	{
		
		DSNAScribeClient client = null;
		
		// Generate the NodeIds Randomly
		NodeIdFactory nidFactory = new RandomNodeIdFactory(env);
		
		// construct the PastryNodeFactory, this is how we use rice.pastry.socket
		PastryNodeFactory factory = new SocketPastryNodeFactory(nidFactory, bindPort, env);

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
		      node.wait(500);
		  
		  // abort if can't join
		  if (node.joinFailed()) {
			  throw new IOException("Could not join the FreePastry ring.  Reason:"+node.joinFailedReason()); 
		      }
		    }       
		  }
		  
		  System.out.println("Finished creating new node: " + node);
	    
		return client;
	}

}
