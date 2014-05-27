package com.dsna.service;

import it.unisa.dia.gas.jpbc.Element;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.bouncycastle.crypto.CipherParameters;

import rice.pastry.JoinFailedException;
import rice.pastry.PastryNode;

import com.dsna.crypto.ibbe.cd07.IBBECD07;
import com.dsna.entity.Notification;
import com.dsna.entity.NotificationType;
import com.dsna.entity.SocialProfile;
import com.dsna.storage.cloud.CloudStorageService;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

public class IdBasedSecureSocialServiceImpl extends SocialServiceImpl implements IdBasedSecureSocialService {

	protected IdBasedSecureSocialServiceImpl(PastryNode pastryNode, SocialEventListener eventListener, CloudStorageService cloudHandler) throws IOException, InterruptedException, JoinFailedException	{
		super(pastryNode, eventListener, cloudHandler);
	}	
	
	public IdBasedSecureSocialServiceImpl(SocialProfile user, PastryNode pastryNode, SocialEventListener uiUpdater, CloudStorageService cloudHandler) throws IOException, InterruptedException, JoinFailedException	{
		super(user, pastryNode, uiUpdater, cloudHandler);
	}
	
	public IdBasedSecureSocialServiceImpl(SocialProfile user, HashMap<String,Long> lastSeqs, PastryNode pastryNode, SocialEventListener uiUpdater, CloudStorageService cloudHandler) throws IOException, InterruptedException, JoinFailedException	{
		super(user, lastSeqs, pastryNode, uiUpdater, cloudHandler);
	}
	
	public IdBasedSecureSocialServiceImpl(String username, PastryNode pastryNode, SocialEventListener uiUpdater, CloudStorageService cloudHandler) throws IOException, InterruptedException, JoinFailedException	{
		super(username, pastryNode, uiUpdater, cloudHandler);
	}	
	
	@Override
	public byte[] changeSessionKey(CipherParameters publicKey, String[] ids) throws UserRecoverableAuthIOException, IOException {
		IBBECD07 cd07 = new IBBECD07();		
    Element[] cd07Ids = cd07.map(publicKey, ids);		
		
    // Encryption/Decryption
    byte[][] ciphertext = cd07.encaps(publicKey, cd07Ids);
    InputStream header = new ByteArrayInputStream(ciphertext[1]);
    String id = cloudHandler.uploadContentToFriendOnlyFolder(new String(ciphertext[1])+".txt", "text/plain", "DSNA Session Key header", header);
		Notification notification = userProfile.createNotification(NotificationType.SESSION_KEY_CHANGE);
		notification.setDescription("DSNA Session Key change");
		notification.setArgument(Notification.GOOGLEID, id);
		broadcaster.publish(userProfile.getToFollowNotificationTopic(), notification, true);			
		return ciphertext[0];
	}

}
