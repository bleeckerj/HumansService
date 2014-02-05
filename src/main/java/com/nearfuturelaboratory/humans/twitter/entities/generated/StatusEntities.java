
package com.nearfuturelaboratory.humans.twitter.entities.generated;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

@Generated("org.jsonschema2pojo")
@Entity(value="entities", noClassnameStored = true)
public class StatusEntities {
//TODO find a status sample that has these three properties completed so we know what they look like
    @Expose
    private List<Object> hashtags = new ArrayList<Object>();
    @Expose
    private List<Object> symbols = new ArrayList<Object>();
    @Expose
    private List<Object> urls = new ArrayList<Object>();
    @SerializedName("user_mentions")
    @Property("user_mentions")
    @Expose
    private List<Object> userMentions = new ArrayList<Object>();
    @SerializedName("media")
    @Expose
    private List<Media> media = new ArrayList<Media>();

    public List<Object> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<Object> hashtags) {
        this.hashtags = hashtags;
    }

    public StatusEntities withHashtags(List<Object> hashtags) {
        this.hashtags = hashtags;
        return this;
    }

    public List<Object> getSymbols() {
        return symbols;
    }

    public void setSymbols(List<Object> symbols) {
        this.symbols = symbols;
    }

    public StatusEntities withSymbols(List<Object> symbols) {
        this.symbols = symbols;
        return this;
    }

    public List<Object> getUrls() {
        return urls;
    }

    public void setUrls(List<Object> urls) {
        this.urls = urls;
    }

    public StatusEntities withUrls(List<Object> urls) {
        this.urls = urls;
        return this;
    }

    public List<Object> getUserMentions() {
        return userMentions;
    }

    public void setUserMentions(List<Object> userMentions) {
        this.userMentions = userMentions;
    }

    public StatusEntities withUserMentions(List<Object> userMentions) {
        this.userMentions = userMentions;
        return this;
    }

    public List<Media> getMedia() {
        return media;
    }

    public void setMedia(List<Media> media) {
        this.media = media;
    }

    public StatusEntities withMedia(List<Media> media) {
        this.media = media;
        return this;
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
