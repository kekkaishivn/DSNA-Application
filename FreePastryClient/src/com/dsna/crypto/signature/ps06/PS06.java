package com.dsna.crypto.signature.ps06;

import java.math.BigInteger;

import it.unisa.dia.gas.crypto.jpbc.signature.ps06.engines.PS06Signer;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.generators.PS06ParametersGenerator;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.generators.PS06SecretKeyGenerator;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.generators.PS06SetupGenerator;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.*;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SHA256Digest;

import com.dsna.util.HashUtil;

/**
* @author Angelo De Caro (jpbclib@gmail.com)
*/
public class PS06 {

   public PS06() {
   }


   public PS06Parameters createParameters(int nU, int nM) {
       // Generate Public PairingParameters
	  	 return new PS06ParametersGenerator().init(
	 				new TypeACurveGenerator(160, 512).generate(),
	         nU, nM).generateParameters();
   }

   public AsymmetricCipherKeyPair setup(PS06Parameters parameters) {
       PS06SetupGenerator setup = new PS06SetupGenerator();
       setup.init(new PS06SetupGenerationParameters(null, parameters));

       return setup.generateKeyPair();
   }


   public CipherParameters extract(AsymmetricCipherKeyPair keyPair, String identity) {
  	 String binaryIdentity = new BigInteger(HashUtil.doSHA256Hash(identity)).toString(2);
  	 
  	 PS06SecretKeyGenerator extract = new PS06SecretKeyGenerator();
       extract.init(new PS06SecretKeyGenerationParameters(keyPair, binaryIdentity));

       return extract.generateKey();
   }

   public byte[] sign(String message, CipherParameters secretKey) {
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

   public boolean verify(CipherParameters publicKey, String message, String identity, byte[] signature) {
       byte[] bytes = message.getBytes();
       String binaryIdentity = new BigInteger(HashUtil.doSHA256Hash(identity)).toString(2);
       
       PS06Signer signer = new PS06Signer(new SHA256Digest());
       signer.init(false, new PS06VerifyParameters((PS06PublicKeyParameters) publicKey, binaryIdentity));
       signer.update(bytes, 0, bytes.length);

       return signer.verifySignature(signature);
   }

   public static void main(String[] args) {
       PS06 ps06 = new PS06();

       // Setup -> (Public Key, Master Secret Key)
       AsymmetricCipherKeyPair keyPair = ps06.setup(ps06.createParameters(256, 256));

       // Extract -> Secret Key for Identity "01001101"
       CipherParameters secretKey = ps06.extract(keyPair, "letiendat3012@gmail.com");

       // Sign
       String message = "Hello World!!!";
       byte[] signature = ps06.sign(message, secretKey);

       // verify with the same identity
       System.out.println(ps06.verify(keyPair.getPublic(), message, "letiendat3012@gmail.com", signature));

       // verify with another identity
       System.out.println(ps06.verify(keyPair.getPublic(), message, "letiendat30@gmail.com", signature));
   }
   
}
  
