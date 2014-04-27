package com.nearfuturelaboratory.humans.tumblr.entities;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class TumblrTextPosts {

    @Expose
    private TumblrMeta meta;
    @Expose
    private TumblrResponse response;

    public TumblrMeta getMeta() {
        return meta;
    }

    public void setMeta(TumblrMeta meta) {
        this.meta = meta;
    }

    public TumblrTextPosts withMeta(TumblrMeta meta) {
        this.meta = meta;
        return this;
    }

    public TumblrResponse getResponse() {
        return response;
    }

    public void setResponse(TumblrResponse response) {
        this.response = response;
    }

    public TumblrTextPosts withResponse(TumblrResponse response) {
        this.response = response;
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

