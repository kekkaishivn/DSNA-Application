package com.dsna.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtil {
	
	public static long getCurrentTimeStamp()	{
		Date now = new Date();
		return now.getTime();
	}
	
	public static String getFormattedTimeStamp(long timeStamp, SimpleDateFormat sdf)	{
		Date date = new Date(timeStamp);
		return sdf.format(date);
	}
	
	public static String getFormattedTimeStamp(long timeStamp)	{
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm, dd MMMMMM");
		Date date = new Date(timeStamp);
		return sdf.format(date);
	}
	
	

}
