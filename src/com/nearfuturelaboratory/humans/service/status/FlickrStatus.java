package com.nearfuturelaboratory.humans.service.status;

import java.util.Date;

import com.google.gson.JsonObject;

public class FlickrStatus implements ServiceStatus {

	protected JsonObject statusJSON;
	protected String owner;
	protected Date datetaken;
	protected String title;
	protected String url_sq, height_sq, width_sq;
	protected String url_t, height_t, width_t;
	protected String url_s, height_s, width_s;
	protected String url_q, height_q, width_q;
	protected String url_m, height_m, width_m;
	protected String url_n, height_n, width_n;
	protected String url_z, height_z, width_z;
	protected String url_c, height_c, width_c;
	
	
	public FlickrStatus() {
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return id;
	}

	public void setId(String aId) {
		id = aId;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param aOwner the owner to set
	 */
	public void setOwner(String aOwner) {
		owner = aOwner;
	}

	/**
	 * @return the datetaken
	 */
	public Date getDatetaken() {
		return datetaken;
	}
	
	/**
	 * @param aDatetaken the datetaken to set
	 */
	public void setDatetaken(Date aDatetaken) {
		datetaken = aDatetaken;
	}
	
	public long getCreated() {
		return datetaken.getTime();
	}
	
	public Date getCreatedDate() {
		return getDatetaken();
	}

	@Override
	public JsonObject getStatusJSON() {
		JsonObject result = new JsonObject();
		result.addProperty("service-identifier", "flickr");
		result.add("status", statusJSON);
		return result;
	}


	public void setStatusJSON(JsonObject aObj) {
		statusJSON = aObj;		
	}

	@Override
	public int compareTo(ServiceStatus aO) {
		Date otherDate = aO.getCreatedDate();
		return otherDate.compareTo(this.getCreatedDate());
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String aTitle) {
		title = aTitle;
	}

	public String getUrl_sq() {
		return url_sq;
	}

	public void setUrl_sq(String aUrl_sq) {
		url_sq = aUrl_sq;
	}

	public String getHeight_sq() {
		return height_sq;
	}

	public void setHeight_sq(String aHeight_sq) {
		height_sq = aHeight_sq;
	}

	public String getWidth_sq() {
		return width_sq;
	}

	public void setWidth_sq(String aWidth_sq) {
		width_sq = aWidth_sq;
	}

	public String getUrl_t() {
		return url_t;
	}

	public void setUrl_t(String aUrl_t) {
		url_t = aUrl_t;
	}

	public String getHeight_t() {
		return height_t;
	}

	public void setHeight_t(String aHeight_t) {
		height_t = aHeight_t;
	}

	public String getWidth_t() {
		return width_t;
	}

	public void setWidth_t(String aWidth_t) {
		width_t = aWidth_t;
	}

	public String getUrl_s() {
		return url_s;
	}

	public void setUrl_s(String aUrl_s) {
		url_s = aUrl_s;
	}

	public String getHeight_s() {
		return height_s;
	}

	public void setHeight_s(String aHeight_s) {
		height_s = aHeight_s;
	}

	public String getWidth_s() {
		return width_s;
	}

	public void setWidth_s(String aWidth_s) {
		width_s = aWidth_s;
	}

	public String getUrl_q() {
		return url_q;
	}

	public void setUrl_q(String aUrl_q) {
		url_q = aUrl_q;
	}

	public String getHeight_q() {
		return height_q;
	}

	public void setHeight_q(String aHeight_q) {
		height_q = aHeight_q;
	}

	public String getWidth_q() {
		return width_q;
	}

	public void setWidth_q(String aWidth_q) {
		width_q = aWidth_q;
	}

	public String getUrl_m() {
		return url_m;
	}

	public void setUrl_m(String aUrl_m) {
		url_m = aUrl_m;
	}

	public String getHeight_m() {
		return height_m;
	}

	public void setHeight_m(String aHeight_m) {
		height_m = aHeight_m;
	}

	public String getWidth_m() {
		return width_m;
	}

	public void setWidth_m(String aWidth_m) {
		width_m = aWidth_m;
	}

	public String getUrl_n() {
		return url_n;
	}

	public void setUrl_n(String aUrl_n) {
		url_n = aUrl_n;
	}

	public String getHeight_n() {
		return height_n;
	}

	public void setHeight_n(String aHeight_n) {
		height_n = aHeight_n;
	}

	public String getWidth_n() {
		return width_n;
	}

	public void setWidth_n(String aWidth_n) {
		width_n = aWidth_n;
	}

	public String getUrl_z() {
		return url_z;
	}

	public void setUrl_z(String aUrl_z) {
		url_z = aUrl_z;
	}

	public String getHeight_z() {
		return height_z;
	}

	public void setHeight_z(String aHeight_z) {
		height_z = aHeight_z;
	}

	public String getWidth_z() {
		return width_z;
	}

	public void setWidth_z(String aWidth_z) {
		width_z = aWidth_z;
	}

	public String getUrl_c() {
		return url_c;
	}

	public void setUrl_c(String aUrl_c) {
		url_c = aUrl_c;
	}

	public String getHeight_c() {
		return height_c;
	}

	public void setHeight_c(String aHeight_c) {
		height_c = aHeight_c;
	}

	public String getWidth_c() {
		return width_c;
	}

	public void setWidth_c(String aWidth_c) {
		width_c = aWidth_c;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FlickrStatus other = (FlickrStatus) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		return true;
	}

	protected String id;

	
}
