package com.dsna.service;

import it.unisa.dia.gas.jpbc.Element;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.crypto.CipherParameters;

import rice.Continuation;
import rice.p2p.commonapi.Id;
import rice.p2p.past.gc.GCPast;
import rice.pastry.JoinFailedException;
import rice.pastry.PastryNode;

import com.dsna.crypto.ibbe.cd07.IBBECD07;
import com.dsna.dht.past.DSNAPastContent;
import com.dsna.entity.Location;
import com.dsna.entity.Notification;
import com.dsna.entity.NotificationType;
import com.dsna.entity.SocialProfile;
import com.dsna.entity.Status;
import com.dsna.entity.encrypted.EncryptedEntity;
import com.dsna.entity.encrypted.KeyInfo;
import com.dsna.storage.cloud.CloudStorageService;
import com.dsna.util.DateTimeUtil;
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
	public KeyInfo distributeSessionKey(CipherParameters publicKey, String[] ids) throws UserRecoverableAuthIOException, IOException {
		IBBECD07 cd07 = new IBBECD07();		
    Element[] cd07Ids = cd07.map(publicKey, ids);		
		
    // Encryption/Decryption
    byte[][] ciphertext = cd07.encaps(publicKey, cd07Ids);
    InputStream header = new ByteArrayInputStream(ciphertext[1]);
    String id = cloudHandler.uploadContentToFriendOnlyFolder(new String(ciphertext[1])+".txt", "text/plain", "DSNA Session Key header", header);
		Notification notification = userProfile.createNotification(NotificationType.SESSION_KEY_CHANGE);
		notification.setDescription("DSNA Session Key change");
		notification.setFileId(Location.GOOGLE_CLOUD, id);
		broadcaster.publish(userProfile.getToFollowNotificationTopic(), notification, true);			
		return new KeyInfo(Location.GOOGLE_CLOUD, id, ciphertext[0]);
	}

	@Override
	public KeyInfo distributeSessionKey(CipherParameters publicKey) throws IOException {
		ArrayList<String> friendUsernames = new ArrayList<String>();
		friendUsernames.addAll(userProfile.getFriendsContacts().keySet());
		friendUsernames.add(userProfile.getOwnerUsername());
		String[] ids = new String[friendUsernames.size()];
		friendUsernames.toArray(ids);
		return distributeSessionKey(publicKey, ids);
	}
	
	protected void postStatus(final Id statusId, final String status, final byte[] key, String algorithm) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException {

		Status theStatus = userProfile.createStatus(status);
		EncryptedEntity sealedStatus = userProfile.createEncryptedEntity(theStatus, key, algorithm);
		
		if (cloudHandler!=null)	{
			InputStream content = createInputStreamFromObject(sealedStatus);
			String id = cloudHandler.uploadContentToFriendOnlyFolder(statusId.toStringFull()+".txt", "text/plain", "DSNA encrypted object", content);
  		Notification notification = userProfile.createNotification(NotificationType.NEWFEEDS);
  		notification.setDescription(statusId.toStringFull());
  		notification.setFileId(Location.GOOGLE_CLOUD, id);
  		broadcaster.publish(userProfile.getToFollowNotificationTopic(), notification, true);			
			return;
		}		
		
		storageHandler.insert(new DSNAPastContent(statusId, sealedStatus, GCPast.INFINITY_EXPIRATION), new Continuation<Boolean[], Exception>() {
      // the result is an Array of Booleans for each insert
      public void receiveResult(Boolean[] results) {          
    		Notification notification = userProfile.createNotification(NotificationType.NEWFEEDS);
    		notification.setDescription(statusId.toStringFull());
    		System.out.println(statusId.toStringFull());
    		notification.setFileId(Location.DHT, statusId.toStringFull());
    		broadcaster.publish(userProfile.getToFollowNotificationTopic(), notification, true);
      }

      public void receiveException(Exception result) {
      	eventListener.receiveInsertException(result);
      }
    });
		
	}

	@Override
	public void postStatus(String status, byte[] key, String algorithm)
			throws UserRecoverableAuthIOException, IOException, InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException {
		Id statusId = idf.buildId(DateTimeUtil.getCurrentTimeStamp() + userProfile.getOwnerUsername() + status);
		postStatus(statusId, status, key, algorithm);		
	}

	@Override
	public void postStatus(String id, String status, byte[] key, String algorithm)
			throws UserRecoverableAuthIOException, IOException, InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException {
		Id statusId = idf.buildIdFromToString(id);
		postStatus(statusId, status, key, algorithm);
	}

}
