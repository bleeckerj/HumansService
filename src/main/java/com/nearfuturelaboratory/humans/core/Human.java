/**
 * 
 */
package com.nearfuturelaboratory.humans.core;

import java.util.*;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

/**
 * @author julian
 *
 */
@Deprecated
public class Human {

	protected String name;
	protected String id;
	protected Set<ServiceUser> serviceUsers;
	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.test.Test");

	/**
	 * 
	 */
	public Human() {
		serviceUsers = new HashSet<ServiceUser>();
	}

	public Human(String aName) {
		// construct by name, meaning load it in
		this();
		this.name = aName;
	}

	public void addServiceUser(String aUsername, String aServiceID, String aService, String aOnBehalfOf, String aImageURL)
	{
		ServiceUser serviceUser = new ServiceUser();
		serviceUser.setUsername(aUsername);
		serviceUser.setServiceID(aServiceID);
		serviceUser.setService(aService);
		serviceUser.setOnBehalfOf(aOnBehalfOf);
		serviceUser.setImageURL(aImageURL);
		serviceUsers.add(serviceUser);
	}

	public void addServiceUserAs(ServiceUser aServiceUser) {
		if(serviceUsers.contains(aServiceUser)) {
			return;
		} else {
			serviceUsers.add(aServiceUser);
		}
	}

	
	/*	public void addServiceUser(String aCodedUsername, String aService, String aOnBehalfOf)
	{

	}
	 */	

	public String getName() {
		return name;
	}

	public void setName(String aName) {
		name = aName;
	}

	public String getID() {
		return id;
	}

	public void setID(String aID) {
		id = aID;
	}

	public Set<ServiceUser> getServiceUsers() {
		return serviceUsers;
	}

	public void setServiceUsers(HashSet<ServiceUser> aServiceUsers) {
		serviceUsers = aServiceUsers;
	}

	
	public String toString() {
		Gson gson = new Gson();
		String result;
		synchronized(gson.toJson(this)) {
			result = gson.toJson(this);
		}
		return result;

	}

	/*	public static Human getHumanForHumanUser(HumansUser aHumanUser, String aHumanName) {
		Human result = null;
		return result;
	}
	 */
	/**
	 * Don't know .. maybe 
	 * @param obj
	 * @return
	 */
	public boolean isEquals(Object obj) {
		if (obj == this) {
			return true;
		} 
		if (obj instanceof Human) {
			Human other = (Human) obj; 
			return (Objects.equals(this.name, other.name) /*||
					Objects.equals(this.id, this.id)*/); 
			//Objects.equals(this.serviceUsers, other.serviceUsers);
		} 
		return false;
	}

	public void addServiceUser(ServiceUser aServiceUserObj) {
		serviceUsers.add(aServiceUserObj);

	}



}
