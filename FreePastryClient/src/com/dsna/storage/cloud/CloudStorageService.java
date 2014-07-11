package com.dsna.storage.cloud;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import rice.Continuation;
import rice.p2p.past.PastContent;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

public interface CloudStorageService {
	public List<String> initializeDSNAFolders() throws UserRecoverableAuthIOException, IOException;
	public String uploadContentToFriendOnlyFolder(String title, String type, String description, InputStream content) throws UserRecoverableAuthIOException, IOException;
	public String uploadContentToPublicFolder(String title, String type, String description, InputStream content) throws UserRecoverableAuthIOException, IOException;
	public List<String> addPermission(String fileId, List<String> userIds, String type, String role) throws UserRecoverableAuthIOException, IOException;
	public List<String> addPermissionToFriendFolder(List<String> userIds, String type, String role) throws UserRecoverableAuthIOException, IOException;
	public void removePermission(String fileId, String permissionId) throws UserRecoverableAuthIOException, IOException;
	public void removePermission(String fileId, String userId, String permission) throws UserRecoverableAuthIOException, IOException;
	public void removePermissionFromFriendFolder(String permissionId) throws UserRecoverableAuthIOException, IOException;
	public String createFolder(String title, String description, String parentId)	throws UserRecoverableAuthIOException, IOException;
	public String createFile(String title, String type, String description, String parentId, InputStream content) throws UserRecoverableAuthIOException, IOException;
	public void getFile(String fileId, Continuation<InputStream, Exception> action);
}
