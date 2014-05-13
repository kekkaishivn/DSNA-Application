package com.dsna.crypto.asn1.params;

import java.util.List;

import org.bouncycastle.asn1.ASN1Sequence;

public class IBESecretParameters extends IBEParameters {
	
	public IBESecretParameters(ASN1Sequence seq)	{
		super(seq);
	}
	
	public IBESecretParameters(List<IBEParameter> parameters)	{
		super(parameters);
	}

}
