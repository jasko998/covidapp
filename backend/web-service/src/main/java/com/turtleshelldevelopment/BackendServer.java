package com.turtleshelldevelopment;

import com.auth0.jwt.algorithms.Algorithm;
import com.turtleshelldevelopment.utils.EnvironmentType;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static spark.Spark.awaitStop;
import static spark.Spark.stop;

//***********************************************************
//*                                                         *
//* Created By: BackendServer                               *
//* Created On: 9/26/2022, 10:06:40 PM                      *
//* Last Modified By: Colin Kinzel                          *
//* Last Modified On: 10/26/2022, 2:06:40 AM                *
//* Description: The start file for the backend webserver   *
//*                                                         *
//***********************************************************
@CommandLine.Command(name = "WebServer", mixinStandardHelpOptions = true)
public class BackendServer implements Runnable {
    //Environment information related to database
    public static Dotenv env;
    //Logger for all logging
    public static final Logger serverLogger = LoggerFactory.getLogger("Dashboard-Backend");
    //JWT algorithm to use based on the private and public key to generate tokens with
    public static Algorithm JWT_ALGO;
    //Instance of the database for getting connections
    public static Database database;
    @CommandLine.Option(names = {"-e", "--environment"}, description = "Valid values: ${COMPLETION-CANDIDATES}")
    public static EnvironmentType environment = EnvironmentType.PROD;
    @CommandLine.Option(names = {"-db", "--database-type"}, description = "Should use test Database")
    boolean enableTestDb = false;

    @Override
    public void run() {
        serverLogger.info("Starting backend in " + environment.name());
        serverLogger.info("Loading .env...");
        env = Dotenv.load();
        serverLogger.info("Loaded .env");
        serverLogger.info("Connecting to Database...");
        if(enableTestDb || environment.equals(EnvironmentType.DEVEL)) {
            database = new Database(env.get("TEST_DB_URL"), env.get("TEST_DB_USERNAME"), env.get("TEST_DB_PASSWORD"));
        } else {
            database = new Database();
        }
        serverLogger.info("Successfully connected to Database!");
        serverLogger.info("Setting up JWT...");
        KeyPair jwtPair;
        jwtPair = new JWTAuthentication().loadJwt();
        if(jwtPair != null) {
            JWT_ALGO = Algorithm.RSA512((RSAPublicKey) jwtPair.getPublic(), (RSAPrivateKey) jwtPair.getPrivate());
        } else {
            throw new RuntimeException("Failed to generating JWT Key pair!");
        }
        serverLogger.info("Successfully Setup JWT Provider!");
        serverLogger.info("Setting up Endpoints");
        //Fire up spark microframework instance
        new Routing().fire();
        //Wait until the spark instance is stopped.
        awaitStop();
    }

    public static void main(String[] args) {
        //Create shutdown hook to make sure spark server is actually stopped before closing
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting Down Backend!");
            stop();
            awaitStop();
        }));
        //Run Webserver with some command line arguments handled by picocli
        new CommandLine(new BackendServer()).execute(args);
    }
}
