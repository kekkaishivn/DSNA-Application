package com.dsna.crypto.ibbe.cd07.params;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

/**
 * @author Angelo De Caro (jpbclib@gmail.com)
 */
public class CD07KeyParameters extends AsymmetricKeyParameter {
    private CD07Parameters parameters;

    public CD07KeyParameters(boolean isPrivate, CD07Parameters parameters) {
	    super(isPrivate);
	    this.parameters = parameters;
    }

    public CD07Parameters getParameters() {
      return parameters;
    }
}
