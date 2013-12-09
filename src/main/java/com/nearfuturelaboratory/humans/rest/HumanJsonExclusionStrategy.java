package com.nearfuturelaboratory.humans.rest;


import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.nearfuturelaboratory.humans.entities.Human;

/**
 * Excludes the password and access_token field
 * @author julian
 *
 */
public class HumanJsonExclusionStrategy implements ExclusionStrategy
{

	@Override
	public boolean shouldSkipField(FieldAttributes aField) {
		//return true;
		System.out.println(aField.getName());

		if ( (aField.getDeclaringClass() == Human.class && aField.getName().equals("humanid")) ) {
			return true;
		}
		if ( (aField.getDeclaringClass() == Human.class && aField.getName().equals("serviceUsers")) ) {
			return false;
		}
		return false;
	}

	@Override
	public boolean shouldSkipClass(Class<?> aClazz) {
		return false;
	}


}


