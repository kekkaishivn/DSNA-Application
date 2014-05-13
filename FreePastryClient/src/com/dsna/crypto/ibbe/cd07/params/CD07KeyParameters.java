package com.dsna.crypto.ibbe.cd07.params;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

/**
 * @author Tien Dat Le (letiendat3012@gmail.com)
 */
public class CD07KeyParameters extends AsymmetricKeyParameter {
	
		public static final String algorithmOid = "2.16.840.1.114334.1.2.1.5.1";
    private CD07Parameters parameters;

    public CD07KeyParameters(boolean isPrivate, CD07Parameters parameters) {
	    super(isPrivate);
	    this.parameters = parameters;
    }

    public CD07Parameters getParameters() {
      return parameters;
    }
}
