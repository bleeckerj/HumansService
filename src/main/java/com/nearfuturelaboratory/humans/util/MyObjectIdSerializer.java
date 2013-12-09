package com.nearfuturelaboratory.humans.util;

import java.lang.reflect.Type;

import org.bson.types.ObjectId;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MyObjectIdSerializer implements JsonSerializer<ObjectId> {
	public JsonElement serialize(ObjectId src, Type typeOfSrc, JsonSerializationContext context) {

		JsonElement result = new JsonPrimitive(src.toString());
		return result;
	}
	
	public ObjectId deserialize(JsonElement json, Type typeOfT,
	   JsonDeserializationContext context) throws JsonParseException
	{
	   try {return new ObjectId(json.getAsJsonObject()
	       .get("$oid").getAsString()); }
	   catch (Exception e) { return null; }
	}
}