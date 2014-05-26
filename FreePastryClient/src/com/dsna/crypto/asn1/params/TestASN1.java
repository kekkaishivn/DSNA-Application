package com.dsna.crypto.asn1.params;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;

import com.dsna.crypto.asn1.exception.InvalidCertificateException;
import com.dsna.crypto.asn1.exception.UnsupportedFormatException;
import com.dsna.crypto.ibbe.cd07.IBBECD07;
import com.dsna.crypto.ibbe.cd07.params.CD07DecryptionParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07SecretKeyParameters;
import com.dsna.crypto.signature.ps06.PS06;
import com.dsna.util.ASN1Util;
import com.dsna.util.FileUtil;

import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06MasterSecretKeyParameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06SecretKeyParameters;
import it.unisa.dia.gas.jpbc.Element;

public class TestASN1 {

	public static void main(String[] args) throws UnsupportedFormatException, IOException, InvalidCertificateException {
		
	    PS06 ps06 = new PS06();
	
	    // Setup -> (Public Key, Master Secret Key)
	    AsymmetricCipherKeyPair ps06KeyPair = ps06.setup(ps06.createParameters(256, 256));
	
			IBBECD07 cd07 = new IBBECD07();
	    // Setup
	    AsymmetricCipherKeyPair cd07KeyPair = cd07.setup(300);
	    
	    String[] idsName = {"letiendat3012@gmail.com", "halo@yahoo.com", "tdle@vnu.edu.vn", "haivl@yahoo.com", "chanchet@haha.com", "Compute@hichic.com"};
	    String encodedPublicKey = ASN1Util.encode(cd07KeyPair.getPublic());
	    
	    Element[] cd07Ids = cd07.map(cd07KeyPair.getPublic(), idsName);
    
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
		    CipherParameters cd07DecryptionKey = new CD07DecryptionParameters((CD07SecretKeyParameters)cd07SecretKey, cd07Ids);

		    // Encryption/Decryption
		    byte[][] ciphertext = cd07.encaps(ASN1Util.decodeCD07PublicParameters(encodedPublicKey), cd07Ids);
		    byte[] key = cd07.decaps(cd07DecryptionKey, ciphertext[1]);			    
		    
		    AsymmetricCipherKeyPair ps06KeyPairDecoded = new AsymmetricCipherKeyPair(publicKeys[0], masterSecretKeys[0]);
		    // Extract -> Secret Key for Identity "01001101"
		    CipherParameters ps06SecretKey = ps06.extract(ps06KeyPair, "letiendat3012@gmail.com");
		    CipherParameters ps06SecretKeyDecoded = ps06.extract(ps06KeyPairDecoded, "letiendat3012@gmail.com");
    
		    // Sign
		    String message = "Hello World!!!";
		    byte[] signature = ps06.sign(message, ps06SecretKeyDecoded);		
		    System.out.println(ps06.verify(ps06KeyPairDecoded.getPublic(), message, "letiendat3012@gmail.com", signature));
				
		    IBESecretParameters ps06SecretKeyObject = (IBESecretParameters)ASN1Util.toASN1Object(ps06SecretKey);
		    IBESecretParameters cd07SecretKeyObject = (IBESecretParameters)ASN1Util.toASN1Object(cd07SecretKey);
		    IBEClientSecretParams clientSecretKeys = new IBEClientSecretParams(1, "KTH - Mobile Service Lab", 10001, notBefore, notAfter, ps06SecretKeyObject, cd07SecretKeyObject);
		    
		    CipherParameters[] clientSecretParams = ASN1Util.extractClientSecretKey(clientSecretKeys, publicKeys);
		    signature = ps06.sign(message, clientSecretParams[0]);
		    System.out.println(ps06.verify(publicKeys[0], message, "letiendat3012@gmail.com", signature));
						    
		    cd07.decaps(new CD07DecryptionParameters((CD07SecretKeyParameters)clientSecretParams[1], cd07Ids), ciphertext[1]);		
		    
		    FileUtil.writeText(new FileOutputStream("MasterSystemSecret.txt"), ASN1Util.encode(sysMasterSecretKeys));
		    FileUtil.writeText(new FileOutputStream("SystemPublic.txt"), ASN1Util.encode(sysPublicKeys));
		    FileUtil.writeText(new FileOutputStream("Letiendat3012Secret.txt"), ASN1Util.encode(clientSecretKeys));
		    
			} catch (Exception e) {
				e.printStackTrace();
			}
	    
	}

}
