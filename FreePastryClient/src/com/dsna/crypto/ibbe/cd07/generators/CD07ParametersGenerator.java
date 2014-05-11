package com.dsna.crypto.ibbe.cd07.generators;
	
import com.dsna.crypto.ibbe.cd07.params.CD07Parameters;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

/**
 * @author Tien Dat Le (letiendat3012@gmail.com)
 */
public class CD07ParametersGenerator {
    private PairingParameters curveParams;
    private Pairing pairing;
    private int nM;


    public CD07ParametersGenerator init(PairingParameters curveParams, int nM) {
        this.curveParams = curveParams;
        this.nM = nM;

        this.pairing = PairingFactory.getPairing(curveParams);
        return this;
    }

    public CD07Parameters generateParameters() {
        return new CD07Parameters(curveParams, nM);
    }
}	

