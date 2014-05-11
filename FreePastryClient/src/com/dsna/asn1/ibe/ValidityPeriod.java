package com.dsna.asn1.ibe;

import java.text.ParseException;
import java.util.Date;

import org.bouncycastle.asn1.*;

public class ValidityPeriod extends ASN1Encodable {
	
	private DERGeneralizedTime notBefore;
	private DERGeneralizedTime notAfter;
	
	public ValidityPeriod(ASN1Sequence seq) throws Exception	{
		if (seq.getObjectAt(0) instanceof DERGeneralizedTime)	
			this.notBefore = (DERGeneralizedTime)seq.getObjectAt(0);
		
		if (seq.getObjectAt(1) instanceof DERGeneralizedTime)	
			this.notAfter = (DERGeneralizedTime)seq.getObjectAt(1);
		
		if (notAfter.getDate().before(notBefore.getDate()))
			throw new Exception("Invalid period");
	}
	
	public ValidityPeriod(Date notBefore, Date notAfter) throws Exception	{
		if (notAfter.before(notBefore))
			throw new Exception("Invalid period");
		
		this.notBefore = new DERGeneralizedTime(notBefore);
		this.notAfter = new DERGeneralizedTime(notAfter);
	}
	
	public Date notBefore() throws ParseException	{
		return notBefore.getDate();
	}
	
	public Date notAfter() throws ParseException	{
		return notAfter.getDate();
	}
	
	@Override
	public DERObject toASN1Object() {
		ASN1EncodableVector v = new ASN1EncodableVector();
		v.add(notBefore);
		v.add(notAfter);
		return new DERSequence(v);
	}
	
}
