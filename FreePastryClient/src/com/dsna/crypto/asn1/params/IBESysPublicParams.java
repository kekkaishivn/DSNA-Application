package com.dsna.crypto.asn1.params;

import java.util.Date;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;

import com.dsna.crypto.asn1.exception.InvalidCertificateException;

public class IBESysPublicParams extends IBESysParams {

	private IBEPublicParameters ps06PublicParameters;
	private IBEPublicParameters cd07IBBEPublicParameters;
	
	public IBESysPublicParams(ASN1Sequence seq) throws InvalidCertificateException {
		super(seq);
		if (seq.getObjectAt(4) instanceof ASN1Sequence)	
			this.ps06PublicParameters = new IBEPublicParameters((ASN1Sequence)seq.getObjectAt(4));
		
		if (seq.getObjectAt(5) instanceof ASN1Sequence)	
			this.cd07IBBEPublicParameters = new IBEPublicParameters((ASN1Sequence)seq.getObjectAt(5));
	}
	
	public IBESysPublicParams(int version, String districtName, int districtSerial, Date notBefore, Date notAfter, 
			IBEPublicParameters ps06PublicParameters, IBEPublicParameters cd07IBBEPublicParameters) 
			throws Exception	{
		super(version, districtName, districtSerial, notBefore, notAfter);
		this.ps06PublicParameters = ps06PublicParameters;
		this.cd07IBBEPublicParameters = cd07IBBEPublicParameters;
	}
	
	@Override
	public DERObject toASN1Object() {
		ASN1EncodableVector v = new ASN1EncodableVector();
		v.add(version);
		v.add(districtName);
		v.add(districtSerial);
		v.add(validity);
		v.add(ps06PublicParameters);
		v.add(cd07IBBEPublicParameters);
		return new DERSequence(v);
	}
	
	public IBEPublicParameters getPs06Parameter()	{
		return ps06PublicParameters;
	}
	
	public IBEPublicParameters getCD07IBBEParameter()	{
		return cd07IBBEPublicParameters;
	}

}
