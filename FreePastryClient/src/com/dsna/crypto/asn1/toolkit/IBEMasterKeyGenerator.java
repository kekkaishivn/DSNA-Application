package com.dsna.crypto.asn1.toolkit;

import it.unisa.dia.gas.jpbc.Element;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

import com.dsna.crypto.asn1.exception.InvalidCertificateException;
import com.dsna.crypto.asn1.exception.UnsupportedFormatException;
import com.dsna.crypto.asn1.params.IBEClientSecretParams;
import com.dsna.crypto.asn1.params.IBEPublicParameters;
import com.dsna.crypto.asn1.params.IBESecretParameters;
import com.dsna.crypto.asn1.params.IBESysMasterSecretParams;
import com.dsna.crypto.asn1.params.IBESysPublicParams;
import com.dsna.crypto.ibbe.cd07.IBBECD07;
import com.dsna.crypto.ibbe.cd07.params.CD07DecryptionParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07SecretKeyParameters;
import com.dsna.crypto.signature.ps06.PS06;
import com.dsna.util.ASN1Util;
import com.dsna.util.FileUtil;

public class IBEMasterKeyGenerator {
	
	public static void main(String[] args) throws UnsupportedFormatException, IOException, InvalidCertificateException {
		
    PS06 ps06 = new PS06();

    // Setup -> (Public Key, Master Secret Key)
    AsymmetricCipherKeyPair ps06KeyPair = ps06.setup(ps06.createParameters(256, 256));

		IBBECD07 cd07 = new IBBECD07();
    // Setup
    AsymmetricCipherKeyPair cd07KeyPair = cd07.setup(300);
    
    IBEPublicParameters ps06PublicKey = (IBEPublicParameters)ASN1Util.toASN1Object(ps06KeyPair.getPublic());
    IBEPublicParameters cd07PublicKey = (IBEPublicParameters)ASN1Util.toASN1Object(cd07KeyPair.getPublic());
    IBESecretParameters ps06MasterSecretKey = (IBESecretParameters)ASN1Util.toASN1Object(ps06KeyPair.getPrivate());
    IBESecretParameters cd07MasterSecretKey = (IBESecretParameters)ASN1Util.toASN1Object(cd07KeyPair.getPrivate());
    
    Calendar cal = Calendar.getInstance();
    cal.set(2015, 5, 10);
    Date notBefore = new Date(System.currentTimeMillis());
    Date notAfter = cal.getTime();	    
    
    try {
			IBESysPublicParams sysPublicKeys = new IBESysPublicParams(1, "KTH - Mobile Service Lab", 10001, notBefore, notAfter, ps06PublicKey, cd07PublicKey);
			IBESysMasterSecretParams sysMasterSecretKeys = new IBESysMasterSecretParams(1, "KTH - Mobile Service Lab", 10001, notBefore, notAfter, ps06MasterSecretKey, cd07MasterSecretKey);
			CipherParameters[] publicKeys =  ASN1Util.extractPublicKey(sysPublicKeys);
			CipherParameters[] masterSecretKeys = ASN1Util.extractMasterSecretKey(sysMasterSecretKeys);
			AsymmetricCipherKeyPair cd07KeyPairDecoded = new AsymmetricCipherKeyPair(publicKeys[1], masterSecretKeys[1]);
			
	    CipherParameters cd07SecretKey = cd07.extract(cd07KeyPairDecoded, "letiendat3012@gmail.com");
	   
	    // Extract -> Secret Key for Identity "01001101"
	    CipherParameters ps06SecretKey = ps06.extract(ps06KeyPair, "letiendat3012@gmail.com");

	    IBESecretParameters ps06SecretKeyObject = (IBESecretParameters)ASN1Util.toASN1Object(ps06SecretKey);
	    IBESecretParameters cd07SecretKeyObject = (IBESecretParameters)ASN1Util.toASN1Object(cd07SecretKey);
	    IBEClientSecretParams clientSecretKeys = new IBEClientSecretParams(1, "KTH - Mobile Service Lab", 10001, notBefore, notAfter, ps06SecretKeyObject, cd07SecretKeyObject);

	    FileUtil.writeText(new FileOutputStream("MasterSystemSecret.txt"), ASN1Util.encode(sysMasterSecretKeys));
	    FileUtil.writeText(new FileOutputStream("SystemPublic.txt"), ASN1Util.encode(sysPublicKeys));
	    FileUtil.writeText(new FileOutputStream("Letiendat3012Secret.txt"), ASN1Util.encode(clientSecretKeys));
	    
		} catch (Exception e) {
			e.printStackTrace();
		}
    
}


}
