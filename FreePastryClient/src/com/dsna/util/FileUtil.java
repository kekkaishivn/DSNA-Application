package com.dsna.util;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class FileUtil {
	
	public static void writeObject(FileOutputStream fout, Serializable object) throws IOException	{
		ObjectOutputStream oos = null;
    oos = new ObjectOutputStream(fout);
    oos.writeObject(object);
    oos.flush();
    oos.close();
	}
	
	public static void writeText(FileOutputStream fout, String s) throws IOException	{
		BufferedOutputStream bos = null;
    bos = new BufferedOutputStream(fout);
    bos.write(s.getBytes());
    bos.flush();
    bos.close();
	}
	
	public static Object readObject(FileInputStream streamIn) throws IOException, ClassNotFoundException	{
		ObjectInputStream objectinputstream = null; 
    objectinputstream = new ObjectInputStream(streamIn);
    Object result = (Object)objectinputstream.readObject();
    objectinputstream .close();
    return result;
	}
	
}
