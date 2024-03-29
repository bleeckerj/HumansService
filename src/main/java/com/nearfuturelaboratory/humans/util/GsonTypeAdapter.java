package com.nearfuturelaboratory.humans.util;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import org.bson.types.ObjectId;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GsonTypeAdapter 
{
	public static enum GsonAdapterType
	{
		DESERIALIZER/*,
		SERIALIZER*/
	}
	
	/**
	 * GsonTypeAdapter.getGsonBuilder
	 * @param g - Deserialize from JSON or Serialize to JSON
	 * @return GsonBuilder object with type adapters for MongoDB registered
	 */
	public static GsonBuilder getGsonBuilder(GsonAdapterType g)
	{		
		GsonBuilder gb = new GsonBuilder();
		switch (g)
		{
			case DESERIALIZER:
				gb.registerTypeAdapter(ObjectId.class, new GsonTypeAdapter.ObjectIdDeserializer());
				//gb.registerTypeAdapter(Date.class, new GsonTypeAdapter.DateDeserializer());				
				break;
/*			case SERIALIZER:
				gb.registerTypeAdapter(ObjectId.class, new GsonTypeAdapter.ObjectIdSerializer());
				gb.registerTypeAdapter(Date.class, new GsonTypeAdapter.DateSerializer());
				break;
*/			default:
				return null;
		}		
		return gb;
	}
	
	/**
	 * ObjectIdDeserializer.deserialize
	 * @return Bson.Types.ObjectId
	 */
	public static class ObjectIdDeserializer implements JsonDeserializer<ObjectId> 
	{
		@Override
		public ObjectId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
		{
			try
			{
				return new ObjectId(json.getAsJsonObject().get("$oid").getAsString());
			}
			catch (Exception e)
			{
				return null;
			}
		}
	}
	
	/**
	 * ObjectIdSerializer.serialize
	 * @return $oid JsonObject from BSON ObjectId
	 */
	public static class ObjectIdSerializer implements JsonSerializer<ObjectId> 
	{
		@Override
		public JsonElement serialize(ObjectId id, Type typeOfT, JsonSerializationContext context)
		{
			JsonObject jo = new JsonObject();
			jo.addProperty("$oid", id.toStringMongod());
			return jo;
		}
	}	
	
	/**
	 * DateDeserializer.deserialize
	 * @return Java.util.Date
	 */
	public static class DateDeserializer implements JsonDeserializer<Date> 
	{
		@Override
		public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException
			{
				Date d = null;
				SimpleDateFormat f2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				try 
				{
					d = f2.parse(json.getAsJsonObject().get("$date").getAsString());
				}
				catch (ParseException e)	
				{
					d = null;
				}
				return d;
			}
	}
	
	/**
	 * DateSerializer.serialize
	 * @return date JsonElement
	 */
	public static class DateSerializer implements JsonSerializer<Date> 
	{
		@Override
		public JsonElement serialize(Date date, Type typeOfT, JsonSerializationContext context)
		{
			Date d = (Date)date;
	        	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		        JsonObject jo = new JsonObject();
			jo.addProperty("$date", format.format(d));
			return jo;
		}
	}	
}