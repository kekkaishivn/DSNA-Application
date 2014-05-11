package com.dsna.crypto.ibbe.cd07.params;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.util.ElementUtils;

/**
 * @author Tien Dat Le (letiendat3012@gmail.com)
 */
public class CD07PublicKeyParameters extends CD07KeyParameters {
    private Element omega, v, h;
    private Element theta;
    private Element[] Ms;


    public CD07PublicKeyParameters(CD07Parameters parameters,
                                   Element omega, Element v, Element h, Element theta,
                                   Element[] Ms) {
        super(false, parameters);

        this.omega = omega.getImmutable();
        this.v = v.getImmutable();
        this.h = h.getImmutable();
        this.theta = theta.getImmutable();
        this.Ms = ElementUtils.cloneImmutable(Ms);
    }
    
    public Element getOmega() {
      return omega;
  }

    public Element getV() {
        return v;
    }

    public Element getH() {
        return h;
    }
    
    public Element getTheta()	{
    		return theta;
    }

    public Element getMAt(int index) {
        return Ms[index];
    }
}
