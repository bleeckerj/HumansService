
package com.nearfuturelaboratory.humans.twitter.entities.generated;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.nearfuturelaboratory.humans.entities.ServiceEntry;
import com.nearfuturelaboratory.humans.service.status.ServiceStatus;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.utils.IndexDirection;

import javax.annotation.Generated;
import java.util.Date;

@Generated("org.jsonschema2pojo")
@Entity(value="status", noClassnameStored = true)
public class TwitterStatus extends ServiceStatus {

    @SerializedName("status_on_behalf_of")
    @Embedded
    private ServiceEntry onBehalfOf;
//    @SerializedName("via-service-user")
//    @Property("via-service-user")
//    @Reference
//    private MinimalSocialServiceUser viaServiceUser;


    @SerializedName("created_at")
    @Property("created_at")
    @Expose
    @Indexed(value = IndexDirection.ASC, name = "created_at", unique = false, dropDups = false)
    private Date createdAt;
    @Expose
    @Id
    @Property("status_id")
    private long id;
    @SerializedName("id_str")
    @Property("id_str")
    @Expose
    private String idStr;
    @Expose
    private String text;
    @Expose
    private String source;
    @Expose
    private Boolean truncated;
    @SerializedName("in_reply_to_status_id")
    @Property("in_reply_to_status_id")
    @Expose
    private String inReplyToStatusId;
    @SerializedName("in_reply_to_status_id_str")
    @Property("in_reply_to_status_id_str")
    @Expose
    private String inReplyToStatusIdStr;
    @SerializedName("in_reply_to_user_id")
    @Property("in_reply_to_user_id")
    @Expose
    private String inReplyToUserId;
    @SerializedName("in_reply_to_user_id_str")
    @Property("in_reply_to_user_id_str")
    @Expose
    private String inReplyToUserIdStr;
    @SerializedName("in_reply_to_screen_name")
    @Property("in_reply_to_screen_name")
    @Expose
    private String inReplyToScreenName;
    @Expose
    private TwitterStatusUser user;
    @Expose
    private Coordinates coordinates;
    @Expose
    private Place place;
//    @Expose
//    private Object contributors;
    @SerializedName("retweet_count")
    @Property("retweet_count")
    @Expose
    private Integer retweetCount;
    @SerializedName("favorite_count")
    @Property("favorite_count")
    @Expose
    private Integer favoriteCount;
    @Expose
    private StatusEntities entities;
    @Expose
    private Boolean favorited;
    @Expose
    private Boolean retweeted;
    @SerializedName("possibly_sensitive")
    @Property("possibly_sensitive")
    @Expose
    private Boolean possiblySensitive;
    @Expose
    private String lang;

    protected Date lastUpdated;


    @PrePersist
    void prePersist() {
        lastUpdated = new Date();
        created = getCreatedAt().getTime();
    }

    public ServiceEntry getOnBehalfOf() {
        return onBehalfOf;
    }

    public void setOnBehalfOf(ServiceEntry onBehalfOf) {
        this.onBehalfOf = onBehalfOf;
    }


    public Date getLastUpdated() {
        if(lastUpdated == null) lastUpdated = new Date();
        return lastUpdated;
    }

    public String getId_str() {
        return user.getIdStr();
    }


    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public TwitterStatus withCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TwitterStatus withId(long id) {
        this.id = id;
        return this;
    }

    public String getIdStr() {
        return idStr;
    }

    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }

    public TwitterStatus withIdStr(String idStr) {
        this.idStr = idStr;
        return this;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TwitterStatus withText(String text) {
        this.text = text;
        return this;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public TwitterStatus withSource(String source) {
        this.source = source;
        return this;
    }

    public Boolean getTruncated() {
        return truncated;
    }

    public void setTruncated(Boolean truncated) {
        this.truncated = truncated;
    }

    public TwitterStatus withTruncated(Boolean truncated) {
        this.truncated = truncated;
        return this;
    }

    public String getInReplyToStatusId() {
        return inReplyToStatusId;
    }

    public void setInReplyToStatusId(String inReplyToStatusId) {
        this.inReplyToStatusId = inReplyToStatusId;
    }

    public TwitterStatus withInReplyToStatusId(String inReplyToStatusId) {
        this.inReplyToStatusId = inReplyToStatusId;
        return this;
    }

    public String getInReplyToStatusIdStr() {
        return inReplyToStatusIdStr;
    }

    public void setInReplyToStatusIdStr(String inReplyToStatusIdStr) {
        this.inReplyToStatusIdStr = inReplyToStatusIdStr;
    }

    public TwitterStatus withInReplyToStatusIdStr(String inReplyToStatusIdStr) {
        this.inReplyToStatusIdStr = inReplyToStatusIdStr;
        return this;
    }

    public String getInReplyToUserId() {
        return inReplyToUserId;
    }

    public void setInReplyToUserId(String inReplyToUserId) {
        this.inReplyToUserId = inReplyToUserId;
    }

    public TwitterStatus withInReplyToUserId(String inReplyToUserId) {
        this.inReplyToUserId = inReplyToUserId;
        return this;
    }

    public String getInReplyToUserIdStr() {
        return inReplyToUserIdStr;
    }

    public void setInReplyToUserIdStr(String inReplyToUserIdStr) {
        this.inReplyToUserIdStr = inReplyToUserIdStr;
    }

    public TwitterStatus withInReplyToUserIdStr(String inReplyToUserIdStr) {
        this.inReplyToUserIdStr = inReplyToUserIdStr;
        return this;
    }

    public String getInReplyToScreenName() {
        return inReplyToScreenName;
    }

    public void setInReplyToScreenName(String inReplyToScreenName) {
        this.inReplyToScreenName = inReplyToScreenName;
    }

    public TwitterStatus withInReplyToScreenName(String inReplyToScreenName) {
        this.inReplyToScreenName = inReplyToScreenName;
        return this;
    }

    public TwitterStatusUser getUser() {
        return user;
    }

    public void setUser(TwitterStatusUser user) {
        this.user = user;
    }

    public TwitterStatus withUser(TwitterStatusUser twitterStatusUser) {
        this.user = twitterStatusUser;
        return this;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public TwitterStatus withCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public TwitterStatus withPlace(Place place) {
        this.place = place;
        return this;
    }

//    public Object getContributors() {
//        return contributors;
//    }
//
//    public void setContributors(Object contributors) {
//        this.contributors = contributors;
//    }
//
//    public TwitterStatus withContributors(Object contributors) {
//        this.contributors = contributors;
//        return this;
//    }

    public Integer getRetweetCount() {
        return retweetCount;
    }

    public void setRetweetCount(Integer retweetCount) {
        this.retweetCount = retweetCount;
    }

    public TwitterStatus withRetweetCount(Integer retweetCount) {
        this.retweetCount = retweetCount;
        return this;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public TwitterStatus withFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
        return this;
    }

    public StatusEntities getEntities() {
        return entities;
    }

    public void setEntities(StatusEntities entities) {
        this.entities = entities;
    }

    public TwitterStatus withEntities(StatusEntities entities) {
        this.entities = entities;
        return this;
    }

    public Boolean getFavorited() {
        return favorited;
    }

    public void setFavorited(Boolean favorited) {
        this.favorited = favorited;
    }

    public TwitterStatus withFavorited(Boolean favorited) {
        this.favorited = favorited;
        return this;
    }

    public Boolean getRetweeted() {
        return retweeted;
    }

    public void setRetweeted(Boolean retweeted) {
        this.retweeted = retweeted;
    }

    public TwitterStatus withRetweeted(Boolean retweeted) {
        this.retweeted = retweeted;
        return this;
    }

    public Boolean getPossiblySensitive() {
        return possiblySensitive;
    }

    public void setPossiblySensitive(Boolean possiblySensitive) {
        this.possiblySensitive = possiblySensitive;
    }

    public TwitterStatus withPossiblySensitive(Boolean possiblySensitive) {
        this.possiblySensitive = possiblySensitive;
        return this;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public TwitterStatus withLang(String lang) {
        this.lang = lang;
        return this;
    }

    public JsonObject getStatusJSON() {
        JsonObject obj = new Gson().toJsonTree(this, this.getClass()).getAsJsonObject();
        obj.addProperty("service", "twitter");
        return obj;
    }

    public String getService() {
        return "twitter";
    }
    @Property("created")
    public long created;
    public long getCreated() {
        return this.createdAt.getTime();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

}
