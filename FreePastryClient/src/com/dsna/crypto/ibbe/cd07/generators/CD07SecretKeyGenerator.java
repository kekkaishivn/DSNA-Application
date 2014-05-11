package com.dsna.crypto.ibbe.cd07.generators;

import it.unisa.dia.gas.crypto.cipher.CipherParametersGenerator;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.KeyGenerationParameters;

import com.dsna.crypto.ibbe.cd07.params.CD07MasterSecretKeyParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07Parameters;
import com.dsna.crypto.ibbe.cd07.params.CD07PublicKeyParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07SecretKeyGenerationParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07SecretKeyParameters;
import com.dsna.util.HashUtil;


/**
 * @author Tien Dat Le (letiendat3012@gmail.com)
 */
public class CD07SecretKeyGenerator implements CipherParametersGenerator {
    private CD07SecretKeyGenerationParameters param;

    public void init(KeyGenerationParameters param) {
        this.param = (CD07SecretKeyGenerationParameters) param;
    }

    public CipherParameters generateKey() {
        // get params
        CD07Parameters parameters = param.getPublicKey().getParameters();
        CD07PublicKeyParameters pk = param.getPublicKey();
        CD07MasterSecretKeyParameters msk = param.getMasterSecretKey();
        byte[] bytes = param.getIdentity().getBytes();

        Pairing pairing = PairingFactory.getPairing(parameters.getCurveParams());
        Element g = msk.getG();
        Element theta = msk.getTheta();

        // compute secret key
        Element id = pairing.getZr().newElementFromHash(bytes, 0, bytes.length);
        Element skid = g.powZn(theta.add(id).invert());

        return new CD07SecretKeyParameters(pk, id, skid);
    }  

}