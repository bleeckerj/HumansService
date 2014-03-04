package com.nearfuturelaboratory.humans.entities;

import org.mongodb.morphia.annotations.Embedded;

import java.util.Date;

public abstract class MinimalSocialServiceUser {

	@Embedded
	protected ServiceEntry onBehalfOf;

    public abstract String getIdStr();

	public abstract String getImageURL();
	
	public abstract String getUserID();
	
	public abstract String getUsername();
	
	public abstract String getFirstName();
	
	public abstract String getLastName();

    public abstract String getFullName();
	
	public abstract String getServiceName();
	
	public abstract String getLargeImageURL();

    public abstract Date getLastUpdated();
	
	public void setOnBehalfOf(ServiceEntry entry) {
		onBehalfOf = entry;
	}
	
	public ServiceEntry getOnBehalfOf() {
		return onBehalfOf;
	}
	
	
	
}

