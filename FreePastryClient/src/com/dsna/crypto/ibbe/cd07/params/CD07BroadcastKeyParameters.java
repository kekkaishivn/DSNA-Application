package com.dsna.crypto.ibbe.cd07.params;

import org.bouncycastle.crypto.CipherParameters;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class CD07BroadcastKeyParameters extends CD07KeyParameters {
	
	protected Element[] ids;
	protected CD07KeyParameters key;

  public CD07BroadcastKeyParameters(boolean isPrivate, CD07KeyParameters key, Element[] identities) {
      super(isPrivate, key.getParameters());
      this.key = key;
      this.ids = identities;
  }
  
  public CD07BroadcastKeyParameters(boolean isPrivate, CD07KeyParameters key, String[] identities) {
    super(isPrivate, key.getParameters());
    this.key = key;
    
    if (key instanceof CD07PublicKeyParameters)	{
    	this.ids = map(key, identities);
    } else {
    	this.ids = map(((CD07SecretKeyParameters) key).getPublicKey(), identities);
    }
  }
  
  public Element[] map(CipherParameters publicKey, String[] ids) {
    Pairing pairing = PairingFactory.getPairing(((CD07PublicKeyParameters) publicKey).getParameters().getCurveParams());

    Element[] elements = new Element[ids.length];
    for (int i = 0; i < elements.length; i++) {
        byte[] id = ids[i].getBytes();
        elements[i] = pairing.getZr().newElementFromHash(id, 0, id.length);
    }
    return elements;
	}

}
