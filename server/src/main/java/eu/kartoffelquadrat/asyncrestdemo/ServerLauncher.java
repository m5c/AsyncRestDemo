package eu.kartoffelquadrat.asyncrestdemo;

import eu.kartoffelquadrat.asyncrestlib.BroadcastContent;
import eu.kartoffelquadrat.asyncrestlib.BroadcastContentManager;
import eu.kartoffelquadrat.asyncrestlib.Transformer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Demo Spring boot application to demonstrate usage of the "Asynchronous Rest Library". -->
 * https://github.com/kartoffelquadrat/AsyncRestLib
 * <p>
 * For an ARL user, the interesting part of this project is the "StateController" class. Also see the "asyncpoll.js"
 * file in the resources folder, for a Javascript client counterpart.
 *
 * @author Maximilian Schiedermeier
 */
@SpringBootApplication
public class ServerLauncher {

    // The Spring boot context. CAn be used to shut down the application. See shutdown() method.
    private static ConfigurableApplicationContext context;

    // The BroadcastContent is the object, that long-polling-clients subscribe to. It is maintained by a
    // BroadcastcontentManager, which registers changes if the BroadcastContent and then unblocks stalled client-requests.
    private static BroadcastContentManager broadcastContentManager;

    /**
     * Powers up the Spring Boot application, initializes the BroadcastContentManager with an initial ChatMessage. Also
     * sets up a demo-transformer that reduces messages to the emptystring, unless they contain a specific letter. This
     * allows client to request updates only, if that specific letter is part of a chat-message. This functionality is
     * accessed via the StateController.asyncHookedGetState() method. This method is a rest- endpoint where the letter
     * is a path parameter. Finally this calss also starts an input scanner that updates the BroadCastManager's
     * ChatMessage, any time a line is entered.
     *
     * @param args none.
     */
    public static void main(String[] args) {
        context = SpringApplication.run(ServerLauncher.class, args);

        // Prepare content that shall be asynchronously broadcast to clients by this library
        BroadcastContent broadcastContent = new ChatMessage("");

        // Register content at libraries content manager
        broadcastContentManager = new BroadcastContentManager(broadcastContent);

        // Register a hook for tag-specific content-transformations, prior to broadcasting (optional)
        Transformer<ChatMessage> demoAppCustomTransformer = new CustomTransformer();
        StateController stateController = (StateController) context.getBean("stateController");
        stateController.init(broadcastContentManager, demoAppCustomTransformer); // second argument is
        // optional (then no tag-specific transformations are applied by the library.)

        // Create something that alters the managers state asynchronously
        new InputScanner(broadcastContentManager);

        // Print welcome messages.
        System.out.println("Server up an running. For a web-UI, go to:\nhttp://127.0.0.1:8446/");
        System.out.println("Go ahead and type messages for connected client(s), or type \"exit\" to shutdown the " +
                "server and disconnect all clients.");
    }

    /**
     * Can be called by the scanner, when "exit" was typed. This will shutdown the spring boot application.
     */
    public static void shutdown() {
        broadcastContentManager.terminate();
        context.close();
    }
}
