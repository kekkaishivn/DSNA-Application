package com.dsna.dht.nat;

import java.io.IOException;
import java.net.InetAddress;

//import net.sbbi.upnp.impls.InternetGatewayDevice;
//import net.sbbi.upnp.messages.UPNPResponseException;

import rice.environment.Environment;
import rice.environment.logging.Logger;
import rice.pastry.socket.nat.CantFindFirewallException;
import rice.pastry.socket.nat.sbbi.SBBINatHandler;

public class ClingNatHandler {

	  Logger logger;
	  Environment environment;
	  
	  boolean searchedForFireWall = false;

	  //InternetGatewayDevice fireWall;

	  InetAddress fireWallExternalAddress;

	  InetAddress localAddress;
	  
	  public ClingNatHandler(Environment env, InetAddress localAddress) {
	    environment = env;
	    logger = env.getLogManager().getLogger(ClingNatHandler.class, null); 
	    this.localAddress = localAddress;
	    System.out.println("SBBINatHandlerhandler is deployed");
	  }
	  
	  public synchronized InetAddress findFireWall(InetAddress bindAddress)
		      throws IOException {
//		    NetworkInterface ni = NetworkInterface.getByInetAddress(bindAddress);
		    if (searchedForFireWall)
		      return fireWallExternalAddress;
		    searchedForFireWall = true;
		    
		    System.out.println("Find fire wall invoked");
		    
		    int discoveryTimeout = environment.getParameters().getInt(
		        "nat_discovery_timeout");

		    // use this code with the next version of sbbi's upnp library, it will only
		    // search for the firewall on the given NetworkInterface
		    // InternetGatewayDevice[] IGDs =
		    // InternetGatewayDevice.getDevices(discoveryTimeout,
		    // Discovery.DEFAULT_TTL,
		    // Discovery.DEFAULT_MX,
		    // ni);
//		    if (IGDs != null) {
//		      // no idea how to handle this if there are 2 firewalls... handle the first
//		      // one
//		      // if they have that interesting of a network, then they know what they
//		      // are doing
//		      // and can configure port forwarding
//		      fireWall = IGDs[0];
//		      try {
//		        fireWallExternalAddress = InetAddress.getByName(fireWall.getExternalIPAddress());
//		      } catch (UPNPResponseException ure) {
//		        if (logger.level <= Logger.WARNING) logger.logException("Error:",ure);
//		        throw new IOException(ure.toString()); 
//		      }
//		    } else {
//		      throw new CantFindFirewallException(
//		          "Could not find firewall for bindAddress:"+bindAddress);
//		    }
		    return fireWallExternalAddress;
		  }
	  
	  
	  
	  public InetAddress getFireWallExternalAddress() {
		    return fireWallExternalAddress;
		  }
}
