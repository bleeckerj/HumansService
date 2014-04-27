
package com.nearfuturelaboratory.humans.tumblr.entities;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class TumblrBlogInfo {

    @Expose
    private String title;
    @Expose
    private int posts;
    @Expose
    private String name;
    @Expose
    private String url;
    @Expose
    private int updated;
    @Expose
    private String description;
    @Expose
    private boolean ask;
    @Expose
    private boolean ask_anon;
    @Expose
    private int likes;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TumblrBlogInfo withTitle(String title) {
        this.title = title;
        return this;
    }

    public int getPosts() {
        return posts;
    }

    public void setPosts(int posts) {
        this.posts = posts;
    }

    public TumblrBlogInfo withPosts(int posts) {
        this.posts = posts;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TumblrBlogInfo withName(String name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public TumblrBlogInfo withUrl(String url) {
        this.url = url;
        return this;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    public TumblrBlogInfo withUpdated(int updated) {
        this.updated = updated;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TumblrBlogInfo withDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean isAsk() {
        return ask;
    }

    public void setAsk(boolean ask) {
        this.ask = ask;
    }

    public TumblrBlogInfo withAsk(boolean ask) {
        this.ask = ask;
        return this;
    }

    public boolean isAsk_anon() {
        return ask_anon;
    }

    public void setAsk_anon(boolean ask_anon) {
        this.ask_anon = ask_anon;
    }

    public TumblrBlogInfo withAsk_anon(boolean ask_anon) {
        this.ask_anon = ask_anon;
        return this;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public TumblrBlogInfo withLikes(int likes) {
        this.likes = likes;
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