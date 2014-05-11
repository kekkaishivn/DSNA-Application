package com.dsna.crypto.ibbe.cd07.params;


import org.bouncycastle.crypto.KeyGenerationParameters;

import java.security.SecureRandom;

/**
 * @author Tien Dat Le (letiendat3012@gmail.com)
 */
public class CD07SetupGenerationParameters extends KeyGenerationParameters {

    private CD07Parameters params;

    public CD07SetupGenerationParameters(SecureRandom random, CD07Parameters params) {
        super(random, params.getSecureLength());
        this.params = params;
    }

    public CD07Parameters getParameters() {
        return params;
    }

}