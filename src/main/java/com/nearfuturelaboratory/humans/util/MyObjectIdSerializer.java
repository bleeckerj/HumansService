package com.nearfuturelaboratory.humans.util;

import java.lang.reflect.Type;

import org.bson.types.ObjectId;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MyObjectIdSerializer implements JsonSerializer<ObjectId> {
	public JsonElement serialize(ObjectId src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(src.toStringMongod());
	}
}