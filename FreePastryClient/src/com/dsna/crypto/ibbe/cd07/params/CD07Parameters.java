package com.dsna.crypto.ibbe.cd07.params;

import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import org.bouncycastle.crypto.CipherParameters;

/**
 * @author Tien Dat Le (letiendat3012@gmail.com)
 */
public class CD07Parameters implements CipherParameters {
    private PairingParameters curveParams;
    private int nM;
    private int secureLength;


    public CD07Parameters(PairingParameters curveParams, int nM) {
        this.curveParams = curveParams;
        this.nM = nM;
        this.secureLength = PairingFactory.getPairing(curveParams).getG1().getLengthInBytes();
    }

    public PairingParameters getCurveParams() {
        return curveParams;
    }

    public int getnM() {
        return nM;
    }
    
    public int getSecureLength()	{
    	return secureLength;
    }

}
