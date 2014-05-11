package com.dsna.crypto.ibbe.cd07;

import java.util.Arrays;

import it.unisa.dia.gas.crypto.kem.KeyEncapsulationMechanism;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.jpbc.PairingParametersGenerator;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;

import com.dsna.crypto.ibbe.cd07.engines.CD07KEMEngine;
import com.dsna.crypto.ibbe.cd07.generators.CD07SecretKeyGenerator;
import com.dsna.crypto.ibbe.cd07.generators.CD07SetupGenerator;
import com.dsna.crypto.ibbe.cd07.params.CD07DecryptionParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07EncryptionParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07Parameters;
import com.dsna.crypto.ibbe.cd07.params.CD07SecretKeyGenerationParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07SecretKeyParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07SetupGenerationParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07PublicKeyParameters;

public class IBBECD07 {
	
	public IBBECD07()	{
		
	}
	
	public AsymmetricCipherKeyPair setup(int maxIds) {
		PairingParametersGenerator<PairingParameters> pg = new TypeACurveGenerator(160, 512);
		CD07Parameters params = new CD07Parameters(pg.generate(), maxIds);
		CD07SetupGenerator setup = new CD07SetupGenerator();
		setup.init(new CD07SetupGenerationParameters(null, params));
		return setup.generateKeyPair();
	}

	public Element[] map(CipherParameters publicKey, String[] ids) {
    Pairing pairing = PairingFactory.getPairing(((CD07PublicKeyParameters) publicKey).getParameters().getCurveParams());

    Element[] elements = new Element[ids.length];
    for (int i = 0; i < elements.length; i++) {
        byte[] id = ids[i].getBytes();
        elements[i] = pairing.getZr().newElementFromHash(id, 0, id.length);
    }
    return elements;
	}


	public CipherParameters extract(AsymmetricCipherKeyPair keyPair, String id) {
	// Extract -> Secret Key for Identity "01001101"
		CD07SecretKeyGenerator extract = new CD07SecretKeyGenerator();
    extract.init(new CD07SecretKeyGenerationParameters(keyPair, id));
    return extract.generateKey();
	}

	public byte[][] encaps(CipherParameters publicKey, Element[] ids) {
    try {
        KeyEncapsulationMechanism kem = new CD07KEMEngine();
        kem.init(true, new CD07EncryptionParameters((CD07PublicKeyParameters) publicKey, ids));

        byte[] ciphertext = kem.process();
        System.out.println(kem.getKeyBlockSize());

        byte[] key = Arrays.copyOfRange(ciphertext, 0, kem.getKeyBlockSize());
        byte[] ct = Arrays.copyOfRange(ciphertext, kem.getKeyBlockSize(), ciphertext.length);

        return new byte[][]{key, ct};
    } catch (InvalidCipherTextException e) {
        e.printStackTrace();
    }
    		return null;
	}

	public byte[] decaps(CipherParameters decryptionKey, byte[] cipherText) {
    try {
        KeyEncapsulationMechanism kem = new CD07KEMEngine();
        kem.init(false, decryptionKey);
        byte[] key = kem.processBlock(cipherText, 0, cipherText.length);
        return key;
    } catch (InvalidCipherTextException e) {
        e.printStackTrace();
    }

    return null;
	}


	public static void main(String[] args) {
		IBBECD07 engine = new IBBECD07();
    // Setup
    AsymmetricCipherKeyPair keyPair = engine.setup(300);
    String[] idsName = {"letiendat3012@gmail.com", "halo@yahoo.com", "tdle@vnu.edu.vn"};

    // KeyGen
    Element[] ids = engine.map(keyPair.getPublic(), idsName);
    CipherParameters secretKey = engine.extract(keyPair, "letiendat3012@gmail.com");
    CipherParameters decryptionKey = new CD07DecryptionParameters((CD07SecretKeyParameters)secretKey, ids);
    
    // Encryption/Decryption
    byte[][] ciphertext = engine.encaps(keyPair.getPublic(), ids);
    byte[] key = engine.decaps(decryptionKey, ciphertext[1]);
	}	

}
