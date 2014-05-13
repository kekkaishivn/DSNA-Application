package com.dsna.crypto.asn1.params;

import java.text.ParseException;
import java.util.Date;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;

import com.dsna.crypto.asn1.exception.InvalidCertificateException;

public abstract class IBESysParams extends ASN1Encodable {
	
	protected DERInteger version;
	protected DERIA5String districtName;
	protected DERInteger districtSerial;
	protected ValidityPeriod validity;
	
	protected IBESysParams(ASN1Sequence seq) throws InvalidCertificateException {
		if (seq.getObjectAt(0) instanceof DERInteger)	
			this.version = (DERInteger)seq.getObjectAt(0);
		
		if (seq.getObjectAt(1) instanceof DERIA5String)	
			this.districtName = (DERIA5String)seq.getObjectAt(1);
		
		if (seq.getObjectAt(2) instanceof DERInteger)	
			this.districtSerial = (DERInteger)seq.getObjectAt(2);
		
		if (seq.getObjectAt(3) instanceof ASN1Sequence)		
			this.validity = new ValidityPeriod((ASN1Sequence)seq.getObjectAt(3));
	}
	
	protected IBESysParams(int version, String districtName, int districtSerial, Date notBefore, Date notAfter) 
			throws InvalidCertificateException	{
		this.version = new DERInteger(version);
		this.districtName = new DERIA5String(districtName);
		this.districtSerial = new DERInteger(districtSerial);
		this.validity = new ValidityPeriod(notBefore, notAfter);
	}
	
	public int getVersion()	{
		return version.getValue().intValue();
	}
	
	public String getDistrictName()	{
		return districtName.getString();
	}
	
	public int getDistrictSerial()	{
		return districtSerial.getValue().intValue();
	}
	
	public boolean isValidPeriod(Date currentTime)	{
		try {
			return validity.isValidPeriod(currentTime);
		} catch (ParseException e) {
			return false;
		}
	}

}
