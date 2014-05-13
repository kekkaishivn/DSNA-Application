package com.dsna.crypto.ibbe.cd07.params;

import it.unisa.dia.gas.jpbc.Element;

/**
 * @author Tien Dat Le (letiendat3012@gmail.com)
 */
public class CD07DecryptionParameters extends CD07BroadcastKeyParameters {

    public CD07DecryptionParameters(CD07SecretKeyParameters privateKey, Element[] identities) {
        super(true, privateKey, identities);
    }
    
    public CD07DecryptionParameters(CD07SecretKeyParameters privateKey, String[] identities) {
      super(true, privateKey, identities);
    }

    public CD07SecretKeyParameters getSecretKey() {
        return (CD07SecretKeyParameters)key;
    }

    public Element[] getIdentities() {
        return this.ids;
    }
}
