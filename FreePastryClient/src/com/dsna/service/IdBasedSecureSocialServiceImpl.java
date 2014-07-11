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

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;

import rice.Continuation;
import rice.p2p.commonapi.Id;
import rice.p2p.past.gc.GCPast;
import rice.pastry.JoinFailedException;
import rice.pastry.PastryNode;

import com.dsna.crypto.ibbe.cd07.IBBECD07;
import com.dsna.dht.past.DSNAPastContent;
import com.dsna.entity.BaseEntity;
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
	
	private Integer cipherParamsSyncObj;
	private Cipher cipher;
	private String keyId;
	private HashMap<String, String> keyFileIdsMap = new HashMap<String, String>();
	
	protected IdBasedSecureSocialServiceImpl(PastryNode pastryNode, SocialEventListener eventListener) throws IOException, InterruptedException, JoinFailedException	{
		super(pastryNode, eventListener);
		cipherParamsSyncObj = new Integer(1);
	}	
	
	public IdBasedSecureSocialServiceImpl(IdbasedSecureSocialProfile user, PastryNode pastryNode, SocialEventListener uiUpdater) throws IOException, InterruptedException, JoinFailedException	{
		super(user, pastryNode, uiUpdater);
		cipherParamsSyncObj = new Integer(1);
	}
	
	public IdBasedSecureSocialServiceImpl(IdbasedSecureSocialProfile user, HashMap<String,Long> lastSeqs, PastryNode pastryNode, SocialEventListener uiUpdater) throws IOException, InterruptedException, JoinFailedException	{
		super(user, lastSeqs, pastryNode, uiUpdater);
		cipherParamsSyncObj = new Integer(1);
	}
	
	public IdBasedSecureSocialServiceImpl(String username, PastryNode pastryNode, SocialEventListener uiUpdater) throws IOException, InterruptedException, JoinFailedException	{
		super(pastryNode, uiUpdater);
		IdbasedSecureSocialProfile profile = null;
		profile = IdbasedSecureSocialProfile.getSocialProfile(username, idf);
		setSocialProfile(profile);
		cipherParamsSyncObj = new Integer(1);
	}	
	
	private void distributeSessionKeyAssist(String symmetricAlgorithm, CipherParameters publicKey, String[] ids, final Continuation<KeyInfo, Exception> action)	{
		
		synchronized(cipherParamsSyncObj)	{
			IBBECD07 cd07 = new IBBECD07();		
	    Element[] cd07Ids = cd07.map(publicKey, ids);	
	    
			try {
		    byte[][] ciphertext = cd07.encaps(publicKey, cd07Ids);
		    final Id headerId = idf.buildId(ciphertext[1]);
		    String keyId = headerId.toStringFull();
		    final	KeyHeader	keyHeader = new KeyHeader(userProfile.getOwnerUsername(), DateTimeUtil.getCurrentTimeStamp(), ciphertext[1], ids, keyId, symmetricAlgorithm);
		    final KeyInfo keyInfo = new KeyInfo(userProfile.getOwnerUsername(), DateTimeUtil.getCurrentTimeStamp(), keyId, ciphertext[0], symmetricAlgorithm);
			
		    // Encryption/Decryption
		    if (cloudStorageHandlers.isEmpty())	{
		    	dhtStorageHandler.insert(new DSNAPastContent(headerId, keyHeader, GCPast.INFINITY_EXPIRATION), new Continuation<Boolean[], Exception>() {
		        // the result is an Array of Booleans for each insert
		        public void receiveResult(Boolean[] results) {          
		      		Notification notification;
		  				try {
		  					notification = userProfile.createNotification(NotificationType.SESSION_KEY_CHANGE);
		  	    		notification.setDescription(headerId.toStringFull());
		  	    		notification.putFileId(Location.DHT, headerId.toStringFull());
		  	    		broadcaster.publish(userProfile.getToFollowNotificationTopic(), notification, true);	
		  	    		setSessionKeyParameter(keyInfo);
		  	    		action.receiveResult(keyInfo);
		  				} catch (UnsupportedNotificationTypeException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
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
		    String id = cloudStorageHandlers.get(Location.GOOGLE_CLOUD).uploadContentToFriendOnlyFolder(new String(ciphertext[1])+".txt", "text/plain", "DSNA Session Key header", header);
				Notification notification;
				notification = userProfile.createNotification(NotificationType.SESSION_KEY_CHANGE);
				notification.setDescription("DSNA Session Key change");
				notification.putFileId(Location.GOOGLE_CLOUD, id);
				broadcaster.publish(userProfile.getToFollowNotificationTopic(), notification, true);
				setSessionKeyParameter(keyInfo);
				action.receiveResult(new KeyInfo(userProfile.getOwnerUsername(), DateTimeUtil.getCurrentTimeStamp(), keyId, ciphertext[0], symmetricAlgorithm));		
			} catch (UnsupportedNotificationTypeException | InvalidCipherTextException | IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
				action.receiveException(e);
			}		
		}
	}
	
	@Override
	public void changeAndDistributeSessionKey(final String symmetricAlgorithm, final CipherParameters publicKey,final String[] ids,final Continuation<KeyInfo, Exception> action) {
		new Thread()	{
			public void run()	{
				distributeSessionKeyAssist(symmetricAlgorithm, publicKey, ids, action);
			}
		}.start();
	}

	@Override
	public void changeAndDistributeSessionKey(String symmetricAlgorithm, CipherParameters publicKey, Continuation<KeyInfo, Exception> action) {
		ArrayList<String> friendUsernames = new ArrayList<String>();
		friendUsernames.addAll(userProfile.getFriendsContacts().keySet());
		friendUsernames.add(userProfile.getOwnerUsername());
		String[] ids = new String[friendUsernames.size()];
		friendUsernames.toArray(ids);
		changeAndDistributeSessionKey(symmetricAlgorithm, publicKey, ids, action);
	}
	
	public void setSessionKeyParameter(KeyInfo key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException	{
		synchronized(cipherParamsSyncObj)	{
			SecretKey k = new SecretKeySpec(key.getValues(), key.getAlgorithm());
			cipher = Cipher.getInstance(key.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, k);
			keyId = key.getKeyId();
		}
	}
	
	@Override
	protected void postStatus(final Id statusId, final String status) throws IOException {

		Status theStatus = userProfile.createStatus(status);
		
		if (!theStatus.getPreferEncrypted())	{
			super.postStatus(statusId, status);
			return;
		}
		
		BaseEntity encryptedEntity = null;
		
		try {
			encryptedEntity = userProfile.createEncryptedEntity(theStatus, keyId, cipher);
		} catch (InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | IllegalBlockSizeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (cloudStorageHandlers.keySet().size()>0)	{
			Notification notification;
			try {
				notification = userProfile.createNotification(NotificationType.NEWFEEDS);
	  		notification.setDescription(statusId.toStringFull());
				for (String location : cloudStorageHandlers.keySet())	{
					CloudStorageService cloudHandler = cloudStorageHandlers.get(location);
					InputStream content = createInputStreamFromObject(encryptedEntity);
					String id = cloudHandler.uploadContentToFriendOnlyFolder(statusId.toStringFull()+".txt", "text/plain", "DSNA status", content);
		  		notification.putFileId(location, id);
				}
				broadcast(userProfile.getToFollowNotificationTopic(), notification, true);
			} catch (UnsupportedNotificationTypeException e) {
				eventListener.receiveInsertException(e);
			}
			return;
		}		
		
		dhtStorageHandler.insert(new DSNAPastContent(statusId, encryptedEntity, GCPast.INFINITY_EXPIRATION), new Continuation<Boolean[], Exception>() {
      // the result is an Array of Booleans for each insert
      public void receiveResult(Boolean[] results) {          
    		Notification notification;
				try {
					notification = userProfile.createNotification(NotificationType.NEWFEEDS);
	    		notification.setDescription(statusId.toStringFull());
	    		notification.putFileId(Location.DHT, statusId.toStringFull());
	    		broadcast(userProfile.getToFollowNotificationTopic(), notification, true);					
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
	public void broadcast(String topicId, BaseEntity msg, boolean isCaching)	{
		
		if (!msg.getPreferEncrypted())	{
			super.broadcast(topicId, msg, isCaching);
			return;
		}
		
		try {
			EncryptedEntity encryptedMsg = userProfile.createEncryptedEntity(msg, keyId, cipher);
			super.broadcast(topicId, encryptedMsg, isCaching);
		} catch (InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | IllegalBlockSizeException | IOException e) {
			eventListener.receiveBroadcastException(e);
		}
	}

	@Override
	public void setPreferEncrypted(boolean preferEncrypted) {
		userProfile.setPreferEncrypted(preferEncrypted);
	}

}
