package com.dsna.service;

import javax.crypto.Cipher;

import com.dsna.entity.encrypted.EncryptedEntity;
import com.dsna.entity.encrypted.KeyInfo;

public interface IdBasedSecureSocialEventListener extends SocialEventListener {
	
	public void receiveEncryptedEntity(EncryptedEntity e);
	public void receiveKeyInfo(KeyInfo encapsulatedKey);

}
