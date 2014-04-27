package com.nearfuturelaboratory.humans.tumblr.entities;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class TumblrPhotoPost {

    @Expose
    private String blog_name;
    @Expose
    private int id;
    @Expose
    private String post_url;
    @Expose
    private String type;
    @Expose
    private String date;
    @Expose
    private int timestamp;
    @Expose
    private String format;
    @Expose
    private String reblog_key;
    @Expose
    private List<Object> tags = new ArrayList<Object>();
    @Expose
    private int note_count;
    @Expose
    private String caption;
    @Expose
    private List<TumblrPhoto> photos = new ArrayList<TumblrPhoto>();

    public String getBlog_name() {
        return blog_name;
    }

    public void setBlog_name(String blog_name) {
        this.blog_name = blog_name;
    }

    public TumblrPhotoPost withBlog_name(String blog_name) {
        this.blog_name = blog_name;
        return this;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TumblrPhotoPost withId(int id) {
        this.id = id;
        return this;
    }

    public String getPost_url() {
        return post_url;
    }

    public void setPost_url(String post_url) {
        this.post_url = post_url;
    }

    public TumblrPhotoPost withPost_url(String post_url) {
        this.post_url = post_url;
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TumblrPhotoPost withType(String type) {
        this.type = type;
        return this;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public TumblrPhotoPost withDate(String date) {
        this.date = date;
        return this;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public TumblrPhotoPost withTimestamp(int timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public TumblrPhotoPost withFormat(String format) {
        this.format = format;
        return this;
    }

    public String getReblog_key() {
        return reblog_key;
    }

    public void setReblog_key(String reblog_key) {
        this.reblog_key = reblog_key;
    }

    public TumblrPhotoPost withReblog_key(String reblog_key) {
        this.reblog_key = reblog_key;
        return this;
    }

    public List<Object> getTags() {
        return tags;
    }

    public void setTags(List<Object> tags) {
        this.tags = tags;
    }

    public TumblrPhotoPost withTags(List<Object> tags) {
        this.tags = tags;
        return this;
    }

    public int getNote_count() {
        return note_count;
    }

    public void setNote_count(int note_count) {
        this.note_count = note_count;
    }

    public TumblrPhotoPost withNote_count(int note_count) {
        this.note_count = note_count;
        return this;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public TumblrPhotoPost withCaption(String caption) {
        this.caption = caption;
        return this;
    }

    public List<TumblrPhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<TumblrPhoto> photos) {
        this.photos = photos;
    }

    public TumblrPhotoPost withPhotos(List<TumblrPhoto> photos) {
        this.photos = photos;
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