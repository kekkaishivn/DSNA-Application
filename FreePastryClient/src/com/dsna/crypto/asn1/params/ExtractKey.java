package com.dsna.crypto.asn1.params;

import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06KeyParameters;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

import com.dsna.crypto.asn1.exception.InvalidCertificateException;
import com.dsna.crypto.asn1.exception.UnsupportedFormatException;
import com.dsna.crypto.ibbe.cd07.IBBECD07;
import com.dsna.crypto.ibbe.cd07.params.CD07KeyParameters;
import com.dsna.crypto.signature.ps06.PS06;
import com.dsna.util.ASN1Util;
import com.dsna.util.FileUtil;

public class ExtractKey {

	public static void main(String[] args) throws Exception {
		
	Calendar cal = Calendar.getInstance();
    cal.set(2015, 5, 10);
    Date notBefore = new Date(System.currentTimeMillis());
    Date notAfter = cal.getTime();	    
   		
		
	String encodedSystemPublicParams = FileUtil.readString(new FileInputStream("SystemPublic.txt"));
	String encodedSystemMasterSecretParams = FileUtil.readString(new FileInputStream("MasterSystemSecret.txt"));
	
	CipherParameters[] publicKeys = ASN1Util.extractPublicKey(ASN1Util.decodeIBESysPublicParams(encodedSystemPublicParams));
	CipherParameters[] masterKeys = ASN1Util.extractMasterSecretKey(ASN1Util.decodeIBESysMasterSecretParams(encodedSystemMasterSecretParams));
	
	AsymmetricCipherKeyPair[] keyPairs = new AsymmetricCipherKeyPair[2];
	keyPairs[0] = new AsymmetricCipherKeyPair(publicKeys[0], masterKeys[0]);
	keyPairs[1] = new AsymmetricCipherKeyPair(publicKeys[1], masterKeys[1]);
	
	String clientId = "dsnatest1@gmail.com";
	clientId = clientId.toLowerCase();		
	
	PS06 ps06 = new PS06();
	IBBECD07 cd07 = new IBBECD07();		
	
	CipherParameters[] clientKeys = new CipherParameters[2];
	clientKeys[0] = ps06.extract(keyPairs[0], clientId);
	clientKeys[1] = cd07.extract(keyPairs[1], clientId);
	
	System.out.println(((PS06KeyParameters) publicKeys[0]).getParameters().getCurveParams());
	System.out.println(((CD07KeyParameters) publicKeys[1]).getParameters().getCurveParams());
	
	IBESecretParameters ps06SecretKeyObject = (IBESecretParameters)ASN1Util.toASN1Object(clientKeys[0]);
    IBESecretParameters cd07SecretKeyObject = (IBESecretParameters)ASN1Util.toASN1Object(clientKeys[1]);
    IBEClientSecretParams clientSecretKeys = new IBEClientSecretParams(1, "KTH - Mobile Service Lab", 10001, notBefore, notAfter, ps06SecretKeyObject, cd07SecretKeyObject);
    
    ASN1Util.extractClientSecretKey(clientSecretKeys, publicKeys);
    
    FileUtil.writeText(new FileOutputStream(clientId+"-Secret.txt"), ASN1Util.encode(clientSecretKeys));   
		
	}

}
