package com.dsna.asn1.ibe;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;

public class IBEPublicParameter extends ASN1Encodable {
	
	private DERObjectIdentifier ibeAlgorithm;
	private DEROctetString publicParameterData;

	@Override
	public DERObject toASN1Object() {
		ASN1EncodableVector v = new ASN1EncodableVector();
		v.add(ibeAlgorithm);
		v.add(publicParameterData);
		return new DERSequence(v);
	}
	
	public IBEPublicParameter(ASN1Sequence seq) {
		if (seq.getObjectAt(0) instanceof DERObjectIdentifier)	
			this.ibeAlgorithm = (DERObjectIdentifier)seq.getObjectAt(0);
		
		if (seq.getObjectAt(1) instanceof DEROctetString)	
			this.publicParameterData = (DEROctetString)seq.getObjectAt(1);
	}
	
	public IBEPublicParameter(DERObjectIdentifier ibeAlgorithm, byte[] publicParameterData)	{
		this.ibeAlgorithm = ibeAlgorithm;
		this.publicParameterData = new DEROctetString(publicParameterData);
	}
	
	public IBEPublicParameter(String ibeAlgorithmImpl, byte[] publicParameterData)	{
		this(new DERObjectIdentifier(ibeAlgorithmImpl), publicParameterData);
	}
	
	public DERObjectIdentifier getIbeAlgorithm()	{
		return ibeAlgorithm;
	}
	
	public DEROctetString getData()	{
		return publicParameterData;
	}
	
	public byte[] getRawData()	{
		return publicParameterData.getOctets();
	}

}
