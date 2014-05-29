package com.dsna.util;

import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06MasterSecretKeyParameters;
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
import com.dsna.crypto.asn1.params.IBESysMasterSecretParams;
import com.dsna.crypto.asn1.params.IBESysPublicParams;
import com.dsna.crypto.asn1.params.IBEClientSecretParams;
import com.dsna.crypto.ibbe.cd07.params.CD07KeyParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07MasterSecretKeyParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07Parameters;
import com.dsna.crypto.ibbe.cd07.params.CD07PublicKeyParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07SecretKeyParameters;

public class ASN1Util {
	
	public static final String PS06AlgorithmOid = "2.16.840.1.114334.1.2.1.5.1";
	
	public static IBEParameters toASN1Object(CipherParameters params) 
			throws UnsupportedFormatException	{
		if (params instanceof CD07PublicKeyParameters)
			return toASN1Object((CD07PublicKeyParameters) params);
		
		if (params instanceof CD07SecretKeyParameters)
			return toASN1Object((CD07SecretKeyParameters) params);
		
		if (params instanceof CD07MasterSecretKeyParameters)
			return toASN1Object((CD07MasterSecretKeyParameters) params);
		
		if (params instanceof PS06PublicKeyParameters)
			return toASN1Object((PS06PublicKeyParameters) params);
		
		if (params instanceof PS06SecretKeyParameters)
			return toASN1Object((PS06SecretKeyParameters) params);
		
		if (params instanceof PS06MasterSecretKeyParameters)
			return toASN1Object((PS06MasterSecretKeyParameters) params);
		
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
	
	public static IBEClientSecretParams decodeIBEClientSecretParams(String encodedString) throws IOException, InvalidCertificateException	{
		DERSequence seq = (DERSequence)DERSequence.fromByteArray(Base64.decode(encodedString));
		IBEClientSecretParams sysSecretParams = new IBEClientSecretParams(seq);
		return sysSecretParams;
	}
	
	public static IBESysMasterSecretParams decodeIBESysMasterSecretParams(String encodedString) throws IOException, InvalidCertificateException	{
		DERSequence seq = (DERSequence)DERSequence.fromByteArray(Base64.decode(encodedString));
		IBESysMasterSecretParams sysMasterSecretParams = new IBESysMasterSecretParams(seq);
		return sysMasterSecretParams;
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
	
	public static IBEParameters toASN1Object(CD07MasterSecretKeyParameters cd07MasterSecretKey)	{
		ArrayList<IBEParameter> secretParams = new ArrayList<IBEParameter>();
		CD07Parameters params = cd07MasterSecretKey.getParameters();
		int nM = params.getnM();
		String algorithmOid = CD07KeyParameters.algorithmOid;
		secretParams.add(IBEParameter.fromString(algorithmOid, params.getCurveParams().toString()));
		secretParams.add(IBEParameter.fromInt(algorithmOid, nM));
		secretParams.add(IBEParameter.fromElement(algorithmOid, cd07MasterSecretKey.getG()));
		secretParams.add(IBEParameter.fromElement(algorithmOid, cd07MasterSecretKey.getTheta()));
		return new IBESecretParameters(secretParams);
	}	
	
	public static IBEParameters toASN1Object(PS06MasterSecretKeyParameters ps06MasterSecretKey)	{
		ArrayList<IBEParameter> masterSecretParams = new ArrayList<IBEParameter>();
		PS06Parameters params = ps06MasterSecretKey.getParameters();
		Element g = params.getG();
		int nU = params.getnU();
		int nM = params.getnM();
		
		String algorithmOid = PS06AlgorithmOid;
		masterSecretParams.add(IBEParameter.fromString(algorithmOid, params.getCurveParams().toString()));
		masterSecretParams.add(IBEParameter.fromElement(algorithmOid, g));
		masterSecretParams.add(IBEParameter.fromInt(algorithmOid, nU));
		masterSecretParams.add(IBEParameter.fromInt(algorithmOid, nM));
		masterSecretParams.add(IBEParameter.fromElement(algorithmOid, ps06MasterSecretKey.getMsk()));
		return new IBESecretParameters(masterSecretParams);
	}		
	
	public static IBEParameters toASN1Object(PS06PublicKeyParameters ps06PublicKey)	{
		ArrayList<IBEParameter> ps06PublicParameters = new ArrayList<IBEParameter>();
		PS06Parameters params = ps06PublicKey.getParameters();
		Element g = params.getG();
		int nU = params.getnU();
		int nM = params.getnM();
		String algorithmOid = PS06AlgorithmOid;
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
		String algorithmOid = PS06AlgorithmOid;
		
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
	
public static PS06MasterSecretKeyParameters toPs06MasterSecretKey(IBEParameters ps06MasterSecretParams) throws UnsupportedFormatException	{
		
		if (!(ps06MasterSecretParams instanceof IBESecretParameters))
			throw new UnsupportedFormatException("Required IBESecretParameters ");
		
		String curveParamDescription = new String(ps06MasterSecretParams.getElementAt(0).getRawData());
		PropertiesParameters curveParams = new PropertiesParameters();
		curveParams.load(new ByteArrayInputStream(curveParamDescription.getBytes()));
		Pairing pairing = PairingFactory.getPairing(curveParams);
		
		Element g = pairing.getG1().newElementFromBytes(ps06MasterSecretParams.getElementAt(1).getRawData()).getImmutable();
		int nU = new BigInteger(ps06MasterSecretParams.getElementAt(2).getRawData()).intValue();
		int nM = new BigInteger(ps06MasterSecretParams.getElementAt(3).getRawData()).intValue();
		
		PS06Parameters ps06Parameters = new PS06Parameters(curveParams, g, nU, nM);
	
		Element msk = pairing.getG1().newElementFromBytes(ps06MasterSecretParams.getElementAt(4).getRawData());
		
		return new PS06MasterSecretKeyParameters(ps06Parameters, msk.getImmutable());
	}
	
	public static CipherParameters[] extractPublicKey(IBESysPublicParams certificate)
		throws InvalidCertificateException {
		if (!certificate.isValidPeriod(new Date(System.currentTimeMillis())))	
			throw new InvalidCertificateException("Certificate is expired");
		
		CipherParameters[] publicKeys = new CipherParameters[2];
		publicKeys[0] = toPs06PublicKey(certificate.getPS06Parameter());
		publicKeys[1] = toCd07PublicKey(certificate.getCD07IBBEParameter());
		return publicKeys;
	}
	
	public static CipherParameters[] extractSecretKey(IBEClientSecretParams certificate, CipherParameters[] publicKeys)
			throws InvalidCertificateException, UnsupportedFormatException {
			if (!certificate.isValidPeriod(new Date(System.currentTimeMillis())))	
				throw new InvalidCertificateException("Certificate is expired");
			
			if (!(publicKeys[0] instanceof PS06PublicKeyParameters))
				throw new UnsupportedFormatException("Need PS06PublicKeyParameters instead");
			
			if (!(publicKeys[1] instanceof CD07PublicKeyParameters))
				throw new UnsupportedFormatException("Need CD07PublicKeyParameters instead");	
			
			CipherParameters[] secretKeys = new CipherParameters[2];
			secretKeys[0] = toPs06SecretKey(certificate.getPS06Parameter(), (PS06PublicKeyParameters) publicKeys[0]);
			secretKeys[1] = toCd07SecretKey(certificate.getCD07IBBEParameter(), (CD07PublicKeyParameters) publicKeys[1]);
			return secretKeys;
		}
	
	public static CipherParameters[] extractMasterSecretKey(IBESysMasterSecretParams certificate)
			throws InvalidCertificateException, UnsupportedFormatException {
			if (!certificate.isValidPeriod(new Date(System.currentTimeMillis())))	
				throw new InvalidCertificateException("Certificate is expired");
			
			CipherParameters[] masterSecretKeys = new CipherParameters[2];
			masterSecretKeys[0] = toPs06MasterSecretKey(certificate.getPS06MasterSecretParams());
			masterSecretKeys[1] = toCd07MasterSecretKey(certificate.getCD07MasterSecretParams());
			return masterSecretKeys;
	}
	
	public static CipherParameters[] extractClientSecretKey(IBEClientSecretParams certificate, CipherParameters[] publicKeys)
			throws InvalidCertificateException, UnsupportedFormatException {
			if (!certificate.isValidPeriod(new Date(System.currentTimeMillis())))	
				throw new InvalidCertificateException("Certificate is expired");
			
			CipherParameters[] clientSecretKeys = new CipherParameters[2];
			clientSecretKeys[0] = toPs06SecretKey(certificate.getPS06Parameter(), (PS06PublicKeyParameters)publicKeys[0]);
			clientSecretKeys[1] = toCd07SecretKey(certificate.getCD07IBBEParameter(), (CD07PublicKeyParameters)publicKeys[1]);
			return clientSecretKeys;
	}
	
public static PS06PublicKeyParameters toPs06PublicKey(IBEPublicParameters ps06PublicParams)	{
		
		String curveParamDescription = new String(ps06PublicParams.getElementAt(0).getRawData());
		PropertiesParameters curveParams = new PropertiesParameters();
		curveParams.load(new ByteArrayInputStream(curveParamDescription.getBytes()));
		Pairing pairing = PairingFactory.getPairing(curveParams);
		
		Element g = pairing.getG1().newElementFromBytes(ps06PublicParams.getElementAt(1).getRawData()).getImmutable();
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
	
	public static PS06SecretKeyParameters toPs06SecretKey(IBESecretParameters ps06SecretParams, PS06PublicKeyParameters publicKey) throws UnsupportedFormatException	{
		String curveParamDescription = new String(ps06SecretParams.getElementAt(0).getRawData());
		PropertiesParameters curveParams = new PropertiesParameters();
		curveParams.load(new ByteArrayInputStream(curveParamDescription.getBytes()));
		if (!curveParams.equals(publicKey.getParameters().getCurveParams()))	{
			System.out.println(curveParams);
			System.out.println(publicKey.getParameters().getCurveParams());
			throw new UnsupportedFormatException("Inconsistent curve params between public and private params");
		}
		Pairing pairing = PairingFactory.getPairing(curveParams);
		 
		String identity = new String(ps06SecretParams.getElementAt(1).getRawData());
		Element D1 = pairing.getG1().newElementFromBytes(ps06SecretParams.getElementAt(2).getRawData());
		Element D2 = pairing.getG1().newElementFromBytes(ps06SecretParams.getElementAt(3).getRawData());
		
		return new PS06SecretKeyParameters(publicKey, identity, D1, D2);
	}
	
	public static CD07PublicKeyParameters toCd07PublicKey(IBEPublicParameters cd07PublicParams) throws InvalidCertificateException	{
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
	
		Element[] Ms = new Element[nM];
		for (int j = 0; j < Ms.length; j++)	{
			Ms[j] = pairing.getG2().newElementFromBytes(cd07PublicParams.getElementAt(index++).getRawData());
		}
		return new CD07PublicKeyParameters(cd07Parameters, omega, v, h, Ms);
	}
	
	public static CD07SecretKeyParameters toCd07SecretKey(IBEParameters cd07SecretParams, CD07PublicKeyParameters publicKey) throws UnsupportedFormatException	{
		
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
	
	public static CD07MasterSecretKeyParameters toCd07MasterSecretKey(IBEParameters cd07MasterSecretParams) throws UnsupportedFormatException	{
		
		if (!(cd07MasterSecretParams instanceof IBESecretParameters))
			throw new UnsupportedFormatException("Required IBESecretParameters ");
		
		String curveParamDescription = new String(cd07MasterSecretParams.getElementAt(0).getRawData());
		PropertiesParameters curveParams = new PropertiesParameters();
		curveParams.load(new ByteArrayInputStream(curveParamDescription.getBytes()));
		
		int nM = new BigInteger(cd07MasterSecretParams.getElementAt(1).getRawData()).intValue();
		
		CD07Parameters cd07Parameters = new CD07Parameters(curveParams, nM);
		
		Pairing pairing = PairingFactory.getPairing(curveParams);
		
		Element g = pairing.getG1().newElementFromBytes(cd07MasterSecretParams.getElementAt(2).getRawData());
		Element theta = pairing.getZr().newElementFromBytes(cd07MasterSecretParams.getElementAt(3).getRawData());
		
		return new CD07MasterSecretKeyParameters(cd07Parameters, g, theta);
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
		
		return toCd07SecretKey(cd07SecretParams, publicKey);
	}	
	
	public static CD07PublicKeyParameters decodeCD07PublicParameters(String encodedString) throws InvalidCertificateException, IOException	{
		DERSequence seq = (DERSequence)DERSequence.fromByteArray(Base64.decode(encodedString));
		IBEPublicParameters cd07PublicParams = new IBEPublicParameters(seq);
		return toCd07PublicKey(cd07PublicParams);
	}
	
	public static CD07MasterSecretKeyParameters decodeCD07MasterSecretKey(String encodedString) throws UnsupportedFormatException, IOException	{	
		DERSequence seq = (DERSequence)DERSequence.fromByteArray(Base64.decode(encodedString));
		IBESecretParameters cd07MasterSecretParams = new IBESecretParameters(seq);	
		return toCd07MasterSecretKey(cd07MasterSecretParams);
	}

}
