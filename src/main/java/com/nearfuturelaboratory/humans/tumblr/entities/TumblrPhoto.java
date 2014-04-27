package com.nearfuturelaboratory.humans.tumblr.entities;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class TumblrPhoto {

    @Expose
    private String caption;
    @Expose
    private List<TumblrAlt_size> alt_sizes = new ArrayList<TumblrAlt_size>();

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public TumblrPhoto withCaption(String caption) {
        this.caption = caption;
        return this;
    }

    public List<TumblrAlt_size> getAlt_sizes() {
        return alt_sizes;
    }

    public void setAlt_sizes(List<TumblrAlt_size> alt_sizes) {
        this.alt_sizes = alt_sizes;
    }

    public TumblrPhoto withAlt_sizes(List<TumblrAlt_size> alt_sizes) {
        this.alt_sizes = alt_sizes;
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