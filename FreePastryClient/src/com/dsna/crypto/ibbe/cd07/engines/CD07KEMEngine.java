package com.dsna.crypto.ibbe.cd07.engines;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import it.unisa.dia.gas.crypto.jpbc.kem.PairingKeyEncapsulationMechanism;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.util.io.PairingStreamReader;

import org.bouncycastle.crypto.InvalidCipherTextException;

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
	
	private static ArrayList<Element> multiply(ArrayList<Element> firstPolynomial,ArrayList<Element> secondPolynomial, Field Zp) {

    ArrayList<Element> array =new ArrayList<Element>(firstPolynomial.size()+secondPolynomial.size()-1);
    for (int i=0;i<firstPolynomial.size()+secondPolynomial.size()-1;i++)
        array.add(i, Zp.newZeroElement());

    for (int i = 0; i < firstPolynomial.size(); i++)

        for (int j = 0; j < secondPolynomial.size(); j++)

            array.set(i+j, firstPolynomial.get(i).mulZn(secondPolynomial.get(j)).add(array.get(i+j)));

    return array;
	}
	
	private static ArrayList<Element> computeFi(Element k, Element[] ids)	{
				
		Element one = k.getField().newOneElement().getImmutable();
		
		ArrayList<Element> firstPolynomial = new ArrayList<Element>();
		firstPolynomial.add(one);
		firstPolynomial.add(ids[0].getImmutable());
		
		for (int i=1; i<ids.length; i++)	{
			ArrayList<Element> secondPolynomial = new ArrayList<Element>();
			secondPolynomial.add(one);
			secondPolynomial.add(ids[i].getImmutable());			
			firstPolynomial = multiply(firstPolynomial, secondPolynomial, k.getField());
		}
		
		for (int i=0; i<firstPolynomial.size(); i++)
			firstPolynomial.set(i, firstPolynomial.get(i).mulZn(k));
	
		return firstPolynomial;
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
 	 	  Element K = v.powZn(k).getImmutable();
 	 	  Element C1 = omega.powZn(k.negate()).getImmutable();
 	 	  Element[] ids = encryptionParams.getIdentities();
 	 	  
 	 	  ArrayList<Element> fi = computeFi(k, ids);
 	 	  
 	 	  Element C2 = h.powZn(fi.get(fi.size()-1)).getImmutable();
 	 	  
 	 	  for (int i=1; i<fi.size(); i++)	{
 	 	  	Element tmp = publicKey.getMAt(i-1).powZn(fi.get(fi.size()-1-i)).getImmutable();
 	 	  	C2 = C2.mul(tmp).getImmutable();
 	 	  }
 	 	  
 	 	  System.out.println("Encap: " + K);
 	 	  
 	 	  // Direct formula with theta to test purpose
/* 	 	  Element tmp = k.mulZn(theta.add(ids[0])).getImmutable();
 	 	  for (int i=1; i<ids.length; i++)
 	 		  tmp = tmp.mulZn(theta.add(ids[i])).getImmutable();
 	 	  
 	 	  Element C2 = h.powZn(tmp).getImmutable();*/
 	 	  
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
 	 	  Element[] ids = decryptionParams.getIdentities();	
	  	Element[] idsExclude = new Element[ids.length-1];
 	 	  Element id = secretKey.getIdentity().getImmutable();
 	 	  Element skid = secretKey.getSkid().getImmutable();
			
			PairingStreamReader streamParser = new PairingStreamReader(pairing, in, inOff);
			
			Element C1 = streamParser.readG1Element();
			Element C2 = streamParser.readG1Element();
			
	  	int count=0;
	  	for (int i=0; i<ids.length; i++)	{	
	  		 if (count>=idsExclude.length)
	  			 throw new InvalidCipherTextException();
	  		 
	  		 if (!ids[i].equals(id))	{
	  			 idsExclude[count++] = ids[i];
	  		 }
	  	}
	  	
	  	ArrayList<Element> fi = computeFi(pairing.getZr().newOneElement().getImmutable(), idsExclude);
	  	Element inv_tmp1 = fi.remove(fi.size()-1).invert().getImmutable();
	  	Element hp1 = h.powZn(fi.get(fi.size()-1)).getImmutable();
	  	for (int i=1; i<fi.size(); i++)	{
 	 	  	Element tmp = publicKey.getMAt(i-1).powZn(fi.get(fi.size()-1-i)).getImmutable();
 	 	  	hp1 = hp1.mul(tmp).getImmutable();
 	 	  }
	  	 
	  	Element tmp4 = pairing.pairing(C1, hp1).getImmutable();
	  	Element tmp5 = pairing.pairing(skid, C2).getImmutable();
	  	Element K = tmp4.mul(tmp5).powZn(inv_tmp1).getImmutable();
	  	System.out.println("Decap K: " + K);
	  	
		 	return K.toBytes();
		}
	}	
	
}
