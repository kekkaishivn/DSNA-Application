package com.dsna.storage.cloud;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rice.Continuation;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.*;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.*;

public class GoogleCloudStorageServiceImpl implements CloudStorageService {

	private Drive service;
	
	public GoogleCloudStorageServiceImpl(Drive service)	{
		this.service = service;
	}
	
	private FileList queryFiles(String query) throws IOException	{
  	Files.List request = service.files().list().setQ(query);
  	return request.execute();
	}
	
	private String queryFileId(String query) throws IOException	{
		FileList files = queryFiles(query);
		return (files.getItems().size()==0 ? null : files.getItems().get(0).getId());
	}
	
	@Override
	public List<String> initializeDSNAFolders() throws UserRecoverableAuthIOException, IOException {
		// TODO Auto-generated method stub
		ArrayList<String> dsnaFolderIds = new ArrayList<String>();
		String rootDSNAFolderId = queryFileId(
				"mimeType='application/vnd.google-apps.folder' and trashed=false and title='DSNA' and properties has { key='rootId' and value='abdu1234' }");
		if (rootDSNAFolderId==null)	{
			Property rootProperty = new Property();
			rootProperty.setKey("rootId");
			rootProperty.setValue("abdu1234");
			rootDSNAFolderId = createFolder("DSNA", "DSNA Root Folder", null, Arrays.asList(rootProperty));
		}
		
		String friendOnlyFolderId = queryFileId(
				"mimeType='application/vnd.google-apps.folder' and trashed=false and title='FriendOnly' and properties has { key='friendOnlyId' and value='ahue1348' }");
		if (friendOnlyFolderId==null)	{
			Property friendOnlyProperty = new Property();
			friendOnlyProperty.setKey("friendOnlyId");
			friendOnlyProperty.setValue("ahue1348");
			friendOnlyFolderId = createFolder("FriendOnly", "DSNA FriendOnly Folder", rootDSNAFolderId, Arrays.asList(friendOnlyProperty));
		}
		
		String publicFolderId = queryFileId(
				"mimeType='application/vnd.google-apps.folder' and trashed=false and title='FriendOnly' and properties has { key='publicId' and value='iuerq9384' }");
		if (publicFolderId==null)	{
			Property publicProperty = new Property();
			publicProperty.setKey("publicId");
			publicProperty.setValue("iuerq9384");
			publicFolderId = createFolder("Public", "DSNA Public Folder", rootDSNAFolderId, Arrays.asList(publicProperty));
		}
		dsnaFolderIds.add(rootDSNAFolderId);
		dsnaFolderIds.add(friendOnlyFolderId);
		dsnaFolderIds.add(publicFolderId);
		return dsnaFolderIds;
	}

	@Override
	public String uploadContentToFriendOnlyFolder(String title, String type, String description,
		InputStream content) throws UserRecoverableAuthIOException, IOException {
		// TODO Auto-generated method stub
  	Files.List request = service.files().list().setQ(
        "mimeType='application/vnd.google-apps.folder' and trashed=false and title='FriendOnly'");
  	
  	FileList files = request.execute();
  	File friendOnlyFolder = (files.size()>0 ? files.getItems().get(0) : null);
  	return createFile(title, type, description, friendOnlyFolder.getId(), content);	
	}

	@Override
	public String uploadContentToPublicFolder(String title, String type, String description,
		InputStream content) throws UserRecoverableAuthIOException, IOException {
  	Files.List request = service.files().list().setQ(
        "mimeType='application/vnd.google-apps.folder' and trashed=false and title='Public'");
  	
  	FileList files = request.execute();
  	File publicFolder = files.getItems().get(0);
  	return createFile(title, type, description, publicFolder.getId(), content);	
	}

	@Override
	public List<String> addPermission(String fileId, List<String> userIds, String type,
		String role) throws UserRecoverableAuthIOException, IOException {
		ArrayList<String> permissionIds = new ArrayList<String>();
    for (String id : userIds)	{
      Permission permission = new Permission();
      permission.setValue(id);
      permission.setType(type);
      permission.setRole(role);  
      permissionIds.add(service.permissions().insert(fileId, permission).execute().getId());
    }  
    return permissionIds;
	}

	@Override
	public void removePermission(String fileId, String permissionId)
			throws UserRecoverableAuthIOException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removePermission(String fileId, String userId, String permission)
			throws UserRecoverableAuthIOException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String createFolder(String title, String description, String parentId)
		throws UserRecoverableAuthIOException, IOException {
		return createFolder(title, description, parentId, null);
	}
	
	public String createFolder(String title, String description, String parentId, List<Property> properties) 
		throws UserRecoverableAuthIOException, IOException	{
		File body = new File();
		if (title!=null)
			body.setTitle(title);
		if (description!=null)
			body.setDescription(description);
		if (parentId!=null)	{
      ParentReference parentRef = new ParentReference();
      parentRef.setIsRoot(false);
      parentRef.setId(parentId);			
			body.setParents(Arrays.asList(parentRef));
		}
		if (properties!=null)	body.setProperties(properties);
		body.setMimeType("application/vnd.google-apps.folder");
		File folder = service.files().insert(body).execute();
		String folderId = (folder==null ? null : folder.getId());
		return folderId;		
	}

	@Override
	public String createFile(String title, String type, String description, String parentId,
		InputStream content) throws UserRecoverableAuthIOException, IOException {
		return this.createFile(title, type, description, parentId, content, null);
	}
	
	public String createFile(String title, String type, String description, String parentId,
		InputStream content, List<Property> properties) throws UserRecoverableAuthIOException, IOException {
		File body = new File();
		if (title!=null)	body.setTitle(title);
		if (description!=null)	body.setDescription(description);
		if (parentId!=null)	{
      ParentReference parentRef = new ParentReference();
      parentRef.setIsRoot(false);
      parentRef.setId(parentId);			
			body.setParents(Arrays.asList(parentRef));
		}
		if (properties!=null)	body.setProperties(properties);
		body.setMimeType(type);
		InputStreamContent mediaContent = new InputStreamContent(type, content);
    File insertedFile = service.files().insert(body, mediaContent).execute();
		String fileId = (insertedFile==null ? null : insertedFile.getId());
		return fileId;
	}

	private InputStream getFile(String fileId) throws UserRecoverableAuthIOException, IOException {
		File file = service.files().get(fileId).execute();
		String downloadURL = file.getDownloadUrl();
    HttpResponse resp =
        service.getRequestFactory().buildGetRequest(new GenericUrl(downloadURL))
            .execute();
    return resp.getContent();
	}

	@Override
	public void getFile(final String fileId, final Continuation<InputStream, Exception> action) {
		Thread thread = new Thread(){
	    public void run(){
	      try {
					InputStream in = getFile(fileId);
					action.receiveResult(in);
				} catch (Exception e) {
					action.receiveException(e);
				}
	    }
	  };
	  thread.start();
	}

}
