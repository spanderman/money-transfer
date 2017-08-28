package name.spanderman.money.transfer.microservice;

import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Main class.
 */
public class Main {
	// Base URI the Grizzly HTTP server will listen on
	public static final int PORT = 1080;
	public static String BASE_URI = "http://0.0.0.0:" + PORT + "/";

	/**
	 * Setup in memory DB
	 * 
	 * @throws Throwable
	 */
	public static void dbSetup() throws Throwable {
		AccountDAO.getInstance().dbSetup();
		DepositDAO.getInstance().dbSetup();
		WithdrawalDAO.getInstance().dbSetup();
		TransferDAO.getInstance().dbSetup();
	}

	/**
	 * Starts Grizzly HTTP server exposing JAX-RS resources defined in this
	 * application.
	 * 
	 * @param port
	 *            the port server will listen on
	 * @return Grizzly HTTP server.
	 */
	public static HttpServer startServer(int port) {
		// create a resource config that scans for JAX-RS resources and
		// providers
		final ResourceConfig rc = new ResourceConfig().packages(Main.class.getPackage().getName())
				.register(JacksonFeature.class);

		// create and start a new instance of grizzly http server
		// exposing the Jersey application at BASE_URI
		BASE_URI = BASE_URI.replace(Integer.toString(PORT), Integer.toString(port));
		return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
	}

	/**
	 * Starts Grizzly HTTP server on standard port ({@value #PORT}) exposing
	 * JAX-RS resources defined in this application.
	 * 
	 * @return Grizzly HTTP server.
	 */
	public static HttpServer startServer() {
		return startServer(PORT);
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		dbSetup();
		String hostname = System.getenv("HOSTNAME");
		if (hostname == null) {
			hostname = "localhost";
		}
		boolean heroku = false;
		String port = System.getenv("PORT");
		if (port != null) {
			heroku = true;
		}
		final HttpServer server = startServer(heroku ? Integer.valueOf(port) : PORT);
		if (heroku) {
			System.out.println(
					String.format("Jersey app started with WADL available at " + "%sapplication.wadl", BASE_URI));
			while (true) {
				System.in.read();
			}
		} else {
			System.out.println(String.format(
					"Jersey app started with WADL available at " + "%sapplication.wadl\nHit enter to stop it...",
					BASE_URI));
			System.in.read();
			server.shutdownNow();
		}
	}
}