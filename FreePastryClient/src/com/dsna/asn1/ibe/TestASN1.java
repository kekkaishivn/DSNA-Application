package com.dsna.asn1.ibe;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SHA256Digest;

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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	// Setup -> (Public Key, Master Secret Key)
    AsymmetricCipherKeyPair keyPair = setup(createParameters(256, 256));
    PS06PublicKeyParameters ps06PublicKey = (PS06PublicKeyParameters)keyPair.getPublic();
    IBEPublicParameters asn1Ps06PublicKey = encodePs06PublicParameters(ps06PublicKey);
    //System.out.println(Base64.encodeBytes(asn1Ps06PublicKey.getDEREncoded()));
 // Extract -> Secret Key for Identity "01001101"
    String id1 = new BigInteger(doHash("letiendat3012@gmail.com")).toString(2);
    String id2 = new BigInteger(doHash("letiendat30@gmail.com")).toString(2);
    
    CipherParameters secretKey = extract(keyPair, id1);

    // Sign
    String message = "Hi there, hello there, hehehee";
    byte[] signature = sign(message, secretKey);    
    try	{
    	PS06PublicKeyParameters decodedPublicKey = decodePs06PublicParameters(Base64.encodeBytes(asn1Ps06PublicKey.getDEREncoded()));
   // verify with the original public key
      System.out.println(verify(keyPair.getPublic(), message, id1, signature));
   // verify with the decoded identity
      System.out.println(verify(decodedPublicKey, message, id2, signature));
    } catch (Exception e)	{
    	e.printStackTrace();
    }
    //ps06KeyPair.
	}
	
	public static PS06PublicKeyParameters decodePs06PublicParameters(String encodedString) throws Exception	{
		DERSequence seq = (DERSequence)DERSequence.fromByteArray(Base64.decode(encodedString));
		//System.out.println(seq.toString());
		IBEPublicParameters ps06PublicParams = new IBEPublicParameters(seq);
		String curveParamDescription = new String(ps06PublicParams.getElementAt(0).getRawData());
		PropertiesParameters curveParams = new PropertiesParameters();
		curveParams.load(new ByteArrayInputStream(curveParamDescription.getBytes()));
		Pairing pairing = PairingFactory.getPairing(curveParams);
		
		Element g = pairing.getG1().newElementFromBytes(ps06PublicParams.getElementAt(1).getRawData());
		int nU = new BigInteger(ps06PublicParams.getElementAt(2).getRawData()).intValue();
		int nM = new BigInteger(ps06PublicParams.getElementAt(3).getRawData()).intValue();
		
		PS06Parameters ps06Parameters = new PS06Parameters(curveParams, g, nU, nM);
		
		Element g1 = pairing.getG1().newElementFromBytes(ps06PublicParams.getElementAt(4).getRawData());
		Element g2 = pairing.getG1().newElementFromBytes(ps06PublicParams.getElementAt(5).getRawData());
		Element uPrime = pairing.getG1().newElementFromBytes(ps06PublicParams.getElementAt(6).getRawData());
		Element mPrime = pairing.getG1().newElementFromBytes(ps06PublicParams.getElementAt(7).getRawData());
		
		Element[] Us = new Element[nU];
		for (int i = 0; i < Us.length; i++) {
      Us[i] = pairing.getG1().newElementFromBytes(ps06PublicParams.getElementAt(i+8).getRawData());
		}
		
		Element[] Ms = new Element[nM];
		for (int j = 0; j < Ms.length; j++)	{
			Ms[j] = pairing.getG2().newElementFromBytes(ps06PublicParams.getElementAt(j+nU+8).getRawData());
		}
		return new PS06PublicKeyParameters(ps06Parameters, g1, g2, uPrime, mPrime, Us, Ms);
	}
	
	public static IBEPublicParameters encodePs06PublicParameters(PS06PublicKeyParameters ps06PublicKey)	{
		ArrayList<IBEPublicParameter> ps06PublicParameters = new ArrayList<IBEPublicParameter>();
		PS06Parameters params = ps06PublicKey.getParameters();
		Element g = params.getG();
		System.out.println(Base64.encodeBytes(g.toBytes()));
		int nU = params.getnU();
		int nM = params.getnM();
		String algorithmOid = "2.16.840.1.114334.1.2.1.5.1";
		ps06PublicParameters.add(encodeString(algorithmOid, params.getCurveParams().toString()));
		ps06PublicParameters.add(encodeElement(algorithmOid, g));
		ps06PublicParameters.add(encodeInt(algorithmOid, nU));
		ps06PublicParameters.add(encodeInt(algorithmOid, nM));
		ps06PublicParameters.add(encodeElement(algorithmOid, ps06PublicKey.getG1()));
		ps06PublicParameters.add(encodeElement(algorithmOid, ps06PublicKey.getG2()));
		ps06PublicParameters.add(encodeElement(algorithmOid, ps06PublicKey.getuPrime()));
		ps06PublicParameters.add(encodeElement(algorithmOid, ps06PublicKey.getmPrime()));

		for (int i=0; i<nU; i++)
			ps06PublicParameters.add(encodeElement(algorithmOid, ps06PublicKey.getUAt(i)));
		for (int i=0; i<nM; i++)
			ps06PublicParameters.add(encodeElement(algorithmOid, ps06PublicKey.getMAt(i)));
		return new IBEPublicParameters(ps06PublicParameters);
	}
	
	public static IBEPublicParameter encodeElement(String oid, Element e)	{
		return new IBEPublicParameter(oid, e.toBytes());
	}
	
	public static IBEPublicParameter encodeString(String oid, String s)	{
		return new IBEPublicParameter(oid, s.getBytes());
	}
	
	public static IBEPublicParameter encodeInt(String oid, int n)	{
		return new IBEPublicParameter(oid, ByteBuffer.allocate(4).putInt(n).array());
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
