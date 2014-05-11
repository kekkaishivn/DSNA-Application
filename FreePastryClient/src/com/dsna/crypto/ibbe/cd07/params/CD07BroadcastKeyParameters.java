package com.dsna.crypto.ibbe.cd07.params;

import it.unisa.dia.gas.jpbc.Element;

public class CD07BroadcastKeyParameters extends CD07KeyParameters {
	
	protected Element[] ids;
	protected CD07KeyParameters key;

  public CD07BroadcastKeyParameters(boolean isPrivate, CD07KeyParameters key, Element[] identities) {
      super(isPrivate, key.getParameters());
      this.key = key;
      this.ids = identities;
  }

}
