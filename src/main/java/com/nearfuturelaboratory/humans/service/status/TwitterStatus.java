package com.nearfuturelaboratory.humans.service.status;

import java.util.Date;

//import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Deprecated
public class TwitterStatus extends ServiceStatus {
	
	protected int id;
	protected String text;
	protected Date created_at;
	protected JsonObject statusJSON;
	
	public TwitterStatus() {
		// TODO Auto-generated constructor stub
	}

	public String getText() {
		return text;
	}


	public JsonObject getStatusJSON() {
		JsonObject result = new JsonObject();
		result.addProperty("service-identifier", "twitter");
		result.add("status", statusJSON);
		return result;
	}
	

	public void setStatusJSON(JsonObject aObj) {
		statusJSON = aObj;
		
	}
	
	public JsonObject getAbbreviatedStatusJSON() {
		return statusJSON;
	}


	public long getCreated() {
		return created_at.getTime();
	}


	public Date getCreatedDate() {
		return created_at;
	}
	
	public int compareTo(ServiceStatus aO) {
		Date otherDate = aO.getCreatedDate();
		return otherDate.compareTo(this.getCreatedDate());
//		return this.getCreatedDate().compareTo(otherDate);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TwitterStatus other = (TwitterStatus) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
