package eu.kartoffelquadrat.asyncrestdemo.client;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClientUtils {
    /**
     * Extract server settings from application.properties and build prepared strings for interaction with the server's
     * REST interface.
     */
    static ServerUrls loadServerSettings() {
        try (InputStream input = ClassLoader.getSystemResourceAsStream("application.properties")) {
            // load the file
            Properties prop = new Properties();
            prop.load(input);

            // get the property value and print it out
            String protocol = prop.getProperty("server.proto");
            String ip = prop.getProperty("server.ip");
            String port = prop.getProperty("server.port");
            String testPath = prop.getProperty("server.testpath");
            String updatePath = prop.getProperty("server.updatepath");

            // Build end points for rest methods
            StringBuilder serverRootBuilder = new StringBuilder(protocol);
            serverRootBuilder.append("://").append(ip).append(":").append(port).append("/");
            String serverRoot = serverRootBuilder.toString();

            // Construct full server Api urls and return them in a bundled object.
            return new ServerUrls(serverRoot + testPath, serverRoot + updatePath);

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Helper method to verify of the server counterpart is up and running, before we start the long-poll loop.
     *
     * @return
     */
    static boolean isServerReachable(String serverTestUrl) {
        try {
            return Unirest.get(serverTestUrl).asString().getStatus() == 200;
        } catch (UnirestException e) {
            return false;
        }
    }

    /**
     * Searches the args for a "-d" flag
     *
     * @param args as the command line args
     * @return boolean frag whether "-d" was found or not
     */
    static boolean duplicatesWantedArg(String[] args) {
        if (args != null) {
            for (String arg : args) {
                if (arg.trim().equals("-d"))
                    return true;
            }
        }
        return false;
    }

    /**
     * Searches args for tag string. Returns null of non was found
     *
     * @param args
     * @return
     */
    static String tagFromArgs(String[] args) {
        if (args != null) {

            for (String arg : args) {
                if (!arg.trim().equals("-d"))
                    return arg.trim();
            }
        }
        return null;
    }

    /**
     * Helper method to verify if the command line arguments are sane.
     *
     * @return false if an illegal combination was used.
     */
     static boolean isSameCommandLineArgsCombo(String[] args) {
        // Parse runtime arguments
        boolean duplicatesWanted = ClientUtils.duplicatesWantedArg(args);

        // Subscribe for only message with specific keyword, if extra tag string was provided
        String tag = ClientUtils.tagFromArgs(args);

        // Verify that there was no illegal combination of command-line arguments.
        if (duplicatesWanted && tag != null) {
            System.out.println("Error: \"-d\" and filter-tag (" + tag + ") can not be combined as command line " +
                    "arguments.");
            return false;
        }
        return true;
    }

    static boolean isServerOnline()
    {
        ServerUrls serverUrls = ClientUtils.loadServerSettings();
        if (!ClientUtils.isServerReachable(serverUrls.getServerTestOnlineUrl())) {
            System.out.println("Error: Server not reachable.");
            return false;
        }
        return true;
    }

    static void printWelcomeMessage()
    {
        System.out.println("Welcome to the ASR demo JAVA client.\nOptional params:\n\t-d to explicitly subscribe to "
                + "duplicate" + " " + "messages (request " + "server to be notified on any status change, even if " +
                "they new state is identical to current local " + "state.)\n\ttag - an optinal string to request only" +
                " updates that contain this tag. Can not be " + "combined with \"-d\".\n\n");
    }
}
