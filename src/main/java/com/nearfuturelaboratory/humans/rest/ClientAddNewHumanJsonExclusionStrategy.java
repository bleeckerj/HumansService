package com.nearfuturelaboratory.humans.rest;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.nearfuturelaboratory.humans.entities.Human;
import com.nearfuturelaboratory.humans.entities.HumansUser;

/**
 * Created by julian on 1/21/14.
 */
public class ClientAddNewHumanJsonExclusionStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes aField) {
        if ( (aField.getDeclaringClass() == Human.class && aField.getName().equals("humanid")) ||
                (aField.getName().equals("lastUpdated"))
                ) {
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
