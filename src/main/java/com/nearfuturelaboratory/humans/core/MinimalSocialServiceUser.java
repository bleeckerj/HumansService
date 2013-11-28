package com.nearfuturelaboratory.humans.core;

import org.mongodb.morphia.annotations.Embedded;

import com.nearfuturelaboratory.humans.entities.ServiceEntry;

public abstract class MinimalSocialServiceUser {

	@Embedded
	protected ServiceEntry onBehalfOf;
		
	public abstract String getImageURL();
	
	public abstract String getUserID();
	
	public abstract String getUsername();
	
	public abstract String getFirstName();
	
	public abstract String getLastName();
	
	public abstract String getServiceName();
	
	public abstract String getLargeImageURL();
	
	public void setOnBehalfOf(ServiceEntry entry) {
		onBehalfOf = entry;
	}
	
	public ServiceEntry getOnBehalfOf() {
		return onBehalfOf;
	}
	
	
	
}

