import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.nearfuturelaboratory.humans.core.HumansUser;
import com.nearfuturelaboratory.util.Constants;


public class CreateUser {

	final static Logger logger = Logger.getLogger("com.nearfuturelaboratory.humans.test.Test");

	
	public CreateUser() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		  
		  
		try {
			Constants.load("/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/dev.app.properties");
			String props = "/Volumes/Slippy/Users/julian/Documents/workspace/HumansService/WebContent/WEB-INF/lib/static-logger.properties";
			PropertyConfigurator.configureAndWatch(props );

		} catch (ConfigurationException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		}

		HumansUser humansUser = new HumansUser();
		humansUser.setUsername(args[0]);
		humansUser.setPassword(args[0]);
		
	}

}
