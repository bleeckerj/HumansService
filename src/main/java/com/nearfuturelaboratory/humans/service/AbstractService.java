package com.nearfuturelaboratory.humans.service;

import java.util.List;

import org.scribe.model.Token;

import com.nearfuturelaboratory.humans.entities.MinimalSocialServiceUser;
import com.nearfuturelaboratory.humans.entities.ServiceUser;
import com.nearfuturelaboratory.humans.exception.BadAccessTokenException;
import com.nearfuturelaboratory.humans.service.status.ServiceStatus;

public interface AbstractService {

	
	public void initServiceOnBehalfOfUsername(String aUsername) throws BadAccessTokenException;
	
	public MinimalSocialServiceUser serviceRequestUserBasic();
	
	public MinimalSocialServiceUser getThisUser();
	
	public MinimalSocialServiceUser serviceRequestUserBasicForUserID(String aUserID);
	
	public void freshenStatus();
	
	public List<ServiceStatus> getStatus();
	
//	public List<ServiceStatus> getStatusForUserID(String aUserID);
		
	public List<ServiceStatus> serviceRequestStatus();
	
	public boolean localFriendsIsFresh();
	
	public List<MinimalSocialServiceUser> getFriends();
	
	//public List<ServiceUser> getFollowsFor(String aUserID);
	
	
	public void serviceRequestFriends();
	
	public void serializeToken(Token aToken, ServiceUser aUser);
	
	public boolean localUserBasicIsFresh();
	
	public boolean localServiceStatusIsFresh();
	
	
	
	
	
	
	
}
