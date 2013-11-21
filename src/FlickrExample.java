import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.scribe.builder.*;
import org.scribe.builder.api.*;
import org.scribe.model.*;
import org.scribe.oauth.*;

import com.nearfuturelaboratory.humans.service.FlickrService;
import com.nearfuturelaboratory.util.Constants;
import com.nearfuturelaboratory.util.file.Find.Finder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FlickrExample
{
  private static final String PROTECTED_RESOURCE_URL = "http://api.flickr.com/services/rest/";
	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.test.Test");

  public static void main(String[] args)
  {
	try {
		Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/dev.app.properties");
		String props = "/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/static-logger.properties";
		PropertyConfigurator.configureAndWatch(props );

	} catch (ConfigurationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	
	String USERS_DB_PATH_ROOT = Constants.getString("SERVICE_DATA_ROOT")+"/flickr/users/";

	Path startingDir = Paths.get(USERS_DB_PATH_ROOT);
	String pattern = "66854529@N00-*[!follows].json";
	//Pair<List<Path>, Boolean> result = new Pair<List<Path>, Boolean>(null, new Boolean(false));
	Finder finder = new Finder(pattern);
	try {
		Files.walkFileTree(startingDir, finder);
	} catch (IOException e) {
		//e.printStackTrace();
		logger.error(e);
	}
	logger.debug(finder.results);
	List<Path> results = finder.results;		

	
/*	FlickrService flickr = FlickrService.createFlickrServiceOnBehalfOfUserID("66854529@N00");
	flickr.getFollows();
	logger.debug(flickr.getURLForBuddyIcon());
*/	
	/*
    // Replace these with your own api key and secret
    String apiKey = Constants.getString("FLICKR_API_KEY");
    String apiSecret = Constants.getString("FLICKR_API_SECRET");
    OAuthService service = new ServiceBuilder().provider(FlickrApi.class).apiKey(apiKey).apiSecret(apiSecret).build();
    Scanner in = new Scanner(System.in);

    System.out.println("=== Flickr's OAuth Workflow ===");
    System.out.println();

    // Obtain the Request Token
    System.out.println("Fetching the Request Token...");
    Token requestToken = service.getRequestToken();
    System.out.println("Got the Request Token!");
    System.out.println();

    System.out.println("Now go and authorize Scribe here:");
    String authorizationUrl = service.getAuthorizationUrl(requestToken);
    System.out.println(authorizationUrl + "&perms=read");
    System.out.println("And paste the verifier here");
    System.out.print(">>");
    Verifier verifier = new Verifier(in.nextLine());
    System.out.println();

    // Trade the Request Token and Verfier for the Access Token
    System.out.println("Trading the Request Token for an Access Token...");
    Token accessToken = service.getAccessToken(requestToken, verifier);
    System.out.println("Got the Access Token!");
    System.out.println("(if your curious it looks like this: " + accessToken + " )");
    System.out.println();

    // Now let's go and ask for a protected resource!
    System.out.println("Now we're going to access a protected resource...");
    OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
    request.addQuerystringParameter("method", "flickr.test.login");
    service.signRequest(accessToken, request);
    Response response = request.send();
    System.out.println("Got it! Lets see what we found...");
    System.out.println();
    System.out.println(response.getBody());

    System.out.println();
    System.out.println("Thats it man! Go and build something awesome with Scribe! :)");*/
  }
}
