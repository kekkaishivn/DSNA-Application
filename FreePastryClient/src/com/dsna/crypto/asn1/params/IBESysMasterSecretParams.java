package com.dsna.crypto.asn1.params;

import java.util.Date;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;

import com.dsna.crypto.asn1.exception.InvalidCertificateException;

public class IBESysMasterSecretParams extends IBESysParams {
	
	private IBESecretParameters ps06MasterSecretParameters;
	private IBESecretParameters cd07IBBEMasterSecretParameters;
	
	public IBESysMasterSecretParams(ASN1Sequence seq) throws InvalidCertificateException {
		super(seq);
		if (seq.getObjectAt(4) instanceof ASN1Sequence)	
			this.ps06MasterSecretParameters = new IBESecretParameters((ASN1Sequence)seq.getObjectAt(4));
		
		if (seq.getObjectAt(5) instanceof ASN1Sequence)	
			this.cd07IBBEMasterSecretParameters = new IBESecretParameters((ASN1Sequence)seq.getObjectAt(5));
	}
	
	public IBESysMasterSecretParams(int version, String districtName, int districtSerial, Date notBefore, Date notAfter, 
			IBESecretParameters ps06SecretParameters, IBESecretParameters cd07IBBESecretParameters) 
			throws Exception	{
		super(version, districtName, districtSerial, notBefore, notAfter);
		this.ps06MasterSecretParameters = ps06SecretParameters;
		this.cd07IBBEMasterSecretParameters = cd07IBBESecretParameters;
	}
	
	@Override
	public DERObject toASN1Object() {
		ASN1EncodableVector v = new ASN1EncodableVector();
		v.add(version);
		v.add(districtName);
		v.add(districtSerial);
		v.add(validity);
		v.add(ps06MasterSecretParameters);
		v.add(cd07IBBEMasterSecretParameters);
		return new DERSequence(v);
	}
	
	public IBESecretParameters getPS06MasterSecretParams()	{
		return ps06MasterSecretParameters;
	}
	
	public IBESecretParameters getCD07MasterSecretParams()	{
		return cd07IBBEMasterSecretParameters;
	}	

}
