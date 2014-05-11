package com.dsna.asn1.ibe;

import java.util.Date;
import java.util.List;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;

public class IBESysParams extends ASN1Encodable {

	private DERInteger version;
	private DERIA5String districtName;
	private DERInteger districtSerial;
	private ValidityPeriod validity;
	private IBEPublicParameters ps06PublicParameters;
	private IBEPublicParameters delerableeIBBEPublicParameters;
	//private DERObjectIdentifier ibeIdentityType;
	
	public IBESysParams(ASN1Sequence seq) throws Exception	{
		if (seq.getObjectAt(0) instanceof DERInteger)	
			this.version = (DERInteger)seq.getObjectAt(0);
		
		if (seq.getObjectAt(1) instanceof DERIA5String)	
			this.districtName = (DERIA5String)seq.getObjectAt(1);
		
		if (seq.getObjectAt(2) instanceof ValidityPeriod)	
			this.validity = (ValidityPeriod)seq.getObjectAt(2);
		
		if (seq.getObjectAt(3) instanceof ASN1Sequence)	
			this.ps06PublicParameters = new IBEPublicParameters((ASN1Sequence)seq.getObjectAt(3));
		
		if (seq.getObjectAt(4) instanceof ASN1Sequence)	
			this.delerableeIBBEPublicParameters = new IBEPublicParameters((ASN1Sequence)seq.getObjectAt(4));
	}
	
	public IBESysParams(int version, String districtName, int districtSerial, Date notBefore, Date notAfter, 
			IBEPublicParameters ps06PublicParameters, IBEPublicParameters delerableeIBBEPublicParameters) 
			throws Exception	{
		this.version = new DERInteger(version);
		this.districtName = new DERIA5String(districtName);
		this.districtSerial = new DERInteger(districtSerial);
		this.validity = new ValidityPeriod(notBefore, notAfter);
		this.ps06PublicParameters = ps06PublicParameters;
		this.delerableeIBBEPublicParameters = delerableeIBBEPublicParameters;
	}
	
	@Override
	public DERObject toASN1Object() {
		ASN1EncodableVector v = new ASN1EncodableVector();
		v.add(version);
		v.add(districtName);
		v.add(districtSerial);
		v.add(validity);
		v.add(ps06PublicParameters);
		v.add(delerableeIBBEPublicParameters);
		return new DERSequence(v);
	}

}
