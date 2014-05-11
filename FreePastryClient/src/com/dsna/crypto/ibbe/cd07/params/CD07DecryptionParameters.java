package com.dsna.crypto.ibbe.cd07.params;

import it.unisa.dia.gas.jpbc.Element;

/**
 * @author Angelo De Caro (jpbclib@gmail.com)
 */
public class CD07DecryptionParameters extends CD07BroadcastKeyParameters {

    public CD07DecryptionParameters(CD07SecretKeyParameters privateKey, Element[] identities) {
        super(true, privateKey, identities);
    }

    public CD07SecretKeyParameters getSecretKey() {
        return (CD07SecretKeyParameters)key;
    }

    public Element[] getIdentities() {
        return this.ids;
    }
}
