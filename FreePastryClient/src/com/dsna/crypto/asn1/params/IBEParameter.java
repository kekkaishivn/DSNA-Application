package com.dsna.crypto.asn1.params;

import it.unisa.dia.gas.jpbc.Element;

import java.nio.ByteBuffer;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;

public class IBEParameter extends ASN1Encodable {
	
	private DERObjectIdentifier ibeAlgorithm;
	private DEROctetString parameterData;

	@Override
	public DERObject toASN1Object() {
		ASN1EncodableVector v = new ASN1EncodableVector();
		v.add(ibeAlgorithm);
		v.add(parameterData);
		return new DERSequence(v);
	}
	
	public IBEParameter(ASN1Sequence seq) {
		if (seq.getObjectAt(0) instanceof DERObjectIdentifier)	
			this.ibeAlgorithm = (DERObjectIdentifier)seq.getObjectAt(0);
		
		if (seq.getObjectAt(1) instanceof DEROctetString)	
			this.parameterData = (DEROctetString)seq.getObjectAt(1);
	}
	
	public IBEParameter(DERObjectIdentifier ibeAlgorithm, byte[] publicParameterData)	{
		this.ibeAlgorithm = ibeAlgorithm;
		this.parameterData = new DEROctetString(publicParameterData);
	}
	
	public IBEParameter(String ibeAlgorithmImpl, byte[] publicParameterData)	{
		this(new DERObjectIdentifier(ibeAlgorithmImpl), publicParameterData);
	}
	
	public static IBEParameter fromInt(String algorithmOid, int data)	{
		return new IBEParameter(algorithmOid, ByteBuffer.allocate(4).putInt(data).array());
	}
	
	public static IBEParameter fromElement(String algorithmOid, Element e)	{
		return new IBEParameter(algorithmOid, e.toBytes());
	}
	
	public static IBEParameter fromString(String algorithmOid, String s)	{
		return new IBEParameter(algorithmOid, s.getBytes());
	}
	
	public DERObjectIdentifier getIbeAlgorithm()	{
		return ibeAlgorithm;
	}
	
	public DEROctetString getData()	{
		return parameterData;
	}
	
	public byte[] getRawData()	{
		return parameterData.getOctets();
	}

}
