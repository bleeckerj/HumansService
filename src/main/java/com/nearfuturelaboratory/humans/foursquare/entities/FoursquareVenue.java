package com.nearfuturelaboratory.humans.foursquare.entities;

import java.util.List;
import java.util.Map;

public class FoursquareVenue {

	String id;
	String name;
	FoursquareContact contact;
	FoursquareLocation location;
	String canonicalUrl;
	List<FoursquareCategory> categories;
	Boolean verified;
	Map<String, String>stats;
	String url;
	Map<String, String>price;
	String rating;
	Map<String, String>menu;
	@Override
	public String toString() {
		return "FoursquareVenue [id=" + id + ", name=" + name + ", contact="
				+ contact + ", location=" + location + ", canonicalUrl="
				+ canonicalUrl + ", categories=" + categories + ", verified="
				+ verified + ", stats=" + stats + ", url=" + url + ", price="
				+ price + ", rating=" + rating + ", menu=" + menu + "]";
	}
	
	
	
}

class FoursquareLocation {
	String address;
	String crossStreet;
	Double lat;
	Double lng;
	Double radius50;
	Double radius90;
	String postcalCode;
	String cc;
	String city;
	String state;
	String country;
	@Override
	public String toString() {
		return "FoursquareLocation [address=" + address + ", crossStreet="
				+ crossStreet + ", lat=" + lat + ", lng=" + lng + ", radius50="
				+ radius50 + ", radius90=" + radius90 + ", postcalCode="
				+ postcalCode + ", cc=" + cc + ", city=" + city + ", state="
				+ state + ", country=" + country + "]";
	}
	
}

class FoursquareContact {
	String phone;
	String formattedPhone;
	@Override
	public String toString() {
		return "FoursquareContact [phone=" + phone + ", formattedPhone="
				+ formattedPhone + "]";
	}
	
}

class FriendVisits {
	Integer count;
	String summary;
	List<FriendVisitsItems> items;
	@Override
	public String toString() {
		return "FriendVisits [count=" + count + ", summary=" + summary
				+ ", items=" + items + "]";
	}
	
}

class FriendVisitsItems {
	Integer visitedCount;
	Boolean liked;
	FoursquareFriend user;
	@Override
	public String toString() {
		return "FriendVisitsItems [visitedCount=" + visitedCount + ", liked="
				+ liked + ", user=" + user + "]";
	}
	
}

class FoursquarePhoto {
	String prefix;
	String suffix;
		
	protected String getSquare(int sideSize) {
		return prefix+sideSize+"x"+sideSize+suffix;
	}
	
	@Override
	public String toString() {
		return "FoursquarePhoto [prefix=" + prefix + ", suffix=" + suffix + "]";
	}
	
}

class FoursquareCategory {
	String id;
	String name;
	String pluralName;
	String shortName;
	FoursquarePhoto icon;
	@Override
	public String toString() {
		return "FoursquareCategory [id=" + id + ", name=" + name
				+ ", pluralName=" + pluralName + ", shortName=" + shortName
				+ ", icon=" + icon + "]";
	}
	
}

