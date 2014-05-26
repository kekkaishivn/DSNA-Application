package com.dsna.crypto.asn1.params;

import java.util.Date;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;

import com.dsna.crypto.asn1.exception.InvalidCertificateException;

public class IBEClientSecretParams extends IBESysParams {
	
	private IBESecretParameters ps06SecretParameters;
	private IBESecretParameters cd07IBBESecretParameters;
	
	public IBEClientSecretParams(ASN1Sequence seq) throws InvalidCertificateException {
		super(seq);
		if (seq.getObjectAt(4) instanceof ASN1Sequence)	
			this.ps06SecretParameters = new IBESecretParameters((ASN1Sequence)seq.getObjectAt(4));
		
		if (seq.getObjectAt(5) instanceof ASN1Sequence)	
			this.cd07IBBESecretParameters = new IBESecretParameters((ASN1Sequence)seq.getObjectAt(5));
	}
	
	public IBEClientSecretParams(int version, String districtName, int districtSerial, Date notBefore, Date notAfter, 
			IBESecretParameters ps06SecretParameters, IBESecretParameters cd07IBBESecretParameters) 
			throws Exception	{
		super(version, districtName, districtSerial, notBefore, notAfter);
		this.ps06SecretParameters = ps06SecretParameters;
		this.cd07IBBESecretParameters = cd07IBBESecretParameters;
	}
	
	@Override
	public DERObject toASN1Object() {
		ASN1EncodableVector v = new ASN1EncodableVector();
		v.add(version);
		v.add(districtName);
		v.add(districtSerial);
		v.add(validity);
		v.add(ps06SecretParameters);
		v.add(cd07IBBESecretParameters);
		return new DERSequence(v);
	}
	
	public IBESecretParameters getPS06Parameter()	{
		return ps06SecretParameters;
	}
	
	public IBESecretParameters getCD07IBBEParameter()	{
		return cd07IBBESecretParameters;
	}

}
