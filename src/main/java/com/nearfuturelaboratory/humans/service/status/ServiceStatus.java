package com.nearfuturelaboratory.humans.service.status;

import java.util.Date;

import com.google.gson.JsonObject;
import com.mongodb.DBObject;

public abstract class ServiceStatus implements Comparable<ServiceStatus> {
	
	public abstract JsonObject getStatusJSON();
	
	public abstract long getCreated();
	
	public abstract String getService();
	
	public ServiceStatus() {}
	
	public Date getCreatedDate() {
		return new Date(getCreated());
	}

	//public int compareTo(ServiceStatus o);

	//public void setStatusJSON(JsonObject obj);
	
	public int compareTo(ServiceStatus aO) {
		Date otherDate = aO.getCreatedDate();
		//otherDate.setTime(aO.getCreated()*1000l);
		return otherDate.compareTo(this.getCreatedDate());

	}


}
