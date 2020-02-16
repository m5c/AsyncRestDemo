package eu.kartoffelquadrat.asyncrestdemo.client;

import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.IOException;

/**
 * Launcher for the Chat demo application. (command line)
 *
 * @author Maximilian Schiedermeier on 2019/09/02 maximilian.schiedermeier@mail.mcgill.ca
 * @project AsyncRestDemo
 */
public class ClientLauncher {

    public static void main(String[] args) {

        // Print welcome message that show command line usage
        ClientUtils.printWelcomeMessage();

        // Verify the command line arguments do not contradict / are sane
        if (!ClientUtils.isSameCommandLineArgsCombo(args))
            return;

        // Verify the server is online
        if(!ClientUtils.isServerOnline())
            return;

        // Print "ok"-message and extract connection setting form cmd line / props-file
        System.out.println("Server up and running, this Java-client is now waiting for replies :-)");
        boolean duplicatesWanted = ClientUtils.duplicatesWantedArg(args);
        String tag = ClientUtils.tagFromArgs(args);
        ServerUrls serverUrls = ClientUtils.loadServerSettings();

        // Looks good, lets start polling for chat-message updates.
        try {
            LongPollLoop.printUpdates(duplicatesWanted, tag, serverUrls.getServerGetUpdateUrl());
        } catch (UnirestException e) {

            // Happens if the connection is brutally closed (network error / server power outage)
            System.out.println("Error: Server was brutally killed. No more messages will be displayed.");
        }
    }
}
