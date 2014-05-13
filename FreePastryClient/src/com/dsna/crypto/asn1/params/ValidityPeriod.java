package com.dsna.crypto.asn1.params;

import java.text.ParseException;
import java.util.Date;

import org.bouncycastle.asn1.*;

import com.dsna.crypto.asn1.exception.InvalidCertificateException;

public class ValidityPeriod extends ASN1Encodable {
	
	private DERGeneralizedTime notBefore;
	private DERGeneralizedTime notAfter;
	
	public ValidityPeriod(ASN1Sequence seq) throws InvalidCertificateException	{
		if (seq.getObjectAt(0) instanceof DERGeneralizedTime)	
			this.notBefore = (DERGeneralizedTime)seq.getObjectAt(0);
		
		if (seq.getObjectAt(1) instanceof DERGeneralizedTime)	
			this.notAfter = (DERGeneralizedTime)seq.getObjectAt(1);
		
		try {
			if (notAfter.getDate().before(notBefore.getDate()))
				throw new InvalidCertificateException("Invalid period");
		} catch (ParseException e) {
			throw new InvalidCertificateException("Invalid date time format");
		}
		
	}
	
	public ValidityPeriod(Date notBefore, Date notAfter) throws InvalidCertificateException	{
		if (notAfter.before(notBefore))
			throw new InvalidCertificateException("Invalid period");
		
		this.notBefore = new DERGeneralizedTime(notBefore);
		this.notAfter = new DERGeneralizedTime(notAfter);
	}
	
	public Date notBefore() throws ParseException	{
		return notBefore.getDate();
	}
	
	public Date notAfter() throws ParseException	{
		return notAfter.getDate();
	}
	
	public boolean isValidPeriod(Date currentTime) throws ParseException	{
		return (notBefore.getDate().before(currentTime) && notAfter.getDate().after(currentTime));
	}
	
	@Override
	public DERObject toASN1Object() {
		ASN1EncodableVector v = new ASN1EncodableVector();
		v.add(notBefore);
		v.add(notAfter);
		return new DERSequence(v);
	}
	
}
