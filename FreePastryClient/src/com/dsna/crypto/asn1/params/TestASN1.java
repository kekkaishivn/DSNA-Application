package com.dsna.crypto.asn1.params;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SHA256Digest;

import com.dsna.crypto.asn1.exception.UnsupportedFormatException;
import com.dsna.crypto.ibbe.cd07.IBBECD07;
import com.dsna.crypto.ibbe.cd07.params.CD07DecryptionParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07PublicKeyParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07SecretKeyParameters;
import com.dsna.util.ASN1Util;
import com.dsna.util.FileUtil;

import rice.p2p.util.Base64;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.engines.PS06Signer;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.generators.PS06ParametersGenerator;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.generators.PS06SecretKeyGenerator;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.generators.PS06SetupGenerator;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.*;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;

public class TestASN1 {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
	// Setup -> (Public Key, Master Secret Key)
    AsymmetricCipherKeyPair keyPair = setup(createParameters(256, 256));
    PS06PublicKeyParameters ps06PublicKey = (PS06PublicKeyParameters)keyPair.getPublic();
    String encodedAsn1Ps06PublicKey = ASN1Util.encode(ps06PublicKey);
 // Extract -> Secret Key for Identity "01001101"
    String id1 = new BigInteger(doHash("letiendat3012@gmail.com")).toString(2);
    String id2 = new BigInteger(doHash("letiendat30@gmail.com")).toString(2);
    
    CipherParameters secretKey = extract(keyPair, id1);
    String encodedSecretKey = ASN1Util.encode(secretKey);

    CipherParameters decodedSecretKey = ASN1Util.decodePs06SecretParameters(encodedSecretKey, ps06PublicKey);

    // Sign
    String message = "Hi there, hello there, hehehee";
    byte[] signature = sign(message, decodedSecretKey);    
    try	{
    	PS06PublicKeyParameters decodedPublicKey = ASN1Util.decodePs06PublicParameters(encodedAsn1Ps06PublicKey);
   // verify with the original public key
      System.out.println(verify(keyPair.getPublic(), message, id1, signature));
   // verify with the decoded identity
      System.out.println(verify(decodedPublicKey, message, id2, signature));
      
      IBBECD07 engine = new IBBECD07();
      // Setup
      AsymmetricCipherKeyPair keyPair1 = engine.setup(300);
      
      String[] idsName = {"letiendat3012@gmail.com", "halo@yahoo.com", "tdle@vnu.edu.vn"};
      String encodedPublicKey1 = ASN1Util.encode(keyPair1.getPublic());
      
      Element[] ids1 = engine.map(keyPair1.getPublic(), idsName);
      CipherParameters secretKey1 = engine.extract(keyPair1, "letiendat3012@gmail.com");
      String encodedSecretKey1 = ASN1Util.encode(secretKey1);
      CipherParameters decodedSecretKey1 = ASN1Util.decodeCD07SecretParameters(encodedSecretKey1, (CD07PublicKeyParameters)keyPair1.getPublic());
      CipherParameters decryptionKey1 = new CD07DecryptionParameters((CD07SecretKeyParameters)decodedSecretKey1, ids1);

      // Encryption/Decryption
      byte[][] ciphertext = engine.encaps(ASN1Util.decodeCD07PublicParameters(encodedPublicKey1), ids1);
      byte[] key = engine.decaps(decryptionKey1, ciphertext[1]); 
      
      IBEPublicParameters ps06Public = (IBEPublicParameters)ASN1Util.toASN1Object(ps06PublicKey);
      IBEPublicParameters cd07Public = (IBEPublicParameters)ASN1Util.toASN1Object(keyPair1.getPublic());
      IBESecretParameters ps06Secret = (IBESecretParameters)ASN1Util.toASN1Object(secretKey);
      IBESecretParameters cd07Secret = (IBESecretParameters)ASN1Util.toASN1Object(secretKey1);
      
      
      Date notBefore = new Date(System.currentTimeMillis());
      Date notAfter = new Date(System.currentTimeMillis()+10000000);
      IBESysPublicParams certificate = new IBESysPublicParams(1, "KTH-Mobile Service", 10001, notBefore, notAfter, ps06Public, cd07Public);
      String encodedCertificate = ASN1Util.encode(certificate);
      IBESysPublicParams decodedCertificate = ASN1Util.decodeIBESysPublicParams(encodedCertificate);
      IBESysSecretParams userCertificate = new IBESysSecretParams(1, "KTH-Mobile Service", 10001, notBefore, notAfter, ps06Secret, cd07Secret);
      IBESysSecretParams decodedUserCertificate = ASN1Util.decodeIBESysSecretParams(ASN1Util.encode(userCertificate));
      CipherParameters[] sysPublicKeys = ASN1Util.extractPublicKey(decodedCertificate);
      CipherParameters[] sysPrivateKeys = ASN1Util.extractSecretKey(decodedUserCertificate, sysPublicKeys);
      CipherParameters decryptionKey2 = new CD07DecryptionParameters((CD07SecretKeyParameters)sysPrivateKeys[1], ids1);
      
      ciphertext = engine.encaps(sysPublicKeys[1], ids1);
      key = engine.decaps(decryptionKey2, ciphertext[1]);       
      
    } catch (Exception e)	{
    	e.printStackTrace();
    }
    
	}
	
  public static PS06Parameters createParameters(int nU, int nM) {
    // Generate Public PairingParameters
    return new PS06ParametersGenerator().init(
    				new TypeACurveGenerator(160, 512).generate(),
            nU, nM).generateParameters();
    
}

	public static AsymmetricCipherKeyPair setup(PS06Parameters parameters) {
	    PS06SetupGenerator setup = new PS06SetupGenerator();
	    setup.init(new PS06SetupGenerationParameters(null, parameters));
	    return setup.generateKeyPair();
	}
	
	public static CipherParameters extract(AsymmetricCipherKeyPair keyPair, String identity) {
    PS06SecretKeyGenerator extract = new PS06SecretKeyGenerator();
    extract.init(new PS06SecretKeyGenerationParameters(keyPair, identity));

    return extract.generateKey();
	}

	public static byte[] sign(String message, CipherParameters secretKey) {
	    byte[] bytes = message.getBytes();
	
	    PS06Signer signer = new PS06Signer(new SHA256Digest());
	    signer.init(true, new PS06SignParameters((PS06SecretKeyParameters) secretKey));
	    signer.update(bytes, 0, bytes.length);
	    
	    byte[] signature = null;
	    try {
	        signature = signer.generateSignature();
	    } catch (CryptoException e) {
	   	 e.printStackTrace();
	    }
	
	    return signature;
	}

	public static boolean verify(CipherParameters publicKey, String message, String identity, byte[] signature) {
	    byte[] bytes = message.getBytes();
	
	    PS06Signer signer = new PS06Signer(new SHA256Digest());
	    signer.init(false, new PS06VerifyParameters((PS06PublicKeyParameters) publicKey, identity));
	    signer.update(bytes, 0, bytes.length);
	
	    return signer.verifySignature(signature);
	}
	
	static byte[] doHash(String id)	{
    byte[] bytes = id.getBytes();
    SHA256Digest digest = new SHA256Digest();
    digest.update(bytes, 0, bytes.length);
    byte[] result = new byte[digest.getByteLength()];
    digest.doFinal(result, 0);
    return result;
	}

}
