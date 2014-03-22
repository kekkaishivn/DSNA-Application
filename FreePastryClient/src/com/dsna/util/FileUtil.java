package com.dsna.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class FileUtil {
	
	public static void writeObject(String fileName, Serializable object)	{
		ObjectOutputStream oos = null;
		FileOutputStream fout = null;
		try{
      fout = new FileOutputStream(fileName, false);
      oos = new ObjectOutputStream(fout);
      oos.writeObject(object);
      oos.flush();
		} catch (Exception ex) {
      ex.printStackTrace();
		}finally {
      if(oos  != null){
          try {
						oos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
       } 
		}
	}
	
	public static Object readObject(String fileName)	{
		 ObjectInputStream objectinputstream = null; 
		try {
        FileInputStream streamIn = new FileInputStream(fileName);
        objectinputstream = new ObjectInputStream(streamIn);
        Object result = objectinputstream.readObject();
        return result;
		 } catch (Exception e) {
	     e.printStackTrace();
	     return null;
		 }finally {
        if(objectinputstream != null){
            try {
							objectinputstream .close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
         } 
		 }
	}
	
}
