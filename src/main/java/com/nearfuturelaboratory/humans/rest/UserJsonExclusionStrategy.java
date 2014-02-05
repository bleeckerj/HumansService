package com.nearfuturelaboratory.humans.rest;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.nearfuturelaboratory.humans.entities.HumansUser;

/**
 * Excludes the password and access_token field
 * @author julian
 *
 */
public class UserJsonExclusionStrategy implements ExclusionStrategy
{

	@Override
	public boolean shouldSkipField(FieldAttributes aField) {
		if ( (aField.getDeclaringClass() == HumansUser.class && aField.getName().equals("password")) ||
			 (aField.getDeclaringClass() == HumansUser.class && aField.getName().equals("access_token")) ) {
			return true;
		} else {
			return false;
		}

		
	}

	@Override
	public boolean shouldSkipClass(Class<?> aClazz) {
		return false;
	}
	

}
