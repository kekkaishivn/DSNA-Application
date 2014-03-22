package com.dsna.dht.past;

import rice.p2p.past.PastContent;

public interface PastEventListener {
	public void receiveLookupResult(PastContent result);
	public void receiveLookupException(Exception result);
	public void receiveInsertResult(Boolean[] results);
	public void receiveInsertException(Exception result);
}
