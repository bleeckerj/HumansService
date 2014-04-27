package com.nearfuturelaboratory.humans.tumblr.entities;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class TumblrResponse {

    @Expose
    private String blog;
    @Expose
    private List<TumblrTextPost> posts = new ArrayList<TumblrTextPost>();
    @Expose
    private int total_posts;

    public String getBlog() {
        return blog;
    }

    public void setBlog(String blog) {
        this.blog = blog;
    }

    public TumblrResponse withBlog(String blog) {
        this.blog = blog;
        return this;
    }

    public List<TumblrTextPost> getPosts() {
        return posts;
    }

    public void setPosts(List<TumblrTextPost> posts) {
        this.posts = posts;
    }

    public TumblrResponse withPosts(List<TumblrTextPost> posts) {
        this.posts = posts;
        return this;
    }

    public int getTotal_posts() {
        return total_posts;
    }

    public void setTotal_posts(int total_posts) {
        this.total_posts = total_posts;
    }

    public TumblrResponse withTotal_posts(int total_posts) {
        this.total_posts = total_posts;
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