package com.dsna.crypto.ibbe.cd07.params;

import it.unisa.dia.gas.jpbc.Element;

/**
 * @author Tien Dat Le (letiendat3012@gmail.com)
 */
public class CD07SecretKeyParameters extends CD07KeyParameters {
    private CD07PublicKeyParameters publicKey;
    private Element identity;
    private Element skid;

    public CD07SecretKeyParameters(CD07PublicKeyParameters publicKey, Element identity, Element skid) {
        super(true, publicKey.getParameters());

        this.publicKey = publicKey;
        this.identity = identity;
        this.skid = skid.getImmutable();
    }

    public CD07PublicKeyParameters getPublicKey() {
        return publicKey;
    }

    public Element getIdentity() {
        return identity;
    }

    public Element getSkid() {
        return skid;
    }
}