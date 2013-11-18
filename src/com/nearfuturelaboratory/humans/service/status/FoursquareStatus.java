package com.nearfuturelaboratory.humans.service.status;

import java.util.ArrayList;
import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;

@Deprecated
public class FoursquareStatus extends ServiceStatus {

	
	protected JsonObject statusJSON;
	protected String id;
	protected long createdAt;
	protected JsonObject venue;
	
	protected JsonObject images;

	public FoursquareStatus() {
		// TODO Auto-generated constructor stub
	}


	public long getCreated() {
		return createdAt*1000l;
	}

	public Date getCreatedDate() {
		Date d = new Date();
		d.setTime(createdAt*1000l);
		return d;
	}
	
	public int compareTo(ServiceStatus aO) {
		Date otherDate = aO.getCreatedDate();
		return otherDate.compareTo(this.getCreatedDate());
//		return this.getCreatedDate().compareTo(otherDate);
	}
	
	
	/**
	 * @return the statusJSON
	 */
	public JsonObject getStatusJSON() {
		JsonObject result = new JsonObject();
		result.addProperty("service-identifier", "foursquare");
		result.add("status", statusJSON);
		return result;
	}
	/**
	 * @param aStatusJSON the statusJSON to set
	 */
	public void setStatusJSON(JsonObject aStatusJSON) {
		statusJSON = aStatusJSON;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param aId the id to set
	 */
	public void setId(String aId) {
		id = aId;
	}

	/**
	 * @return the createdAt
	 */
	public long getCreatedAt() {
		return createdAt;
	}

	/**
	 * @param aCreatedAt the createdAt to set
	 */
	public void setCreatedAt(long aCreatedAt) {
		createdAt = aCreatedAt;
	}

	/**
	 * @return the venue
	 */
	public JsonObject getVenue() {
		return venue;
	}

	/**
	 * @param aVenue the venue to set
	 */
	public void setVenue(JsonObject aVenue) {
		venue = aVenue;
	}

	/**
	 * @return the images
	 */
	public JsonObject getImages() {
		return images;
	}

	/**
	 * @param aImages the images to set
	 */
	public void setImages(JsonObject aImages) {
		images = aImages;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		FoursquareStatus other = (FoursquareStatus) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	public String toString() {
		String result;
		if(venue != null) {
			result = venue.get("name").toString();
		} else {
			result = super.toString();
		}
		return result;
	}

}
