package com.dsna.crypto.asn1.params;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;

public class IBEParameters extends ASN1Encodable {
	
	protected List<IBEParameter> ibeParameters;
	
	public IBEParameters(ASN1Sequence seq) {
		ibeParameters = new ArrayList<IBEParameter>();
		Enumeration objects = seq.getObjects();
		while (objects.hasMoreElements())	{
			Object obj = objects.nextElement();
			if (obj instanceof ASN1Sequence)	
				ibeParameters.add(new IBEParameter((ASN1Sequence)obj));
		}
	}
	
	public IBEParameters(List<IBEParameter> parameters)	{
		ibeParameters = new ArrayList<IBEParameter>();
		ibeParameters.addAll(parameters);
	}

	@Override
	public DERObject toASN1Object() {
		ASN1EncodableVector v = new ASN1EncodableVector();
		for (IBEParameter param : ibeParameters)
			v.add(param);
		return new DERSequence(v);
	}
	
	public IBEParameter getElementAt(int index)	{
		return ibeParameters.get(index);
	}

}
