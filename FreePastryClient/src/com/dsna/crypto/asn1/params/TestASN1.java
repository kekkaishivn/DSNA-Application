package com.dsna.crypto.asn1.params;

import java.io.IOException;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

import com.dsna.crypto.asn1.exception.InvalidCertificateException;
import com.dsna.crypto.asn1.exception.UnsupportedFormatException;
import com.dsna.crypto.ibbe.cd07.IBBECD07;
import com.dsna.crypto.ibbe.cd07.params.CD07DecryptionParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07PublicKeyParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07SecretKeyParameters;
import com.dsna.crypto.signature.ps06.PS06;
import com.dsna.util.ASN1Util;

import it.unisa.dia.gas.jpbc.Element;

public class TestASN1 {

	public static void main(String[] args) throws UnsupportedFormatException, IOException, InvalidCertificateException {
		{
	    PS06 ps06 = new PS06();
	
	    // Setup -> (Public Key, Master Secret Key)
	    AsymmetricCipherKeyPair ps06KeyPair = ps06.setup(ps06.createParameters(256, 256));
	
	    // Extract -> Secret Key for Identity "01001101"
	    CipherParameters ps06SecretKey = ps06.extract(ps06KeyPair, "letiendat3012@gmail.com");
	
	    // Sign
	    String message = "Hello World!!!";
	    byte[] signature = ps06.sign(message, ps06SecretKey);
	
	    // verify with the same identity
	    System.out.println(ps06.verify(ps06KeyPair.getPublic(), message, "letiendat3012@gmail.com", signature));
	
	    // verify with another identity
	    System.out.println(ps06.verify(ps06KeyPair.getPublic(), message, "letiendat30@gmail.com", signature));
		}
		{
			IBBECD07 engine = new IBBECD07();
	    // Setup
	    AsymmetricCipherKeyPair keyPair = engine.setup(50);
	    
	    String[] idsName = {"letiendat3012@gmail.com", "halo@yahoo.com", "tdle@vnu.edu.vn"};
	    String encodedPublicKey = ASN1Util.encode(keyPair.getPublic());
	    
	    Element[] ids = engine.map(keyPair.getPublic(), idsName);
	    CipherParameters secretKey = engine.extract(keyPair, "letiendat3012@gmail.com");
	    String encodedSecretKey = ASN1Util.encode(secretKey);
	    CipherParameters decodedSecretKey = ASN1Util.decodeCD07SecretParameters(encodedSecretKey, (CD07PublicKeyParameters)keyPair.getPublic());
	    CipherParameters decryptionKey = new CD07DecryptionParameters((CD07SecretKeyParameters)decodedSecretKey, ids);

	    // Encryption/Decryption
	    byte[][] ciphertext = engine.encaps(ASN1Util.decodeCD07PublicParameters(encodedPublicKey), ids);
	    byte[] key = engine.decaps(decryptionKey, ciphertext[1]);			
		}
		
	}

}
