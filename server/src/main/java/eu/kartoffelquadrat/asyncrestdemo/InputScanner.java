package eu.kartoffelquadrat.asyncrestdemo;

import eu.kartoffelquadrat.asyncrestlib.BroadcastContentManager;

import java.util.Scanner;

/**
 * Simple scanner class that wraps up every line typed as a ChatMessage, to update the BroadcastContentManager. Only
 * exception is the "exit" message. If typed, this is interpreted as instruction to shut-down the server.
 *
 * @author Maximilian Schiedermeier
 */
public class InputScanner {

    /**
     * Scanning of inputs is initiated by the constructor, but realized in an extra thread. (Waiting for inputs is a
     * blocking command)
     *
     * @param broadcastContentManager
     */
    public InputScanner(BroadcastContentManager broadcastContentManager) {

        // Inner anonymous thread to avoid blocking the constructor, while waiting for inputs.
        (new Thread() {
            public void run() {

                // Advise scanner to work in typed inputs.
                Scanner myObj = new Scanner(System.in);

                // Initialize last read message
                String line = "";

                // Keep updating server state with every line until user typed "exit"
                while (!line.equals("exit")) {

                    // Actually wait for a new input (blocking)
                    line = myObj.nextLine();

                    // Update the BCM (will unblock the stalled long-polls)
                    broadcastContentManager.updateBroadcastContent(new ChatMessage("Server: " + line));
                }

                // Terminating the BCM triggers the sending of a final 410 HTTP code to all long-polling clients. (Tells
                // them to stop long-polling)
                broadcastContentManager.terminate();

                // Wait a second so everyone has received the 204 reply, then issue server shutdown.
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ServerLauncher.shutdown();
            }
        }).start();
    }
}
