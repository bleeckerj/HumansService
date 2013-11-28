package com.nearfuturelaboratory.humans.entities;

import java.util.List;

public class UserEntities {
		List<String> hashtags;
		List<String> urls;
		List<String> user_mentions;
		public List<String> getHashtags() {
			return hashtags;
		}
		public void setHashtags(List<String> aHashtags) {
			hashtags = aHashtags;
		}
		public List<String> getUrls() {
			return urls;
		}
		public void setUrls(List<String> aUrls) {
			urls = aUrls;
		}
		public List<String> getUser_mentions() {
			return user_mentions;
		}
		public void setUser_mentions(List<String> aUser_mentions) {
			user_mentions = aUser_mentions;
		}
		@Override
		public String toString() {
			return "UserEntities [hashtags=" + hashtags + ", urls=" + urls
					+ ", user_mentions=" + user_mentions + "]";
		}
		
		
}
