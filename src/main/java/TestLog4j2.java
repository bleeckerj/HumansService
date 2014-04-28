import org.apache.logging.log4j.*;

public class TestLog4j2 {
	static Logger logger = LogManager.getLogger(TestLog4j2.class.getName());
    static Logger logger_2 = LogManager.getLogger("TestLog4j3");
	public TestLog4j2() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		logger.trace("trace - Entering application.");
		logger.error("error - Didn't do it.");
		logger.debug("debug - Debug did it?");
        logger.warn("warn - Warn did it?");

        logger_2.info("logger2 - info - Hello Logger??");
        logger_2.warn("logger2 - warn - Warning...");
	}

}
