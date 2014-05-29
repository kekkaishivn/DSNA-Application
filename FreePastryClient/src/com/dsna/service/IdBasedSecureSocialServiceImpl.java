package com.dsna.service;

import it.unisa.dia.gas.jpbc.Element;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;

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
import com.dsna.entity.encrypted.IdbasedSecureSocialProfile;
import com.dsna.entity.encrypted.KeyHeader;
import com.dsna.entity.encrypted.KeyInfo;
import com.dsna.entity.exception.UnsupportedNotificationTypeException;
import com.dsna.storage.cloud.CloudStorageService;
import com.dsna.util.DateTimeUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

public class IdBasedSecureSocialServiceImpl extends SocialServiceImpl implements IdBasedSecureSocialService {

	protected IdBasedSecureSocialServiceImpl(PastryNode pastryNode, SocialEventListener eventListener) throws IOException, InterruptedException, JoinFailedException	{
		super(pastryNode, eventListener);
	}	
	
	public IdBasedSecureSocialServiceImpl(IdbasedSecureSocialProfile user, PastryNode pastryNode, SocialEventListener uiUpdater) throws IOException, InterruptedException, JoinFailedException	{
		super(user, pastryNode, uiUpdater);
	}
	
	public IdBasedSecureSocialServiceImpl(IdbasedSecureSocialProfile user, HashMap<String,Long> lastSeqs, PastryNode pastryNode, SocialEventListener uiUpdater) throws IOException, InterruptedException, JoinFailedException	{
		super(user, lastSeqs, pastryNode, uiUpdater);
	}
	
	public IdBasedSecureSocialServiceImpl(String username, PastryNode pastryNode, SocialEventListener uiUpdater) throws IOException, InterruptedException, JoinFailedException	{
		super(pastryNode, uiUpdater);
		IdbasedSecureSocialProfile profile = null;
		profile = IdbasedSecureSocialProfile.getSocialProfile(username, idf);
		setSocialProfile(profile);
	}	
	
	private void distributeSessionKeyAssist(CipherParameters publicKey, String[] ids, final Continuation<KeyInfo, Exception> action)	{
		IBBECD07 cd07 = new IBBECD07();		
    Element[] cd07Ids = cd07.map(publicKey, ids);	
    for (int i=0; i<cd07Ids.length; i++)
    	System.out.println(cd07Ids[i]);
    
		try {
	    byte[][] ciphertext = cd07.encaps(publicKey, cd07Ids);
	    final Id headerId = idf.buildId(ciphertext[1]);
	    String keyId = headerId.toStringFull();
	    final	KeyHeader	keyHeader = new KeyHeader(userProfile.getOwnerUsername(), DateTimeUtil.getCurrentTimeStamp(), ciphertext[1], ids, keyId);
	    final KeyInfo keyInfo = new KeyInfo(userProfile.getOwnerUsername(), DateTimeUtil.getCurrentTimeStamp(), keyId, ciphertext[0]);
		
	    // Encryption/Decryption
	    if (cloudHandlers.isEmpty())	{
	    	storageHandler.insert(new DSNAPastContent(headerId, keyHeader, GCPast.INFINITY_EXPIRATION), new Continuation<Boolean[], Exception>() {
	        // the result is an Array of Booleans for each insert
	        public void receiveResult(Boolean[] results) {          
	      		Notification notification;
	  				try {
	  					notification = userProfile.createNotification(NotificationType.SESSION_KEY_CHANGE);
	  	    		notification.setDescription(headerId.toStringFull());
	  	    		notification.putFileId(Location.DHT, headerId.toStringFull());
	  	    		broadcaster.publish(userProfile.getToFollowNotificationTopic(), notification, true);	
	  	    		action.receiveResult(keyInfo);
	  				} catch (UnsupportedNotificationTypeException e) {
	  					eventListener.receiveInsertException(e);
	  				}
	        }
	
	        public void receiveException(Exception result) {
	        	eventListener.receiveInsertException(result);
	        }
	      });
	    	return;
	    }
    
	    InputStream header = createInputStreamFromObject(keyHeader);    
	    String id = cloudHandlers.get(Location.GOOGLE_CLOUD).uploadContentToFriendOnlyFolder(new String(ciphertext[1])+".txt", "text/plain", "DSNA Session Key header", header);
			Notification notification;
			notification = userProfile.createNotification(NotificationType.SESSION_KEY_CHANGE);
			notification.setDescription("DSNA Session Key change");
			notification.putFileId(Location.GOOGLE_CLOUD, id);
			broadcaster.publish(userProfile.getToFollowNotificationTopic(), notification, true);			
			action.receiveResult(new KeyInfo(userProfile.getOwnerUsername(), DateTimeUtil.getCurrentTimeStamp(), keyId, ciphertext[0]));		
		} catch (UnsupportedNotificationTypeException | InvalidCipherTextException | IOException e) {
			action.receiveException(e);
		}		
	}
	
	@Override
	public void distributeSessionKey(final CipherParameters publicKey,final String[] ids,final Continuation<KeyInfo, Exception> action) {
		new Thread()	{
			public void run()	{
				distributeSessionKeyAssist(publicKey, ids, action);
			}
		}.start();
	}

	@Override
	public void distributeSessionKey(CipherParameters publicKey, Continuation<KeyInfo, Exception> action) {
		ArrayList<String> friendUsernames = new ArrayList<String>();
		friendUsernames.addAll(userProfile.getFriendsContacts().keySet());
		friendUsernames.add(userProfile.getOwnerUsername());
		String[] ids = new String[friendUsernames.size()];
		friendUsernames.toArray(ids);
		distributeSessionKey(publicKey, ids, action);
	}
	
	protected void postStatus(final Id statusId, final String status, final String keyId, final byte[] key, String algorithm) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException {

		Status theStatus = userProfile.createStatus(status);
		EncryptedEntity sealedStatus = userProfile.createEncryptedEntity(theStatus, keyId, key, algorithm);
		
		if (cloudHandlers.keySet().size()>0)	{
			Notification notification;
			try {
				notification = userProfile.createNotification(NotificationType.NEWFEEDS);
				notification.setDescription(statusId.toStringFull());
				for (String location : cloudHandlers.keySet())	{
					CloudStorageService cloudHandler = cloudHandlers.get(location);
					InputStream content = createInputStreamFromObject(sealedStatus);
					String id = cloudHandler.uploadContentToFriendOnlyFolder(statusId.toStringFull()+".txt", "text/plain", "DSNA status", content);
		  		notification.putFileId(location, id);
				}
	  		broadcaster.publish(userProfile.getToFollowNotificationTopic(), notification, true);
			} catch (UnsupportedNotificationTypeException e) {
				eventListener.receiveInsertException(e);
			}
  		return;
		}		
		
		storageHandler.insert(new DSNAPastContent(statusId, sealedStatus, GCPast.INFINITY_EXPIRATION), new Continuation<Boolean[], Exception>() {
      // the result is an Array of Booleans for each insert
      public void receiveResult(Boolean[] results) {          
    		Notification notification;
				try {
					notification = userProfile.createNotification(NotificationType.NEWFEEDS);
	    		notification.setDescription(statusId.toStringFull());
	    		System.out.println(statusId.toStringFull());
	    		notification.putFileId(Location.DHT, statusId.toStringFull());
	    		broadcaster.publish(userProfile.getToFollowNotificationTopic(), notification, true);					
				} catch (UnsupportedNotificationTypeException e) {
					eventListener.receiveInsertException(e);
				}
      }

      public void receiveException(Exception result) {
      	eventListener.receiveInsertException(result);
      }
    });
		
	}

	@Override
	public void postStatus(String status, String keyId, byte[] key, String algorithm)
			throws UserRecoverableAuthIOException, IOException, InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException {
		Id statusId = idf.buildId(DateTimeUtil.getCurrentTimeStamp() + userProfile.getOwnerUsername() + status);
		postStatus(statusId, status, keyId, key, algorithm);		
	}

	@Override
	public void postStatus(String id, String status, String keyId, byte[] key, String algorithm)
			throws UserRecoverableAuthIOException, IOException, InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException {
		Id statusId = idf.buildIdFromToString(id);
		postStatus(statusId, status, keyId, key, algorithm);
	}

}
