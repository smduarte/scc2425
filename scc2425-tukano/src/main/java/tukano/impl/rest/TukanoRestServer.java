package main.java.tukano.impl.rest;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import main.java.tukano.impl.Token;
import main.java.utils.Args;
import main.java.utils.IP;
import main.java.utils.Props;

@ApplicationPath("/rest")
public class TukanoRestServer extends Application {
	final private static Logger Log = Logger.getLogger(TukanoRestServer.class.getName());
	static String SERVER_BASE_URI = "http://%s:%s/tukano/rest";
	public static final int PORT = 8080;
	public static String serverURI;
			
	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
		serverURI = String.format(SERVER_BASE_URI, IP.hostname(), PORT);
	}

	public TukanoRestServer() {
		Token.setSecret("defaultSecret1234567890");
		Log.info(String.format("Tukano Server ready @ %s\n",  serverURI));
	}

	@Override
	public Set<Object> getSingletons() {
		Set<Object> singletons = new HashSet<>();
		singletons.add(new RestBlobsResource());
		singletons.add(new RestUsersResource());
		singletons.add(new RestShortsResource());
		return singletons;
	}

	public static void main(String[] args) throws Exception {
		Args.use(args);
		Token.setSecret( Args.valueOf("-secret", "defaultSecret1234567890"));
		//Props.load( Args.valueOf("-props", "").split(","));
		Log.info("Server setup complete. Ready for deployment.");
	}

}
