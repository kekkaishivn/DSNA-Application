package com.dsna.crypto.ibbe.cd07.params;

import it.unisa.dia.gas.jpbc.Element;

/**
 * @author Tien Dat Le (letiendat3012@gmail.com)
 */
public class CD07MasterSecretKeyParameters extends CD07KeyParameters {
    private Element g, theta;

    public CD07MasterSecretKeyParameters(CD07Parameters parameters, Element g, Element theta) {
        super(true, parameters);
        this.g = g.getImmutable();
        this.theta = theta.getImmutable();
    }

    public Element getG()	{
    		return g;
    }
    
    public Element getTheta()	{
    		return theta;
    }
}
