package com.dsna.crypto.asn1.params;

import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06Parameters;
import it.unisa.dia.gas.crypto.jpbc.signature.ps06.params.PS06PublicKeyParameters;

import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.crypto.CipherParameters;

import com.dsna.crypto.asn1.exception.UnsupportedFormatException;
import com.dsna.crypto.ibbe.cd07.params.CD07Parameters;
import com.dsna.crypto.ibbe.cd07.params.CD07PublicKeyParameters;

public class IBEPublicParameters extends IBEParameters {

	public IBEPublicParameters(ASN1Sequence seq) {
		super(seq);
	}
	
	public IBEPublicParameters(List<IBEParameter> parameters)	{
		super(parameters);
	}
	
}
