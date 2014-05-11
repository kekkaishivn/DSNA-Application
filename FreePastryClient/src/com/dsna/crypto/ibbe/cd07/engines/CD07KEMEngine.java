package com.dsna.crypto.ibbe.cd07.engines;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import it.unisa.dia.gas.crypto.jpbc.kem.PairingKeyEncapsulationMechanism;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.util.io.PairingStreamReader;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.Wrapper;

import com.dsna.crypto.ibbe.cd07.params.CD07DecryptionParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07EncryptionParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07PublicKeyParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07SecretKeyParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07KeyParameters;


public class CD07KEMEngine extends PairingKeyEncapsulationMechanism {
	
	@Override
	public void initialize() {
		if (forEncryption) {
      if (!(key instanceof CD07EncryptionParameters))
          throw new IllegalArgumentException("CD07EncryptionParameters are required for encryption.");
	  } else {
	      if (!(key instanceof CD07DecryptionParameters))
	          throw new IllegalArgumentException("CD07DecryptionParameters are required for decryption.");
	  }
	
	  this.pairing = PairingFactory.getPairing(((CD07KeyParameters) key).getParameters().getCurveParams());
	  this.keyBytes = pairing.getGT().getLengthInBytes();
	  this.inBytes = 2 * pairing.getG1().getLengthInBytes();
	  this.outBytes = 1 * pairing.getGT().getLengthInBytes() + 2 * pairing.getG1().getLengthInBytes();		
	}

	@Override
	public byte[] process(byte[] in, int inOff, int inLength)
			throws InvalidCipherTextException {
		
		if (key instanceof CD07EncryptionParameters) {
			
			CD07EncryptionParameters encryptionParams = (CD07EncryptionParameters)key;
			CD07PublicKeyParameters publicKey = encryptionParams.getPublicKey();
			Element k = pairing.getZr().newRandomElement().getImmutable();
 	 	  Element v = publicKey.getV().getImmutable();
 	 	  Element h = publicKey.getH().getImmutable();
 	 	  Element omega = publicKey.getOmega().getImmutable();
 	 	  Element theta = publicKey.getTheta().getImmutable();
 	 	  Element K = v.powZn(k).getImmutable();
 	 	  Element C1 = omega.powZn(k.negate()).getImmutable();
 	 	  Element[] ids = encryptionParams.getIdentities();
 	 	  
 	 	  Element tmp = k.mulZn(theta.add(ids[0])).getImmutable();
 	 	  for (int i=1; i<ids.length; i++)
 	 		  tmp = tmp.mulZn(theta.add(ids[i])).getImmutable();
 	 	  
 	 	  Element C2 = h.powZn(tmp).getImmutable();
 	 	  System.out.println(K);
 	 	  
 	 	  ByteArrayOutputStream bytes = new ByteArrayOutputStream(getOutputBlockSize());
 	 	  try	{
	 	 	  bytes.write(K.toBytes());
	 	 	  bytes.write(C1.toBytes());
	 	 	  bytes.write(C2.toBytes());
 	 	  } catch (IOException e)	{
 	 	  	e.printStackTrace();
 	 	  }
 	 	  return bytes.toByteArray();			
			
		} else	{
			
			CD07DecryptionParameters decryptionParams = (CD07DecryptionParameters)key;
			CD07SecretKeyParameters secretKey = decryptionParams.getSecretKey();	
			CD07PublicKeyParameters publicKey = secretKey.getPublicKey();
 	 	  Element h = publicKey.getH().getImmutable();
 	 	  Element theta = publicKey.getTheta().getImmutable();
 	 	  Element[] ids = decryptionParams.getIdentities();	
 	 	  Element id = secretKey.getIdentity().getImmutable();
 	 	  Element skid = secretKey.getSkid().getImmutable();
			
			PairingStreamReader streamParser = new PairingStreamReader(pairing, in, inOff);
			
			Element C1 = streamParser.readG1Element();
			Element C2 = streamParser.readG1Element();
			
			Element tmp1 = pairing.getZr().newOneElement().getImmutable();
	  	Element tmp2 = pairing.getZr().newOneElement().getImmutable();
	  	for (int i=0; i<ids.length; i++)	{	
	  		 if (!ids[i].equals(id))	{
	  			 tmp1 = tmp1.mulZn(ids[i]).getImmutable();
	  			 tmp2 = tmp2.mulZn(theta.add(ids[i])).getImmutable();
	  		 }
	  	}
	  	
	  	Element tmp3 = theta.invert().mulZn(tmp2.sub(tmp1)).getImmutable();
	  	Element inv_tmp1 = tmp1.invert().getImmutable();
	  	 
	  	Element tmp4 = pairing.pairing(C1, h.powZn(tmp3)).getImmutable();
	  	Element tmp5 = pairing.pairing(skid, C2).getImmutable();
	  	Element K = tmp4.mul(tmp5).powZn(inv_tmp1).getImmutable();
	  	System.out.println("Decap K: " + K);
	  	
		 	return K.toBytes();
		}
	}	
	
}
