
package com.nearfuturelaboratory.humans.twitter.entities.generated;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

@Generated("org.jsonschema2pojo")
@Entity(value="user", noClassnameStored = true)
public class TwitterStatusUser {

    @Expose
    private String id;
    @SerializedName("id_str")
    @Property("id_str")
    @Expose
    private String idStr;
    @Expose
    private String name;
    @SerializedName("screen_name")
    @Property("screen_name")
    @Expose
    private String screenName;
    @Expose
    private String location;
    @Expose
    private String description;
    @Expose
    private String url;

    @SerializedName("protected")
    @Expose
    private Boolean _protected;

    @SerializedName("followers_count")
    @Property("followers_count")
    @Expose
    private Integer followersCount;

    @SerializedName("friends_count")
    @Property("friends_count")
    @Expose
    private Integer friendsCount;

    @SerializedName("listed_count")
    @Property("listed_count")
    @Expose
    private Integer listedCount;

    @SerializedName("created_at")
    @Property("created_at")
    @Expose
    private String createdAt;

    @SerializedName("favourites_count")
    @Property("favourites_count")
    @Expose
    private Integer favouritesCount;

    @SerializedName("utc_offset")
    @Property("utc_offset")
    @Expose
    private Integer utcOffset;

    @SerializedName("time_zone")
    @Expose
    private String timeZone;

    @SerializedName("geo_enabled")
    @Property("geo_enabled")
    @Expose
    private Boolean geoEnabled;

    @Expose
    private Boolean verified;

    @SerializedName("statuses_count")
    @Property("statuses_count")
    @Expose
    private Integer statusesCount;

    @Expose
    private String lang;

    @SerializedName("contributors_enabled")
    @Property("contributors_enabled")
    @Expose
    private Boolean contributorsEnabled;

    @SerializedName("is_translator")
    @Property("is_translator")
    @Expose
    private Boolean isTranslator;

    @SerializedName("profile_background_color")
    @Property("profile_background_color")
    @Expose
    private String profileBackgroundColor;

    @SerializedName("profile_background_image_url")
    @Property("profile_background_image_url")
    @Expose
    private String profileBackgroundImageUrl;

    @SerializedName("profile_background_image_url_https")
    @Property("profile_background_image_url_https")
    @Expose
    private String profileBackgroundImageUrlHttps;

    @SerializedName("profile_background_tile")
    @Property("profile_background_tile")
    @Expose
    private Boolean profileBackgroundTile;

    @SerializedName("profile_image_url")
    @Property("profile_image_url")
    @Expose
    private String profileImageUrl;

    @SerializedName("profile_image_url_https")
    @Property("profile_image_url_https")
    @Expose
    private String profileImageUrlHttps;

    @SerializedName("profile_link_color")
    @Property("profile_link_color")
    @Expose
    private String profileLinkColor;

    @SerializedName("profile_sidebar_border_color")
    @Property("profile_sidebar_border_color")
    @Expose
    private String profileSidebarBorderColor;

    @SerializedName("profile_sidebar_fill_color")
    @Property("profile_sidebar_fill_color")
    @Expose
    private String profileSidebarFillColor;

    @SerializedName("profile_text_color")
    @Property("profile_text_color")
    @Expose
    private String profileTextColor;

    @SerializedName("profile_use_background_image")
    @Property("profile_use_background_image")
    @Expose
    private Boolean profileUseBackgroundImage;

    @SerializedName("default_profile")
    @Property("default_profile")
    @Expose
    private Boolean defaultProfile;

    @SerializedName("default_profile_image")
    @Property("default_profile_image")
    @Expose
    private Boolean defaultProfileImage;

    @Expose
    private Boolean following;

    @SerializedName("follow_request_sent")
    @Property("follow_request_sent")
    @Expose
    private Boolean followRequestSent;

    @Expose
    private Boolean notifications;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TwitterStatusUser withId(String id) {
        this.id = id;
        return this;
    }

    public String getIdStr() {
        return idStr;
    }

    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }

    public TwitterStatusUser withIdStr(String idStr) {
        this.idStr = idStr;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TwitterStatusUser withName(String name) {
        this.name = name;
        return this;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public TwitterStatusUser withScreenName(String screenName) {
        this.screenName = screenName;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public TwitterStatusUser withLocation(String location) {
        this.location = location;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TwitterStatusUser withDescription(String description) {
        this.description = description;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public TwitterStatusUser withUrl(String url) {
        this.url = url;
        return this;
    }

    public Boolean getProtected() {
        return _protected;
    }

    public void setProtected(Boolean _protected) {
        this._protected = _protected;
    }

    public TwitterStatusUser withProtected(Boolean _protected) {
        this._protected = _protected;
        return this;
    }

    public Integer getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Integer followersCount) {
        this.followersCount = followersCount;
    }

    public TwitterStatusUser withFollowersCount(Integer followersCount) {
        this.followersCount = followersCount;
        return this;
    }

    public Integer getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(Integer friendsCount) {
        this.friendsCount = friendsCount;
    }

    public TwitterStatusUser withFriendsCount(Integer friendsCount) {
        this.friendsCount = friendsCount;
        return this;
    }

    public Integer getListedCount() {
        return listedCount;
    }

    public void setListedCount(Integer listedCount) {
        this.listedCount = listedCount;
    }

    public TwitterStatusUser withListedCount(Integer listedCount) {
        this.listedCount = listedCount;
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public TwitterStatusUser withCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Integer getFavouritesCount() {
        return favouritesCount;
    }

    public void setFavouritesCount(Integer favouritesCount) {
        this.favouritesCount = favouritesCount;
    }

    public TwitterStatusUser withFavouritesCount(Integer favouritesCount) {
        this.favouritesCount = favouritesCount;
        return this;
    }

    public Integer getUtcOffset() {
        return utcOffset;
    }

    public void setUtcOffset(Integer utcOffset) {
        this.utcOffset = utcOffset;
    }

    public TwitterStatusUser withUtcOffset(Integer utcOffset) {
        this.utcOffset = utcOffset;
        return this;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public TwitterStatusUser withTimeZone(String timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    public Boolean getGeoEnabled() {
        return geoEnabled;
    }

    public void setGeoEnabled(Boolean geoEnabled) {
        this.geoEnabled = geoEnabled;
    }

    public TwitterStatusUser withGeoEnabled(Boolean geoEnabled) {
        this.geoEnabled = geoEnabled;
        return this;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public TwitterStatusUser withVerified(Boolean verified) {
        this.verified = verified;
        return this;
    }

    public Integer getStatusesCount() {
        return statusesCount;
    }

    public void setStatusesCount(Integer statusesCount) {
        this.statusesCount = statusesCount;
    }

    public TwitterStatusUser withStatusesCount(Integer statusesCount) {
        this.statusesCount = statusesCount;
        return this;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public TwitterStatusUser withLang(String lang) {
        this.lang = lang;
        return this;
    }

    public Boolean getContributorsEnabled() {
        return contributorsEnabled;
    }

    public void setContributorsEnabled(Boolean contributorsEnabled) {
        this.contributorsEnabled = contributorsEnabled;
    }

    public TwitterStatusUser withContributorsEnabled(Boolean contributorsEnabled) {
        this.contributorsEnabled = contributorsEnabled;
        return this;
    }

    public Boolean getIsTranslator() {
        return isTranslator;
    }

    public void setIsTranslator(Boolean isTranslator) {
        this.isTranslator = isTranslator;
    }

    public TwitterStatusUser withIsTranslator(Boolean isTranslator) {
        this.isTranslator = isTranslator;
        return this;
    }

    public String getProfileBackgroundColor() {
        return profileBackgroundColor;
    }

    public void setProfileBackgroundColor(String profileBackgroundColor) {
        this.profileBackgroundColor = profileBackgroundColor;
    }

    public TwitterStatusUser withProfileBackgroundColor(String profileBackgroundColor) {
        this.profileBackgroundColor = profileBackgroundColor;
        return this;
    }

    public String getProfileBackgroundImageUrl() {
        return profileBackgroundImageUrl;
    }

    public void setProfileBackgroundImageUrl(String profileBackgroundImageUrl) {
        this.profileBackgroundImageUrl = profileBackgroundImageUrl;
    }

    public TwitterStatusUser withProfileBackgroundImageUrl(String profileBackgroundImageUrl) {
        this.profileBackgroundImageUrl = profileBackgroundImageUrl;
        return this;
    }

    public String getProfileBackgroundImageUrlHttps() {
        return profileBackgroundImageUrlHttps;
    }

    public void setProfileBackgroundImageUrlHttps(String profileBackgroundImageUrlHttps) {
        this.profileBackgroundImageUrlHttps = profileBackgroundImageUrlHttps;
    }

    public TwitterStatusUser withProfileBackgroundImageUrlHttps(String profileBackgroundImageUrlHttps) {
        this.profileBackgroundImageUrlHttps = profileBackgroundImageUrlHttps;
        return this;
    }

    public Boolean getProfileBackgroundTile() {
        return profileBackgroundTile;
    }

    public void setProfileBackgroundTile(Boolean profileBackgroundTile) {
        this.profileBackgroundTile = profileBackgroundTile;
    }

    public TwitterStatusUser withProfileBackgroundTile(Boolean profileBackgroundTile) {
        this.profileBackgroundTile = profileBackgroundTile;
        return this;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public TwitterStatusUser withProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    public String getProfileImageUrlHttps() {
        return profileImageUrlHttps;
    }

    public void setProfileImageUrlHttps(String profileImageUrlHttps) {
        this.profileImageUrlHttps = profileImageUrlHttps;
    }

    public TwitterStatusUser withProfileImageUrlHttps(String profileImageUrlHttps) {
        this.profileImageUrlHttps = profileImageUrlHttps;
        return this;
    }

    public String getProfileLinkColor() {
        return profileLinkColor;
    }

    public void setProfileLinkColor(String profileLinkColor) {
        this.profileLinkColor = profileLinkColor;
    }

    public TwitterStatusUser withProfileLinkColor(String profileLinkColor) {
        this.profileLinkColor = profileLinkColor;
        return this;
    }

    public String getProfileSidebarBorderColor() {
        return profileSidebarBorderColor;
    }

    public void setProfileSidebarBorderColor(String profileSidebarBorderColor) {
        this.profileSidebarBorderColor = profileSidebarBorderColor;
    }

    public TwitterStatusUser withProfileSidebarBorderColor(String profileSidebarBorderColor) {
        this.profileSidebarBorderColor = profileSidebarBorderColor;
        return this;
    }

    public String getProfileSidebarFillColor() {
        return profileSidebarFillColor;
    }

    public void setProfileSidebarFillColor(String profileSidebarFillColor) {
        this.profileSidebarFillColor = profileSidebarFillColor;
    }

    public TwitterStatusUser withProfileSidebarFillColor(String profileSidebarFillColor) {
        this.profileSidebarFillColor = profileSidebarFillColor;
        return this;
    }

    public String getProfileTextColor() {
        return profileTextColor;
    }

    public void setProfileTextColor(String profileTextColor) {
        this.profileTextColor = profileTextColor;
    }

    public TwitterStatusUser withProfileTextColor(String profileTextColor) {
        this.profileTextColor = profileTextColor;
        return this;
    }

    public Boolean getProfileUseBackgroundImage() {
        return profileUseBackgroundImage;
    }

    public void setProfileUseBackgroundImage(Boolean profileUseBackgroundImage) {
        this.profileUseBackgroundImage = profileUseBackgroundImage;
    }

    public TwitterStatusUser withProfileUseBackgroundImage(Boolean profileUseBackgroundImage) {
        this.profileUseBackgroundImage = profileUseBackgroundImage;
        return this;
    }

    public Boolean getDefaultProfile() {
        return defaultProfile;
    }

    public void setDefaultProfile(Boolean defaultProfile) {
        this.defaultProfile = defaultProfile;
    }

    public TwitterStatusUser withDefaultProfile(Boolean defaultProfile) {
        this.defaultProfile = defaultProfile;
        return this;
    }

    public Boolean getDefaultProfileImage() {
        return defaultProfileImage;
    }

    public void setDefaultProfileImage(Boolean defaultProfileImage) {
        this.defaultProfileImage = defaultProfileImage;
    }

    public TwitterStatusUser withDefaultProfileImage(Boolean defaultProfileImage) {
        this.defaultProfileImage = defaultProfileImage;
        return this;
    }

    public Boolean getFollowing() {
        return following;
    }

    public void setFollowing(Boolean following) {
        this.following = following;
    }

    public TwitterStatusUser withFollowing(Boolean following) {
        this.following = following;
        return this;
    }

    public Boolean getFollowRequestSent() {
        return followRequestSent;
    }

    public void setFollowRequestSent(Boolean followRequestSent) {
        this.followRequestSent = followRequestSent;
    }

    public TwitterStatusUser withFollowRequestSent(Boolean followRequestSent) {
        this.followRequestSent = followRequestSent;
        return this;
    }

    public Boolean getNotifications() {
        return notifications;
    }

    public void setNotifications(Boolean notifications) {
        this.notifications = notifications;
    }

    public TwitterStatusUser withNotifications(Boolean notifications) {
        this.notifications = notifications;
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
