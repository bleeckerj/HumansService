package com.nearfuturelaboratory.humans.service.status;

import java.util.ArrayList;
import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jayway.jsonpath.JsonPath;

public class InstagramStatus implements ServiceStatus {

	protected JsonObject statusJSON;
	protected String id;
	protected long created_time;
	protected JsonObject images;
	protected JsonElement caption;

	public static final String STANDARD_RESOLUTION = "standard_resolution";
	public static final String THUMBNAIL_RESOLUTION = "thumbnail_resolution";
	public static final String LOW_RESOLUTION = "low_resolution";


	public InstagramStatus(JsonObject aStatusJSON) {
		// TODO Auto-generated constructor stub
		statusJSON = aStatusJSON;
		//id = statusJSON.get("id").getAsString(); //JsonPath.read(statusJSON, "id");
		id = statusJSON.getAsJsonPrimitive("id").getAsString();
	}


	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	/**
	 * @return the created
	 */
	public long getCreated() {
		return created_time*1000l;
	}

	public Date getCreatedDate() {
		Date d = new Date();
		d.setTime(created_time*1000l);
		return d;
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


//	public void setCaption(JsonElement aCaption) {
//		caption = (JsonObject)aCaption;
//	}
	

	public String getCaptionText() {
		String result = null;
		if(caption != null && (caption instanceof JsonNull) == false) {
			result = ((JsonObject)caption).get("text").toString();
		}
		return result;
	}

	public String getImageURL_StandardResolution() {
		//		System.out.println(images.isJsonObject());
		//		System.out.println(images.isJsonArray());
		JsonObject im = images.getAsJsonObject();
		JsonObject std = im.getAsJsonObject("standard_resolution");
		//JsonObject obj = JsonPath.read(im, "$thumbnail");
		return std.get("url").getAsString();
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
		InstagramStatus other = (InstagramStatus) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}



	public JsonObject getStatusJSON() {
		JsonObject result = new JsonObject();
		result.addProperty("service-identifier", "instagram");
		result.add("status", statusJSON);
		return result;
	}



	public int compareTo(ServiceStatus aO) {
		Date otherDate = aO.getCreatedDate();
		//otherDate.setTime(aO.getCreated()*1000l);
		return otherDate.compareTo(this.getCreatedDate());
	}


	/*	public String toString() {
		return statusJSON.toString();
	}
	 */

	public void setStatusJSON(JsonObject aJsonElement) {
		// TODO Auto-generated method stub
		statusJSON = aJsonElement.getAsJsonObject();
	}



}
