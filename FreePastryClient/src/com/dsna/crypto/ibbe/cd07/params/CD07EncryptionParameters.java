package com.dsna.crypto.ibbe.cd07.params;

import it.unisa.dia.gas.jpbc.Element;

/**
 * @author Angelo De Caro (jpbclib@gmail.com)
 */
public class CD07EncryptionParameters extends CD07BroadcastKeyParameters {

  public CD07EncryptionParameters(CD07PublicKeyParameters publicKey, Element[] identities) {
      super(true, publicKey, identities);
  }
  
  public CD07EncryptionParameters(CD07PublicKeyParameters publicKey, String[] identities) {
    super(true, publicKey, identities);
  }  

  public CD07PublicKeyParameters getPublicKey() {
      return (CD07PublicKeyParameters)key;
  }

  public Element[] getIdentities() {
      return ids;
  }
}
