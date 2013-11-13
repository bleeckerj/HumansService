package com.nearfuturelaboratory.util;



import java.io.*;
import java.util.*;

import org.apache.commons.configuration.*;
import org.apache.log4j.Logger;

/**
 * Central place to store constants used on the site.
 *
 * @author Julian Bleecker
 */

public class Constants /*extends PropertiesConfiguration*/ {

  /** Class-wide, webapp-wide cache for our constants */
  private static PropertiesConfiguration sProps = new PropertiesConfiguration();
  private final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.util.Constants");

/*
  static {
	 try {
		 load("./conf/app.properties");
	 }
	 catch(Exception e) {
		 e.printStackTrace();
	 }
  }
*/

  /**
 * @throws ConfigurationException 
	* Loads the constants from a file.
	*
	* @param aFile A properties file with constants to load
	* @throws FileNotFoundException If the aFile file could not be opened
	* @throws IOException If an error happened when reading aFile
 * @throws  
	*/
  public static synchronized void load(String aFile) throws IOException, ConfigurationException  {
	
		sProps = new PropertiesConfiguration(aFile);
	
	 logger.info("app constants loaded from "+aFile);
  }

  public static synchronized void load(InputStream aStream) throws IOException, ConfigurationException {
	 sProps = new PropertiesConfiguration();
	 sProps.load(aStream);
	 logger.info("app constants loaded from "+aStream);
  }
  

  public static Iterator getKeys() {
	 return sProps.getKeys();
  }
  
  public static String getString(String aKey) {
    logger.info("Getting a constant string for key "+aKey);
	 return get(aKey);
  }
  
  public static void setProperty(String aKey, Object aVal) {
	 sProps.setProperty(aKey, aVal);
  }
  

  /**
	* @param aKey The name of the constant to retrieve
	* @return The value of <code>aKey</code> from the properties
	* @deprecated
	*/
  public static String get(String aKey) {
	 if(sProps.getString(aKey) == null) {
		 // log...
		 logger.error("You attempted to get property "+aKey+" but it doesn't exist.");
		 Exception e = new Exception("where is it??");
		 logger.error(e.getMessage(), e);
	 }
	 return sProps.getString(aKey);
  }

  /**
	*/
  public static String getString(String aKey, String aDefault) {
	 if(sProps.getString(aKey) == null) {
		 // log...
		 logger.error("You attempted to get property "+aKey+" but it doesn't exist.");
		 Exception e = new Exception("where is it??");
		 logger.error(e.getMessage(), e);
	 }
	 return sProps.getString(aKey, aDefault);
  }

  public static float getFloat(String aKey, float aDefault) {
	 float result = sProps.getFloat(aKey, aDefault);
	 return result;
  }
  
  public static float getFloat(String aKey) {
	 float result = sProps.getFloat(aKey);
	 return result;
  }

  public static long getLong(String aKey, long aDefault) {
	 long result = sProps.getLong(aKey, aDefault);
	 return result;
  }
  
  public static long getLong(String aKey) {
	 long result = sProps.getLong(aKey);
	 return result;
  }

  public static double getDouble(String aKey, double aDefault) {
	 double result = sProps.getDouble(aKey, aDefault);
	 return result;
  }
  
  public static double getDouble(String aKey) {
	 double result = sProps.getDouble(aKey);
	 return result;
  }


  public static int getInt(String aKey, int aDefault) {
	 int result = sProps.getInt(aKey, aDefault);
	 return result;
  }

  /**
	* Converts String values from the properties files to integers.
	*
	* <p>
	* If the property is not found or is an invalid integer, then
	* <code>0</code> is returned.
	*
	* @param aKey The name of the constants to retrieve
	* @return The value of <code>aKey</code> from the properties
	* @deprecated
	*/
  public static int getInt(String aKey) {
	 int i = 0;
	 if(sProps.getProperty(aKey) == null) {
		 // log...
		 logger.error("You attempted to get property "+aKey+" but it doesn't exist.");
       Exception e = new Exception("where is it??");
		 logger.error(e.getMessage(), e);	
	 }
	 try {
		 i = sProps.getInt(aKey);
	 }
	 catch (NumberFormatException e) {
		 i = 0;
	 }
	 return i;
  }


  public static boolean getBoolean(String aKey) {
	 return sProps.getBoolean(aKey);
  }
  
  public static boolean getBoolean(String aKey, boolean aDefault) {
	 return sProps.getBoolean(aKey, aDefault);
  }
  
  public static Boolean getBoolean(String aKey, Boolean aDefault) {
	 return sProps.getBoolean(aKey, aDefault);
  }
  

}
