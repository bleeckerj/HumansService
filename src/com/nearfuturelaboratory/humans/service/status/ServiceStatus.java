package com.nearfuturelaboratory.humans.service.status;

import java.util.Date;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public interface ServiceStatus extends Comparable<ServiceStatus> {
	
	public  JsonObject getStatusJSON();
	
	public  long getCreated();
	
	public  Date getCreatedDate();
	//public int compareTo(ServiceStatus o);
	
	public  void setStatusJSON(JsonObject obj);
	
	public int compareTo(ServiceStatus aO);


}
