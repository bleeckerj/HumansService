
package com.nearfuturelaboratory.humans.twitter.entities.generated;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.mongodb.morphia.annotations.Entity;

@Generated("org.jsonschema2pojo")
@Entity(value="sizes", noClassnameStored = true)
public class Sizes {

    @Expose
    private Large large;
    @Expose
    private Thumb thumb;
    @Expose
    private Small small;
    @Expose
    private Medium medium;

    public Large getLarge() {
        return large;
    }

    public void setLarge(Large large) {
        this.large = large;
    }

    public Sizes withLarge(Large large) {
        this.large = large;
        return this;
    }

    public Thumb getThumb() {
        return thumb;
    }

    public void setThumb(Thumb thumb) {
        this.thumb = thumb;
    }

    public Sizes withThumb(Thumb thumb) {
        this.thumb = thumb;
        return this;
    }

    public Small getSmall() {
        return small;
    }

    public void setSmall(Small small) {
        this.small = small;
    }

    public Sizes withSmall(Small small) {
        this.small = small;
        return this;
    }

    public Medium getMedium() {
        return medium;
    }

    public void setMedium(Medium medium) {
        this.medium = medium;
    }

    public Sizes withMedium(Medium medium) {
        this.medium = medium;
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
