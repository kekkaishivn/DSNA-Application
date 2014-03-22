package com.dsna.Entity;

import java.util.Set;

public class IpUpdateNotification extends Notification {
	
	private String status;
	private String ipaddress;

	public IpUpdateNotification(String ownerId, String timeStamp,
			String description, NotificationType type) {
		super(ownerId, timeStamp, description, type);
	}	

	public String getStatus() {
	    return this.status;
	}

	public String getIpaddress() {
	    return this.ipaddress;
	}

	public void setStatus(String status) {
	    this.status = status;
	}

	public void setIpaddress(String ipaddress) {
	    this.ipaddress = ipaddress;
	}	
}
