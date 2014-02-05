import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Scanner;

import org.apache.commons.configuration.ConfigurationException;
import org.scribe.builder.*;
import org.scribe.builder.api.*;
import org.scribe.model.*;
import org.scribe.oauth.*;

import com.nearfuturelaboratory.humans.service.*;
import com.nearfuturelaboratory.util.Constants;

public class TwitterExample
{
	private static final String PROTECTED_RESOURCE_URL = "https://api.twitter.com/1.1/account/verify_credentials.json";
	static TwitterService twitter;
	public static void main(String[] args)
	{
		
		try {
			Constants.load("./WebContent/WEB-INF/lib/dev.app.properties");
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// If you choose to use a callback, "oauth_verifier" will be the return value by Twitter (request param)
		OAuthService service = new ServiceBuilder()
		.provider(TwitterApi.class)
		.apiKey("09ARKva0K7HMz1DW1GUg")
		.apiSecret("rwy7rZ2Uu3lkliYMfOaJD4UeUHFebDqXXrBgjnT8Rw")
		.callback("http://nearfuturelaboratory.com/scrumpy-twitter")
		.build();
		Scanner in = new Scanner(System.in);

		System.out.println("=== Twitter's OAuth Workflow ===");
		System.out.println();
		Token accessToken = deserializeToken(args[0]);
		if(accessToken == null) {


			// Obtain the Request Token
			System.out.println("Fetching the Request Token...");
			Token requestToken = service.getRequestToken();
			System.out.println("Got the Request Token!");
			System.out.println();

			String authUrl = service.getAuthorizationUrl(requestToken);
			System.out.println(authUrl);

			System.out.println("Now go and authorize Scribe here:");
			System.out.println(service.getAuthorizationUrl(requestToken));
			System.out.println("And paste the verifier here");
			System.out.print(">>");
			Verifier verifier = new Verifier(in.nextLine());
			System.out.println();

			// Trade the Request Token and Verfier for the Access Token
			System.out.println("Trading the Request Token for an Access Token...");
			accessToken = service.getAccessToken(requestToken, verifier);
			twitter = new TwitterService(accessToken);
			
			
			//twitter.unpackFollowsFor("2801571");
			//twitter.getFollowsLocal("2801571");

			System.out.println("Got the Access Token!");
			System.out.println("(if your curious it looks like this: " + accessToken + " )");
			System.out.println();

			serializeToken(accessToken);

		}


		// = new TwitterService(accessToken);
		
		//twitter.getFollows("2801571");
		//twitter.getUserBasicForUserID("2801571", true);
		//long time = twitter.getUserBasicLastModifiedTime("2801571");
		//System.out.println(time);
		/*
    // Now let's go and ask for a protected resource!
    System.out.println("Now we're going to access a protected resource...");
    OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
    //request.addBodyParameter("status", "this is sparta! *");
    service.signRequest(accessToken, request);
    Response response = request.send();
    System.out.println("Got it! Lets see what we found...");
    System.out.println();
    System.out.println(response.getBody());

    System.out.println();
    System.out.println("Thats it man! Go and build something awesome with Scribe! :)");
		 */
	}

	static void serializeToken(Token aToken) {
		try{
			//use buffering
			OutputStream file = new FileOutputStream( "twitter-token-for-"+twitter.getThisUser().get("screen_name")+".ser" );
			OutputStream buffer = new BufferedOutputStream( file );
			ObjectOutput output = new ObjectOutputStream( buffer );
			try{
				output.writeObject(aToken);
			}
			finally{
				output.close();
			}
		}  
		catch(IOException ex){
			//fLogger.log(Level.SEVERE, "Cannot perform output.", ex);
			ex.printStackTrace();
		}

	}

	static Token deserializeToken(String aTwitterUsername) {
		Token result = null;
		try{
			//use buffering
			InputStream file = new FileInputStream( "twitter-token-for-"+aTwitterUsername+".ser" );
			InputStream buffer = new BufferedInputStream( file );
			ObjectInput input = new ObjectInputStream ( buffer );
			try{
				//deserialize the List
				result = (Token)input.readObject();
				//display its data
				System.out.println("Deserialized Token is: "+result);
			}
			finally{
				input.close();
			}
		}
		catch(ClassNotFoundException ex){
			//fLogger.log(Level.SEVERE, "Cannot perform input. Class not found.", ex);
			ex.printStackTrace();
		}
		catch(IOException ex){
			//		      fLogger.log(Level.SEVERE, "Cannot perform input.", ex);
			ex.printStackTrace();
		}
		return result;
	}


}