import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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


public class Foursquare2Example {
	 private static final String PROTECTED_RESOURCE_URL = "https://api.foursquare.com/v2/users/self/friends?oauth_token=";
	  private static final Token EMPTY_TOKEN = null;
	  private static String apiKey = "MKGJ3OZYTDNZAI5ZMROF3PAMAUND0ZO2HYRTZYXHIIR5TW1Q";
	  private static String apiSecret = "RJEJLYBMTVI02K5RJ4KTYSOWSYZCTD5KLPZETO30CMGUGIKG";
	  
	public Foursquare2Example() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Constants.load("./WebContent/WEB-INF/lib/dev.app.properties");
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		FoursquareService foursquare = FoursquareService.createFoursquareServiceOnBehalfOfCodedUsername("41-Julian_Bleecker");
		foursquare.getFollowsForUserID("41");
		
		
	
		/*
		OAuthService service = new ServiceBuilder()
		.provider(Foursquare2Api.class)
		.apiKey(apiKey)
		.apiSecret(apiSecret)
		.callback("http://localhost:8080/HumansService/FoursquareLogin")
		.build();
		Scanner in = new Scanner(System.in);

		System.out.println("=== Foursquare2's OAuth Workflow ===");
		System.out.println();

		// Obtain the Authorization URL
		System.out.println("Fetching the Authorization URL...");
		String authorizationUrl = service.getAuthorizationUrl(EMPTY_TOKEN);
		System.out.println("Got the Authorization URL!");
		System.out.println("Now go and authorize Scribe here:");
		System.out.println(authorizationUrl);
		System.out.println("And paste the authorization code here");
		System.out.print(">>");
		Verifier verifier = new Verifier(in.nextLine());
		System.out.println();

		// Trade the Request Token and Verfier for the Access Token
		System.out.println("Trading the Request Token for an Access Token...");
		Token accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
		System.out.println("Got the Access Token!");
		System.out.println("(if your curious it looks like this: " + accessToken + " )");
		System.out.println();

		// Now let's go and ask for a protected resource!
		System.out.println("Now we're going to access a protected resource...");
		OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL + accessToken.getToken());
		service.signRequest(accessToken, request);
		Response response = request.send();
		System.out.println("Got it! Lets see what we found...");
		System.out.println();
		System.out.println(response.getCode());
		System.out.println(response.getBody());

		System.out.println();
		System.out.println("Thats it man! Go and build something awesome with Scribe! :)");*/
	}

}
