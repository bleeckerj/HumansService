package com.nearfuturelaboratory.humans.flickr.entities;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.annotations.Version;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nearfuturelaboratory.humans.service.status.ServiceStatus;

@Entity("status")
public class FlickrStatus extends ServiceStatus {
	final static Logger logger = Logger.getLogger(com.nearfuturelaboratory.humans.flickr.entities.FlickrStatus.class);
	@Version
	@Property ("version")
	private Long version;
	protected Date lastUpdated;

	@Transient
	protected String service="flickr";

	
	@Id
	protected String id;
	protected String owner;
	protected String secret;
	protected String server;
	protected String farm;
	protected String title;
	protected String ispublic;
	protected String isfriend;
	protected String isfamily;
	protected Map<String, String> description;
	protected Integer license;
	protected Long dateupload;
	protected Date datetaken;
	protected Long datetakengranularity;
	protected String ownername;
	protected Integer iconserver;
	protected Integer iconfarm;
	protected String originalsecret;
	protected String originalformat;
	protected Long lastupdate;
	protected Double latitude;
	protected Double longitude;
	protected Double accuracy;
	protected Integer context;
	protected String tags;
	protected String machine_tags;
	protected Integer o_width;
	protected Integer o_height;
	protected Integer views;
	protected String media;
	protected String media_status;
	protected String pathalias;
	protected String url_sq;
	protected Integer height_sq;
	protected Integer width_sq;

	protected String url_t;
	protected Integer height_t;
	protected Integer width_t;
	
	protected String url_s;
	protected Integer height_s;
	protected Integer width_s;

	protected String url_q;
	protected Integer height_q;
	protected Integer width_q;

	protected String url_m;
	protected Integer height_m;
	protected Integer width_m;
	
	protected String url_n;
	protected Integer height_n;
	protected Integer width_n;

	protected String url_z;
	protected Integer height_z;
	protected Integer width_z;

	protected String url_c;
	protected Integer height_c;
	protected Integer width_c;

	protected String url_l;
	protected Integer height_l;
	protected Integer width_l;

	protected String url_o;
	protected Integer height_o;
	protected Integer width_o;
	
	@PrePersist void prePersist() {
		lastUpdated = new Date();
	}

	public Long getVersion() {
		return version;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public String getId() {
		return id;
	}

	public String getOwner() {
		return owner;
	}

	public String getSecret() {
		return secret;
	}

	public String getServer() {
		return server;
	}

	public String getFarm() {
		return farm;
	}

	public String getTitle() {
		return title;
	}

	public String getIspublic() {
		return ispublic;
	}

	public String getIsfriend() {
		return isfriend;
	}

	public String getIsfamily() {
		return isfamily;
	}

	public Map<String, String> getDescription() {
		return description;
	}

	public Integer getLicense() {
		return license;
	}

	public Long getDateupload() {
		return dateupload;
	}

	public Date getDatetaken() {
		return datetaken;
	}

	public Long getDatetakengranularity() {
		return datetakengranularity;
	}

	public String getOwnername() {
		return ownername;
	}

	public Integer getIconserver() {
		return iconserver;
	}

	public Integer getIconfarm() {
		return iconfarm;
	}

	public String getOriginalsecret() {
		return originalsecret;
	}

	public String getOriginalformat() {
		return originalformat;
	}

	public Long getLastupdate() {
		return lastupdate;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public Double getAccuracy() {
		return accuracy;
	}

	public Integer getContext() {
		return context;
	}

	public String getTags() {
		return tags;
	}

	public String getMachine_tags() {
		return machine_tags;
	}

	public Integer getO_width() {
		return o_width;
	}

	public Integer getO_height() {
		return o_height;
	}

	public Integer getViews() {
		return views;
	}

	public String getMedia() {
		return media;
	}

	public String getMedia_status() {
		return media_status;
	}

	public String getPathalias() {
		return pathalias;
	}

	public String getUrl_sq() {
		return url_sq;
	}

	public Integer getHeight_sq() {
		return height_sq;
	}

	public Integer getWidth_sq() {
		return width_sq;
	}

	public String getUrl_t() {
		return url_t;
	}

	public Integer getHeight_t() {
		return height_t;
	}

	public Integer getWidth_t() {
		return width_t;
	}

	public String getUrl_s() {
		return url_s;
	}

	public Integer getHeight_s() {
		return height_s;
	}

	public Integer getWidth_s() {
		return width_s;
	}

	public String getUrl_q() {
		return url_q;
	}

	public Integer getHeight_q() {
		return height_q;
	}

	public Integer getWidth_q() {
		return width_q;
	}

	public String getUrl_m() {
		return url_m;
	}

	public Integer getHeight_m() {
		return height_m;
	}

	public Integer getWidth_m() {
		return width_m;
	}

	public String getUrl_n() {
		return url_n;
	}

	public Integer getHeight_n() {
		return height_n;
	}

	public Integer getWidth_n() {
		return width_n;
	}

	public String getUrl_z() {
		return url_z;
	}

	public Integer getHeight_z() {
		return height_z;
	}

	public Integer getWidth_z() {
		return width_z;
	}

	public String getUrl_c() {
		return url_c;
	}

	public Integer getHeight_c() {
		return height_c;
	}

	public Integer getWidth_c() {
		return width_c;
	}

	public String getUrl_l() {
		return url_l;
	}

	public Integer getHeight_l() {
		return height_l;
	}

	public Integer getWidth_l() {
		return width_l;
	}

	public String getUrl_o() {
		return url_o;
	}

	public Integer getHeight_o() {
		return height_o;
	}

	public Integer getWidth_o() {
		return width_o;
	}

	@Override
	public JsonObject getStatusJSON() {
		JsonObject obj = new Gson().toJsonTree(this, this.getClass()).getAsJsonObject();
		obj.addProperty("service", "flickr");
		return obj;
	}

	@Override
	/**
	 * Use date uploaded for sorting
	 */
	public long getCreated() {
		return this.getDateupload().longValue();
	}


}
