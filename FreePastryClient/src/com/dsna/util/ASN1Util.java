package com.dsna.util;

import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06Parameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06PublicKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06SecretKeyParameters;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.crypto.CipherParameters;

import rice.p2p.util.Base64;

import com.dsna.crypto.asn1.exception.InvalidCertificateException;
import com.dsna.crypto.asn1.exception.UnsupportedFormatException;
import com.dsna.crypto.asn1.params.IBEParameter;
import com.dsna.crypto.asn1.params.IBEParameters;
import com.dsna.crypto.asn1.params.IBESecretParameters;
import com.dsna.crypto.asn1.params.IBEPublicParameters;
import com.dsna.crypto.asn1.params.IBESysPublicParams;
import com.dsna.crypto.asn1.params.IBESysSecretParams;
import com.dsna.crypto.ibbe.cd07.params.CD07KeyParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07Parameters;
import com.dsna.crypto.ibbe.cd07.params.CD07PublicKeyParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07SecretKeyParameters;

public class ASN1Util {
	
	public static IBEParameters toASN1Object(CipherParameters params) 
			throws UnsupportedFormatException	{
		if (params instanceof CD07PublicKeyParameters)
			return toASN1Object((CD07PublicKeyParameters) params);
		
		if (params instanceof CD07SecretKeyParameters)
			return toASN1Object((CD07SecretKeyParameters) params);
		
		if (params instanceof PS06PublicKeyParameters)
			return toASN1Object((PS06PublicKeyParameters) params);
		
		if (params instanceof PS06SecretKeyParameters)
			return toASN1Object((PS06SecretKeyParameters) params);
		
		throw new UnsupportedFormatException("Dont support this kind of param: " + params.getClass().toString());
	}
	
	public static String encode(CipherParameters params) 
			throws UnsupportedFormatException	{
		IBEParameters asn1Object = toASN1Object(params);
		return Base64.encodeBytes(asn1Object.getDEREncoded());
	}
	
	public static String encode(ASN1Encodable object)	{
		return Base64.encodeBytes(object.getDEREncoded());
	}
	
	public static IBESysPublicParams decodeIBESysPublicParams(String encodedString) throws IOException, InvalidCertificateException 	{
		DERSequence seq = (DERSequence)DERSequence.fromByteArray(Base64.decode(encodedString));
		IBESysPublicParams sysPublicParams = new IBESysPublicParams(seq);
		return sysPublicParams;
	}
	
	public static IBESysSecretParams decodeIBESysSecretParams(String encodedString) throws IOException, InvalidCertificateException	{
		DERSequence seq = (DERSequence)DERSequence.fromByteArray(Base64.decode(encodedString));
		IBESysSecretParams sysSecretParams = new IBESysSecretParams(seq);
		return sysSecretParams;
	}
	
	public static IBEParameters toASN1Object(CD07PublicKeyParameters cd07PublicKey)	{
		ArrayList<IBEParameter> publicParams = new ArrayList<IBEParameter>();
		CD07Parameters params = cd07PublicKey.getParameters();
		String algorithmOid = CD07KeyParameters.algorithmOid;
		int nM = params.getnM();
		publicParams.add(IBEParameter.fromString(algorithmOid, params.getCurveParams().toString()));
		publicParams.add(IBEParameter.fromInt(algorithmOid, nM));
		publicParams.add(IBEParameter.fromElement(algorithmOid, cd07PublicKey.getOmega()));
		publicParams.add(IBEParameter.fromElement(algorithmOid, cd07PublicKey.getV()));
		publicParams.add(IBEParameter.fromElement(algorithmOid, cd07PublicKey.getH()));
		publicParams.add(IBEParameter.fromElement(algorithmOid, cd07PublicKey.getTheta()));
		for (int i=0; i<nM; i++)
			publicParams.add(IBEParameter.fromElement(algorithmOid, cd07PublicKey.getMAt(i)));
		return new IBEPublicParameters(publicParams);
	}	
	
	public static IBEParameters toASN1Object(CD07SecretKeyParameters cd07SecretKey)	{
		ArrayList<IBEParameter> secretParams = new ArrayList<IBEParameter>();
		CD07Parameters params = cd07SecretKey.getParameters();
		String algorithmOid = CD07KeyParameters.algorithmOid;
		secretParams.add(IBEParameter.fromString(algorithmOid, params.getCurveParams().toString()));
		secretParams.add(IBEParameter.fromElement(algorithmOid, cd07SecretKey.getIdentity()));
		secretParams.add(IBEParameter.fromElement(algorithmOid, cd07SecretKey.getSkid()));
		return new IBESecretParameters(secretParams);
	}	
	
	public static IBEParameters toASN1Object(PS06PublicKeyParameters ps06PublicKey)	{
		ArrayList<IBEParameter> ps06PublicParameters = new ArrayList<IBEParameter>();
		PS06Parameters params = ps06PublicKey.getParameters();
		Element g = params.getG();
		int nU = params.getnU();
		int nM = params.getnM();
		String algorithmOid = "2.16.840.1.114334.1.2.1.5.1";
		ps06PublicParameters.add(IBEParameter.fromString(algorithmOid, params.getCurveParams().toString()));
		ps06PublicParameters.add(IBEParameter.fromElement(algorithmOid, g));
		ps06PublicParameters.add(IBEParameter.fromInt(algorithmOid, nU));
		ps06PublicParameters.add(IBEParameter.fromInt(algorithmOid, nM));
		ps06PublicParameters.add(IBEParameter.fromElement(algorithmOid, ps06PublicKey.getG1()));
		ps06PublicParameters.add(IBEParameter.fromElement(algorithmOid, ps06PublicKey.getG2()));
		ps06PublicParameters.add(IBEParameter.fromElement(algorithmOid, ps06PublicKey.getuPrime()));
		ps06PublicParameters.add(IBEParameter.fromElement(algorithmOid, ps06PublicKey.getmPrime()));

		for (int i=0; i<nU; i++)
			ps06PublicParameters.add(IBEParameter.fromElement(algorithmOid, ps06PublicKey.getUAt(i)));
		for (int i=0; i<nM; i++)
			ps06PublicParameters.add(IBEParameter.fromElement(algorithmOid, ps06PublicKey.getMAt(i)));	
		return new IBEPublicParameters(ps06PublicParameters);
	}
	
	public static IBEParameters toASN1Object(PS06SecretKeyParameters ps06SecretKey)	{
		ArrayList<IBEParameter> ps06PrivateParameters = new ArrayList<IBEParameter>();
		PS06Parameters params = ps06SecretKey.getParameters();
		String algorithmOid = "2.16.840.1.114334.1.2.1.5.1";
		
		ps06PrivateParameters.add(IBEParameter.fromString(algorithmOid, params.getCurveParams().toString()));
		ps06PrivateParameters.add(IBEParameter.fromString(algorithmOid, ps06SecretKey.getIdentity()));
		ps06PrivateParameters.add(IBEParameter.fromElement(algorithmOid, ps06SecretKey.getD1()));
		ps06PrivateParameters.add(IBEParameter.fromElement(algorithmOid, ps06SecretKey.getD2()));

		return new IBESecretParameters(ps06PrivateParameters);
	}
	
	public static PS06PublicKeyParameters decodePs06PublicParameters(String encodedString) throws Exception	{
		DERSequence seq = (DERSequence)DERSequence.fromByteArray(Base64.decode(encodedString));
		IBEPublicParameters ps06PublicParams = new IBEPublicParameters(seq);
		return toPs06PublicKey(ps06PublicParams);
	}
	
	public static PS06PublicKeyParameters toPs06PublicKey(IBEPublicParameters ps06PublicParams)	{
		
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
	
	public static CipherParameters[] extractPublicKey(IBESysPublicParams certificate)
		throws InvalidCertificateException {
		if (!certificate.isValidPeriod(new Date(System.currentTimeMillis())))	
			throw new InvalidCertificateException("Certificate is expired");
		
		CipherParameters[] publicKeys = new CipherParameters[2];
		publicKeys[0] = toPs06PublicKey(certificate.getPs06Parameter());
		publicKeys[1] = toCD07PublicKey(certificate.getCD07IBBEParameter());
		return publicKeys;
	}
	
	public static CipherParameters[] extractSecretKey(IBESysSecretParams certificate, CipherParameters[] publicKeys)
			throws InvalidCertificateException, UnsupportedFormatException {
			if (!certificate.isValidPeriod(new Date(System.currentTimeMillis())))	
				throw new InvalidCertificateException("Certificate is expired");
			
			if (!(publicKeys[0] instanceof PS06PublicKeyParameters))
				throw new UnsupportedFormatException("Need PS06PublicKeyParameters instead");
			
			if (!(publicKeys[1] instanceof CD07PublicKeyParameters))
				throw new UnsupportedFormatException("Need CD07PublicKeyParameters instead");	
			
			CipherParameters[] secretKeys = new CipherParameters[2];
			secretKeys[0] = toPs06SecretKey(certificate.getPs06Parameter(), (PS06PublicKeyParameters) publicKeys[0]);
			secretKeys[1] = toCD07SecretKey(certificate.getCD07IBBEParameter(), (CD07PublicKeyParameters) publicKeys[1]);
			return secretKeys;
		}
	
	public static PS06SecretKeyParameters toPs06SecretKey(IBESecretParameters ps06SecretParams, PS06PublicKeyParameters publicKey) throws UnsupportedFormatException	{
		String curveParamDescription = new String(ps06SecretParams.getElementAt(0).getRawData());
		PropertiesParameters curveParams = new PropertiesParameters();
		curveParams.load(new ByteArrayInputStream(curveParamDescription.getBytes()));
		if (!curveParams.equals(publicKey.getParameters().getCurveParams()))
			throw new UnsupportedFormatException("Inconsistent curve params between public and private params");
		Pairing pairing = PairingFactory.getPairing(curveParams);
		 
		String identity = new String(ps06SecretParams.getElementAt(1).getRawData());
		Element D1 = pairing.getG1().newElementFromBytes(ps06SecretParams.getElementAt(2).getRawData());
		Element D2 = pairing.getG1().newElementFromBytes(ps06SecretParams.getElementAt(3).getRawData());
		
		return new PS06SecretKeyParameters(publicKey, identity, D1, D2);
	}
	
	public static CD07PublicKeyParameters toCD07PublicKey(IBEPublicParameters cd07PublicParams) throws InvalidCertificateException	{
		int index = 0;

		String curveParamDescription = new String(cd07PublicParams.getElementAt(index++).getRawData());
		PropertiesParameters curveParams = new PropertiesParameters();
		curveParams.load(new ByteArrayInputStream(curveParamDescription.getBytes()));
		Pairing pairing = PairingFactory.getPairing(curveParams);
		
		int nM = new BigInteger(cd07PublicParams.getElementAt(index++).getRawData()).intValue();
		
		CD07Parameters cd07Parameters = new CD07Parameters(curveParams, nM);
		
		Element omega = pairing.getG1().newElementFromBytes(cd07PublicParams.getElementAt(index++).getRawData());
		Element v = pairing.getGT().newElementFromBytes(cd07PublicParams.getElementAt(index++).getRawData());
		Element h = pairing.getG2().newElementFromBytes(cd07PublicParams.getElementAt(index++).getRawData());
		Element theta = pairing.getZr().newElementFromBytes(cd07PublicParams.getElementAt(index++).getRawData());
		
		Element[] Ms = new Element[nM];
		for (int j = 0; j < Ms.length; j++)	{
			Ms[j] = pairing.getG2().newElementFromBytes(cd07PublicParams.getElementAt(index++).getRawData());
		}
		return new CD07PublicKeyParameters(cd07Parameters, omega, v, h, theta, Ms);
	}
	
	public static CD07SecretKeyParameters toCD07SecretKey(IBEParameters cd07SecretParams, CD07PublicKeyParameters publicKey) throws UnsupportedFormatException	{
		
		String curveParamDescription = new String(cd07SecretParams.getElementAt(0).getRawData());
		PropertiesParameters curveParams = new PropertiesParameters();
		curveParams.load(new ByteArrayInputStream(curveParamDescription.getBytes()));
		
		if (!curveParams.equals(publicKey.getParameters().getCurveParams()))
			throw new UnsupportedFormatException("Inconsistent curve params between public and private params");
		
		Pairing pairing = PairingFactory.getPairing(curveParams);
		
		Element id = pairing.getZr().newElementFromBytes(cd07SecretParams.getElementAt(1).getRawData());
		Element skid = pairing.getG1().newElementFromBytes(cd07SecretParams.getElementAt(2).getRawData());
		
		return new CD07SecretKeyParameters(publicKey, id, skid);
	}
	
	public static PS06SecretKeyParameters decodePs06SecretParameters(String encodedString, PS06PublicKeyParameters publicKey) throws Exception	{
		DERSequence seq = (DERSequence)DERSequence.fromByteArray(Base64.decode(encodedString));
		IBESecretParameters ps06SecretParams = new IBESecretParameters(seq);
		return toPs06SecretKey(ps06SecretParams, publicKey);
	}
	
	public static CD07SecretKeyParameters decodeCD07SecretParameters(String encodedString, CD07PublicKeyParameters publicKey) 
			throws UnsupportedFormatException, IOException	{
		DERSequence seq = (DERSequence)DERSequence.fromByteArray(Base64.decode(encodedString));

		IBEParameters cd07SecretParams = new IBESecretParameters(seq);
		
		return toCD07SecretKey(cd07SecretParams, publicKey);
	}	
	
	public static CD07PublicKeyParameters decodeCD07PublicParameters(String encodedString) throws InvalidCertificateException, IOException	{
		DERSequence seq = (DERSequence)DERSequence.fromByteArray(Base64.decode(encodedString));
		IBEPublicParameters cd07PublicParams = new IBEPublicParameters(seq);
		return toCD07PublicKey(cd07PublicParams);
	}

}
