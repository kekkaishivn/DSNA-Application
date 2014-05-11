package com.dsna.crypto.ibbe.cd07.generators;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.KeyGenerationParameters;

import com.dsna.crypto.ibbe.cd07.params.CD07MasterSecretKeyParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07Parameters;
import com.dsna.crypto.ibbe.cd07.params.CD07PublicKeyParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07SetupGenerationParameters;


/**
 * @author Tien Dat Le (letiendat3012@gmail.com)
 */
public class CD07SetupGenerator implements AsymmetricCipherKeyPairGenerator {
    private CD07SetupGenerationParameters param;


    public void init(KeyGenerationParameters param) {
        this.param = (CD07SetupGenerationParameters) param;
    }

    public AsymmetricCipherKeyPair generateKeyPair() {
        CD07Parameters parameters = param.getParameters();

        // take params
        Pairing pairing = PairingFactory.getPairing(parameters.getCurveParams());
        
        // Generate master secret key
        Element g = pairing.getG1().newRandomElement().getImmutable();
        Element theta = pairing.getZr().newRandomElement().getImmutable();
        
        int nM = parameters.getnM();

        // compute pk
        Element h = pairing.getG2().newRandomElement().getImmutable();
        Element omega = g.powZn(theta).getImmutable();
        Element v = pairing.pairing(g, h).getImmutable();
        
        Element[] Ms = new Element[nM];
        Ms[0] = h.powZn(theta);
        for (int i = 1; i < Ms.length; i++) {
            Ms[i] = Ms[i-1].powZn(theta).getImmutable();
        }

        return new AsymmetricCipherKeyPair(
            new CD07PublicKeyParameters(parameters, omega, v, h, theta, Ms),
            new CD07MasterSecretKeyParameters(parameters, g, theta)
        );
    }

}
