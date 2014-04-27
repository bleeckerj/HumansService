package com.nearfuturelaboratory.humans.tumblr.entities;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class TumblrTextPost {

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
    private String state;
    @Expose
    private String format;
    @Expose
    private String reblog_key;
    @Expose
    private List<String> tags = new ArrayList<String>();
    @Expose
    private int note_count;
    @Expose
    private String title;
    @Expose
    private String body;

    public String getBlog_name() {
        return blog_name;
    }

    public void setBlog_name(String blog_name) {
        this.blog_name = blog_name;
    }

    public TumblrTextPost withBlog_name(String blog_name) {
        this.blog_name = blog_name;
        return this;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TumblrTextPost withId(int id) {
        this.id = id;
        return this;
    }

    public String getPost_url() {
        return post_url;
    }

    public void setPost_url(String post_url) {
        this.post_url = post_url;
    }

    public TumblrTextPost withPost_url(String post_url) {
        this.post_url = post_url;
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TumblrTextPost withType(String type) {
        this.type = type;
        return this;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public TumblrTextPost withDate(String date) {
        this.date = date;
        return this;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public TumblrTextPost withTimestamp(int timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public TumblrTextPost withState(String state) {
        this.state = state;
        return this;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public TumblrTextPost withFormat(String format) {
        this.format = format;
        return this;
    }

    public String getReblog_key() {
        return reblog_key;
    }

    public void setReblog_key(String reblog_key) {
        this.reblog_key = reblog_key;
    }

    public TumblrTextPost withReblog_key(String reblog_key) {
        this.reblog_key = reblog_key;
        return this;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public TumblrTextPost withTags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    public int getNote_count() {
        return note_count;
    }

    public void setNote_count(int note_count) {
        this.note_count = note_count;
    }

    public TumblrTextPost withNote_count(int note_count) {
        this.note_count = note_count;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TumblrTextPost withTitle(String title) {
        this.title = title;
        return this;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public TumblrTextPost withBody(String body) {
        this.body = body;
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