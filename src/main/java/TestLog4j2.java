import org.apache.logging.log4j.*;

public class TestLog4j2 {
	static Logger logger = LogManager.getLogger(TestLog4j2.class.getName());
	public TestLog4j2() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		logger.trace("Entering application.");
		logger.error("Didn't do it.");
		logger.debug("Debug did it?");
	}

}
