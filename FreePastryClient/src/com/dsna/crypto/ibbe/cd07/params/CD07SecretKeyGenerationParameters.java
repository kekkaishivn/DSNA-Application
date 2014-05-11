package com.dsna.crypto.ibbe.cd07.params;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.KeyGenerationParameters;

/**
 * @author Tien Dat Le (letiendat3012@gmail.com)
 */
public class CD07SecretKeyGenerationParameters extends KeyGenerationParameters {

    private CD07PublicKeyParameters publicKey;
    private CD07MasterSecretKeyParameters masterSecretKey;
    private String identity;


    public CD07SecretKeyGenerationParameters(AsymmetricCipherKeyPair keyPair, String identity) {
        super(null, ((CD07PublicKeyParameters) keyPair.getPublic()).getH().getField().getLengthInBytes());

        this.publicKey = (CD07PublicKeyParameters) keyPair.getPublic();
        this.masterSecretKey = (CD07MasterSecretKeyParameters) keyPair.getPrivate();
        this.identity = identity;
    }


    public CD07PublicKeyParameters getPublicKey() {
        return publicKey;
    }

    public CD07MasterSecretKeyParameters getMasterSecretKey() {
        return masterSecretKey;
    }

    public String getIdentity() {
        return identity;
    }
}