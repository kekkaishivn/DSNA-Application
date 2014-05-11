package com.dsna.asn1.ibe;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;

public class IBEPublicParameters extends ASN1Encodable {
	
	private List<IBEPublicParameter> ibePublicParameters;
	
	public IBEPublicParameters(ASN1Sequence seq) throws Exception	{
		ibePublicParameters = new ArrayList<IBEPublicParameter>();
		Enumeration objects = seq.getObjects();
		while (objects.hasMoreElements())	{
			Object obj = objects.nextElement();
			if (obj instanceof ASN1Sequence)	
				ibePublicParameters.add(new IBEPublicParameter((ASN1Sequence)obj));
		}
	}
	
	public IBEPublicParameters(List<IBEPublicParameter> parameters)	{
		ibePublicParameters = new ArrayList<IBEPublicParameter>();
		ibePublicParameters.addAll(parameters);
		
	}

	@Override
	public DERObject toASN1Object() {
		ASN1EncodableVector v = new ASN1EncodableVector();
		for (IBEPublicParameter param : ibePublicParameters)
			v.add(param);
		return new DERSequence(v);
	}
	
	public IBEPublicParameter getElementAt(int index)	{
		return ibePublicParameters.get(index);
	}

}
