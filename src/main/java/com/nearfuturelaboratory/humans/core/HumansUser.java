package com.nearfuturelaboratory.humans.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.nearfuturelaboratory.util.Constants;
import com.nearfuturelaboratory.util.Pair;
import com.nearfuturelaboratory.util.file.FileUtils;
import com.nearfuturelaboratory.util.file.Finder;

/**
 * Basically a wrapper around the attributes and facets of a "user" in the
 * Humans system someone who uses Humans
 * 
 * @author julian
 * 
 */
@Deprecated
public class HumansUser {

	protected String username;
	protected String email;
	protected String passwordEnc;
	protected JSONObject userJSON;
	protected String firstName;
	protected String lastName;
	protected boolean validUser;
/*	private DataSource dataSource;
	private Connection connection;
	private Statement statement;
*/	// a map of service name (foursquare, instagram, etc.) to a list/array of
	// userid-username pairs
	// which provide an index of sorts to the user details for each service in
	// the file hierarchy (i.e. token and user data)
	// protected Map<String, List> servicesToAccount;

	protected static String USER_DATA_ROOT = Constants.getString("USER_DATA_ROOT");
	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.test.Test");

	/**
	 * Default constructor. Use this for creating a new user.
	 */
	public HumansUser() {
		validUser = false;
		userJSON = new JSONObject();

		/*
		 * try { // Get DataSource Context initContext = new InitialContext();
		 * Context envContext = (Context)initContext.lookup("java:/comp/env");
		 * dataSource = (DataSource)envContext.lookup("jdbc/humans"); } catch
		 * (NamingException e) { e.printStackTrace(); logger.error(e); }
		 */
		// TODO Auto-generated constructor stub
	}

	/**
	 * More of an administrator task as you don't need a password..err
	 * 
	 * @param aUsername
	 */
	protected void loadByUsername(String aUsername) {
		userJSON = new JSONObject();
		File f = new File(USER_DATA_ROOT + "/" + aUsername +"/"+aUsername+"-user.json");
		if (f.exists()) {
			JSONParser parser = new JSONParser();
			try {
				Object obj = parser.parse(new FileReader(f));
				userJSON = (JSONObject) obj;
			} catch (IOException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e);
			}
		}
	}

	public HumansUser(String aUsername, String aPassword) {
		this();
		validUser = false;
		this.loadByUsername(aUsername);
		if (verifyPassword(aPassword) == false) {
			userJSON = new JSONObject();
			logger.info("Bad login. Returning empty user");
		} else {
			validUser = true;
		}
	}

	
	public static List<HumansUser> getAllHumansUsers() {
		File f = new File(USER_DATA_ROOT);
		File[] listOfFiles = f.listFiles();
		List<File> filesList = Arrays.asList(listOfFiles);
		List<HumansUser> result = new ArrayList<HumansUser>();
		// now get the usernames
		for(int i=0; i<filesList.size(); i++) {
			File userDirectory = filesList.get(i);
			File userFile = new File(userDirectory.getAbsolutePath()+File.separator+userDirectory.getName()+"-user.json");
			if(userFile.exists()) {
				JSONParser parser = new JSONParser();
				Object obj = null;
				try {
					obj = parser.parse(new FileReader(userFile));
					JSONObject json = (JSONObject)obj;
					String username = json.get("username").toString();
					HumansUser user = new HumansUser();
					user.loadByUsername(username);
					result.add(user);
				} catch(IOException | ParseException | NullPointerException e) {
					e.printStackTrace();
					logger.error(e);
					logger.error("obj="+obj);
					logger.error("userFIle="+userFile);
				}
			}
		}
		return result;
		
	}
	
	
	public List<String> getAllHumansUsers_Usernames() {
		File f = new File(USER_DATA_ROOT);
		File[] listOfFiles = f.listFiles();
		List<File> filesList = Arrays.asList(listOfFiles);
		List<String> result = new ArrayList<String>();
		// now get the usernames
		for(int i=0; i<filesList.size(); i++) {
			File userDirName = filesList.get(i);
			if(f.exists()) {
				result.add(userDirName.getName());
/*				JSONParser parser = new JSONParser();
				try {
					Object obj = parser.parse(new FileReader(userFile));
					JSONObject json = (JSONObject)obj;
					result.add((String)json.get("username"));
				} catch(IOException | ParseException e) {
					e.printStackTrace();
					logger.error(e);
				}
*/			}
		}
		
		
		return result;
	}
	
	/**
	 * A Human user has a bunch of Humans that they follow This is how you add
	 * them..one at a time.
	 * 
	 * 
	 * @param aHuman
	 */
	@SuppressWarnings("unchecked")
	public void addHuman(Human aHuman) {
		Gson gson = new Gson();
		JSONObject humanJObj = (JSONObject) JSONValue
				.parse(gson.toJson(aHuman));

		JSONArray existingUsersHumans = (JSONArray) userJSON.get("humans");
		if (existingUsersHumans == null) {
			existingUsersHumans = new JSONArray();
		}

		// check to see if this human is already in there
		Iterator<Human> iter = existingUsersHumans.iterator();

		while (iter.hasNext()) {
			Object obj = iter.next();
			Human candidateHuman = gson.fromJson(obj.toString(), Human.class);
			// logger.debug("candidate = "+candidateHuman);
			if (candidateHuman.isEquals(aHuman)) {
				// duplicate by the Human isEquals..
				// don't add it
				return;
			}
		}

		// now add it to the array
		existingUsersHumans.add(humanJObj);
		userJSON.put("humans", existingUsersHumans);
		saveUserJSON();
	}
	
	/**
	 * Find a human for this humans user with a specific name
	 * 
	 * @param aHumanName
	 * @return
	 */
	public Human getHumanByName(String aHumanName) {
		Human result = null;
		List<Human> humans = getAllHumans();
		Iterator<Human>iter = humans.iterator();
		while(iter.hasNext()) {
			Human human = iter.next();
			if(human.name.equalsIgnoreCase(aHumanName)) {
				result = human;
			}
		}
		return result;
	}
	
	/**
	 * Returns a list of all the humans for this user
	 * @return
	 */
	public List<Human> getAllHumans() {
		Gson gson = new Gson();
		List<Human> result = new ArrayList<Human>();
		JSONArray allHumans = (JSONArray) this.userJSON.get("humans");
		if(allHumans == null) {
			logger.error("Why is this null? A bad userJSON?");
			logger.error(this.userJSON.toJSONString());
		}
		if(allHumans != null) {
		Iterator<JSONObject>iter = allHumans.iterator();
		while(iter.hasNext()) {
			JSONObject human = iter.next();
			Human aHuman = gson.fromJson(human.toJSONString(), Human.class);
			result.add(aHuman);
		}
		}
		return result;
	}
	
	public List<ServiceUser> getServiceUsersForAllHumansByService(String aService) {
		List<ServiceUser> result = new ArrayList<ServiceUser>();
		List<ServiceUser> all = getServiceUsersForAllHumans();
		for(int i=0; i<all.size(); i++) {
			ServiceUser su = all.get(i);
			if(su.getService().equalsIgnoreCase(aService)) {
				result.add(su);
			}
		}
		return result;
	}

	public List<ServiceUser> getServiceUsersForAllHumans() {
		Gson gson = new Gson();
		List<ServiceUser> result = new ArrayList<ServiceUser>();
		JSONArray allHumans = (JSONArray) this.userJSON.get("humans");
		if(allHumans == null) {
			return result;
		}
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> iter = allHumans.iterator();
		while (iter.hasNext()) {
			JSONObject human = iter.next();
			// logger.debug(obj.toJSONString());
			JSONArray serviceUsers = (JSONArray) human.get("serviceUsers");
			// logger.debug(serviceUsers.toJSONString());
			for (int i = 0; i < serviceUsers.size(); i++) {
				JSONObject serviceUser = (JSONObject) serviceUsers.get(i);
				ServiceUser serviceUserObj = gson.fromJson(
						serviceUser.toJSONString(), ServiceUser.class);
				result.add(serviceUserObj);
				// logger.debug(su);
			}
		}
		return result;
	}

	/*
	 * public void deleteHumanByID(String aID) {
	 * 
	 * }
	 */
	public void deleteHumanByName(String aName) {
		JSONArray humansJObj = (JSONArray) userJSON.get("humans");
		if (humansJObj == null) {
			return;
		}
		Gson gson = new Gson();
		// check to see if this human is already in there
		Iterator<String> iter = humansJObj.iterator();
		while (iter.hasNext()) {
			Human aHuman = gson.fromJson(iter.next(), Human.class);
		}
	}


	public boolean isValidUser() {
		return validUser;
	}

	public static boolean doesUsernameExist(String aUsername) {
		boolean result = false;
		Path startingDir = Paths.get(Constants.getString("USER_DATA_ROOT"));
		Finder finder = new Finder(aUsername + "-user.json");
		try {
			Files.walkFileTree(startingDir, finder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e);
		}
		List<Path> results = finder.results;
		if (results.size() > 0) {
			result = true;
		}
		return result;
	}

	public String getUsername() {
		// logger.debug(userJSON);
		// logger.debug(userJSON.get("username"));
		return (String) userJSON.get("username");

	}

	@SuppressWarnings("unchecked")
	public void setEmail(String aEmail) {
		userJSON.put("email", aEmail);
		this.saveUserJSON();
	}

	public String getEmail() {
		// logger.debug(userJSON);
		// logger.debug(userJSON.get("email"));
		return (String) userJSON.get("email");

	}

	@SuppressWarnings("unchecked")
	public void setUsername(String aUsername) {
		userJSON.put("username", aUsername);
		this.saveUserJSON();
	}

	@SuppressWarnings("unchecked")
	public String getPasswordEnc() {
		return (String) userJSON.get("password");
	}

	@SuppressWarnings("unchecked")
	public void setPassword(String aClearPassword) {
		StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
		String encryptedPassword = passwordEncryptor
				.encryptPassword(aClearPassword);
		userJSON.put("password", encryptedPassword);
		this.saveUserJSON();
	}

	protected boolean verifyPassword(String aPassword) {
		StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
		boolean result = false;
		if (passwordEncryptor.checkPassword(aPassword,
				(String) userJSON.get("password"))) {
			// correct!
			result = true;
		} else {
			// bad login!
			result = false;
			logger.warn("Bad password attempt for " + userJSON.get("username")
					+ " " + aPassword);
		}
		return result;
	}


	/**
	 * Get all of the services this Humans User has assigned twitter,
	 * instagram, flickr, etc., etc.
	 * 
	 * @return
	 */
	public List<String> getServicesAssigned() {
		List<String> result = new ArrayList<String>();
		JSONObject servicesObj = (JSONObject) userJSON.get("services");
		Set<String> keys = servicesObj.keySet();
		Iterator<String> iter = keys.iterator();
		while (iter.hasNext()) {
			result.add(iter.next());
		}
		return result;
	}

	public List<ServiceUser> getServiceUsersForServiceName(String aServiceName) {
		List<ServiceUser>serviceUsers = new ArrayList<ServiceUser>();
		return serviceUsers;
	}
	
	
	/**
	 * 
	 * @param ServiceName
	 *            typically lowercase name of a service, eg twitter, instagram,
	 *            flickr, foursquare
	 * @return a List<String> of the accounts for that service assigned/attached
	 *         on behalf of this humans user in this "coded" format of
	 *         id-username which is a good key to useful files.
	 */
	@Deprecated
	public List<Pair<String,String>> __getServiceUsersForServiceName(String aServiceName) {

		List<Pair<String,String>>aListOfPairs = new ArrayList<Pair<String,String>>();
		//List<String> aListResult = new ArrayList<String>();
		if (aServiceName != null) {
			aServiceName = aServiceName.toLowerCase();
			JSONArray servicesArray = (JSONArray) userJSON.get("services");
			if (servicesArray != null) {
				// logger.debug(userJSON);
				for(int i=0; i<servicesArray.size(); i++) {
					JSONObject obj = (JSONObject)servicesArray.get(i);
					logger.debug(obj.get("flickr"));
				}
				
//				JSONArray serviceArrayOfUsernames = (JSONArray) servicesObj.get(aServiceName);
//				// logger.debug(aServiceName+" "+servicesObj+" "+serviceArrayOfUsernames);
//				// aListResult = serviceArrayOfUsernames.clone();
//				if (serviceArrayOfUsernames != null) {
//					Iterator iter = serviceArrayOfUsernames.iterator();
//					while (iter.hasNext()) {
//						String token = (String)iter.next();
//						String[] elements = token.split("-");
//						String id = elements[0];
//						String username = elements[1];
//						aListOfPairs.add(new Pair<String,String>(id, username));
////						aListResult.add((String) iter.next());
//					}
//				}
			}
		}
		return aListOfPairs;
	}
	
/*	public List<String>getCodedServiceUsersForServiceName(String aServiceName) {
		List<String>aListResult = new ArrayList<String>();
		List<ServiceUser> pairs = getServiceUsersForServiceName(aServiceName);
		for(int i=0; i<pairs.size(); i++) {
			aListResult.add(pairs.get(i).getFirst()+"-"+pairs.get(i).getSecond());
		}
		return aListResult;
	}
*/
	@SuppressWarnings("unchecked")
	public void addServiceForHuman(String aServiceName,
			String aServiceUsername, String aServiceUserID) {
		logger.debug("adding for " + aServiceName + " with:" + aServiceUsername
				+ " and:" + aServiceUserID);
		if (aServiceName == null || aServiceUsername == null
				|| aServiceUserID == null) {
			return;
		}

		aServiceName = aServiceName.toLowerCase();

		String newEntry = aServiceUserID + "-" + aServiceUsername;
		boolean exists = false;
		// get the service out of the user object
		JSONObject servicesObj = (JSONObject) userJSON.get("services");
		logger.debug("current services are "+userJSON.get("services"));

		if (servicesObj == null) {
			servicesObj = new JSONObject();
			userJSON.put("services", servicesObj);
		}
		JSONArray serviceArrayOfUsernames = (JSONArray) servicesObj.get(aServiceName);
		logger.debug("and serviceArray is " + serviceArrayOfUsernames);
		if (serviceArrayOfUsernames != null) {
			Iterator<String> iter = serviceArrayOfUsernames.iterator();
			// check for a duplicate
			while (iter.hasNext()) {
				String entry = iter.next();
				if (entry.equalsIgnoreCase(newEntry)) {
					logger.debug("duplicate. already found service for "
							+ newEntry + " (" + entry + ")");
					exists = true;
					break;
				}
			}
			// it's not there, so update the array
			// remove the old array from the current servicesObj
			// add the new updated array
			if (exists != true) {
				serviceArrayOfUsernames.add(newEntry);
				servicesObj.put(aServiceName, serviceArrayOfUsernames);

				userJSON.put("services", servicesObj);
				logger.debug(userJSON);
				saveUserJSON();
			}
		} else {
			serviceArrayOfUsernames = new JSONArray();
			serviceArrayOfUsernames.add(newEntry);

			servicesObj.put(aServiceName, serviceArrayOfUsernames);
			userJSON.put("services", servicesObj);
			// logger.debug(userJSON);
			saveUserJSON();
		}
	}

	/**
	 * This just breaks apart a service "code" (e.g. 1739934-darthjulian) into
	 * separate userid (1739934) and username (darthjulian)
	 * 
	 * @param aServiceName
	 * @param aServiceCode
	 *            basically userid-username for a service
	 */
	public void removeServiceUser(String aServiceName, String aServiceCode) {
		if (aServiceName == null || aServiceCode == null) {
			return;
		}
		aServiceName = aServiceName.toLowerCase();

		logger.debug("removeServiceUser " + aServiceName + " " + aServiceCode);
		Pattern p = Pattern.compile("-");
		String[] toks = p.split(aServiceCode);
		if (toks == null || toks.length != 2) {
			logger.warn("Attempt to remove service user for " + aServiceName
					+ " but service code is weird (" + aServiceCode + ")");
			return;
		} else {
			removeServiceUser(aServiceName, toks[1], toks[0]);
		}
	}

	public void removeServiceUsersOnBehalfOf(String aOnBehalfOf) {
		JSONObject serviceUsersObj  =null;
		JSONArray serviceUsers = null;
		try {
		JSONArray humans = (JSONArray) userJSON.get("humans");
		//Iterator iter = humansObj.iterator();
		//while(iter != null && iter.hasNext()) {
		for(int j=0; j<humans.size(); j++) {	
		serviceUsersObj = (JSONObject)humans.get(j);
		serviceUsers = (JSONArray)serviceUsersObj.get("serviceUsers");
			for(int i=0; i<serviceUsers.size(); i++)  {
				JSONObject serviceUser = (JSONObject)serviceUsers.get(i);
				if(serviceUser != null && serviceUser.get("onBehalfOf").toString().equalsIgnoreCase(aOnBehalfOf)) {
					serviceUsers.remove(i);
					serviceUsersObj.remove("serviceUsers");
					serviceUsersObj.put("serviceUsers", serviceUsers);
					humans.remove(j);
					//humans.add(serviceUser);
				}
			}
			
		}
		userJSON.remove("humans");
		userJSON.put("humans", humans);
		this.saveUserJSON();
		} catch(ClassCastException e) {
			logger.error(e);
			logger.error(serviceUsersObj);
			logger.error(serviceUsers);
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void removeServiceUser(String aServiceName, String aServiceUsername,
			String aServiceUserID) {
		logger.debug("removing for " + aServiceName + " with:"
				+ aServiceUsername + " and:" + aServiceUserID);
		if (aServiceName == null || aServiceUsername == null
				|| aServiceUserID == null) {
			return;
		}

		aServiceName = aServiceName.toLowerCase();

		String entryToRemove = aServiceUserID + "-" + aServiceUsername;
		boolean exists = false;
		// get the service out of the user object
		JSONObject servicesObj = (JSONObject) userJSON.get("services");
		JSONArray serviceArrayOfUsernames = (JSONArray)servicesObj.get(aServiceName);
		logger.debug("and serviceArray is " + serviceArrayOfUsernames);
		if (serviceArrayOfUsernames != null) {
			Iterator<String> iter = serviceArrayOfUsernames.iterator();
			// check for a duplicate
			while (iter.hasNext()) {
				String entry = iter.next();
				if (entry.equalsIgnoreCase(entryToRemove)) {
					logger.debug("found item to remove: " + entryToRemove
							+ " (" + entry + ")");
					exists = true;
					break;
				}
			}
			// it's not there, so update the array
			// remove the old array from the current userJSON
			// add the new updated array
			if (exists == true) {
				serviceArrayOfUsernames.remove(entryToRemove);
				servicesObj.remove(aServiceName);
				servicesObj.put(aServiceName, serviceArrayOfUsernames);

				userJSON.remove("services");
				userJSON.put("services", servicesObj);
				logger.debug("removing this " + entryToRemove);
				saveUserJSON();
			}
		}
	}

	protected boolean doesServiceUserExist(String aServiceName,
			String aServiceUsername, String aServiceUserID) {
		boolean result = false;

		if (aServiceName == null || aServiceUsername == null
				|| aServiceUserID == null) {
			return result;
		}

		aServiceName = aServiceName.toLowerCase();

		String entryToTest = aServiceUserID + "-" + aServiceUsername;
		JSONObject servicesObj = (JSONObject) userJSON.get("services");
		JSONArray serviceArrayOfUsernames = (JSONArray) servicesObj
				.get(aServiceName);
		if (serviceArrayOfUsernames != null) {
			Iterator<String> iter = serviceArrayOfUsernames.iterator();
			// check for a duplicate
			while (iter.hasNext()) {
				String entry = iter.next();
				if (entry.equalsIgnoreCase(entryToTest)) {
					logger.debug("found item: " + entryToTest + " (" + entry
							+ ")");
					result = true;
					break;
				}
			}
		}
		return result;
	}

	protected void saveUserJSON() {
		try {
			Path startingDir = Paths.get(Constants.getString("USER_DATA_ROOT"));
			// Finder finder = new
			// Finder(request.getParameter("username")+"-user.json");
			// Files.walkFileTree(startingDir, finder);
			// List<Path> results = finder.results;

			String jsonUserString = userJSON.toString();
			Writer output = null;
			File file = new File(startingDir.toFile() + "/"
					+ this.getUsername() + "/" + this.getUsername()+"-user.json");

			
			if(file.exists() == false) {
			List<String> dirs = new ArrayList<String>();
			dirs.add(this.getUsername());
			
			FileUtils.mkDirs(startingDir.toFile(), dirs);
			}

			
			output = new BufferedWriter(new FileWriter(file));
			output.write(jsonUserString);
			output.close();

			logger.debug("Saving User JSON: " + jsonUserString);
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			logger.error(e);
		}

	}
	
	public JSONObject loadHumanStatuses(String aHumanName) {
		JSONObject humanJSON = null;
		BufferedReader br = null;
		JSONParser parser = new JSONParser();
		try {
			Path startingDir = Paths.get(Constants.getString("USER_DATA_ROOT"));
			//String humanName = humanJSON.get("name").toString();
			String fsHumanName = FileUtils.filenameSafeEncode(aHumanName);
			String filename = fsHumanName +"-status.json";
			File file = new File(startingDir.toFile()+"/"+this.getUsername()+"/"+filename);
			
			//InputStreamReader char_input = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8").newDecoder());
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8").newDecoder()));
			//Gson gson = new Gson();
			
			Object obj = parser.parse(br);
			
			humanJSON = (JSONObject)obj;
			

		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.error(e);
				}
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.error(e);
				}
			}
		}
		return humanJSON;
	}

	public void saveHumanStatus(JSONObject humanJSON) {
		try {
			Path startingDir = Paths.get(Constants.getString("USER_DATA_ROOT"));
			String humanName = humanJSON.get("name").toString();
			String fsHumanName = FileUtils.filenameSafeEncode(humanName);
			String filename = fsHumanName +"-status.json";
			File file = new File(startingDir.toFile()+"/"+this.getUsername()+"/"+filename);
			
			if(file.exists() == false) {
			List<String> dirs = new ArrayList<String>();
			dirs.add(this.getUsername());
			
			FileUtils.mkDirs(startingDir.toFile(), dirs);
			}
			OutputStreamWriter char_output = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8").newEncoder());
			char_output.write(humanJSON.toJSONString());
			char_output.flush();
			char_output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e);
		}
		
	}
	
	public String toString() {
		return userJSON.toString();
	}

}
