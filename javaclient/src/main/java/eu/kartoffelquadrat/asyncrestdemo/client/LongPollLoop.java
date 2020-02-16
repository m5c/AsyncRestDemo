package eu.kartoffelquadrat.asyncrestdemo.client;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Main control long-poll loop. In short: keep polling as long as the reply had a return code 200 (ok) or 408 (timeout).
 * In case of f 200 (ok), print the message payload.
 */
public class LongPollLoop {

    /**
     * Repeatedly queries the server for updates. Any client using the ARL has to imitate the main loop, to implement
     * the repeated long polling. A new query is launched whenever the server sent an HTTP reply. THe remote method
     * defers the HTTP reply until the predefined timeout is reached or the servers state changed (whatever comes
     * first). In case the  reply was due a status update, this method prints the reply's payload.
     *
     * @param duplicatesWanted - if set the client will not add the hash of the current client status to every long
     *                         poll. This prevents the server from skipping irrelevant status change messages.
     * @param filterTag        - if non-null and not-empty, the client can long poll for specific flags in the observed
     *                         server content, e.g. only chat messages containing a specific string. Requires
     *                         suppressDuplicates to be set to true.
     */
    static void printUpdates(boolean duplicatesWanted, String filterTag, String serverUpdateUrl) throws UnirestException {

        if (filterTag != null && filterTag.contains("/"))
            throw new RuntimeException("Url delimited not allowed in " + "filter tag");

        // we store the hash of the most recent message. Later subscriptions can pass this hash to the server, to
        // omit irrelevant status updates.
        String lastHash = DigestUtils.md5Hex("{\"line\":\"[INIT]\"}");
        int returnCode = 200;

        // Continue asking for updates, until the server told us to stop long-polling (response code 500) or
        // connection broke down
        while (returnCode != 500) {

            // Field to store the eventually received server response
            HttpResponse<String> httpReply;

            // Subscribe to all updates
            if (duplicatesWanted)
                httpReply = Unirest.get(serverUpdateUrl).asString();

                // Subscribe to content-changes, only (by passing the hash of the current local state)
            else if (filterTag == null || filterTag.isEmpty())
                httpReply = Unirest.get(serverUpdateUrl).queryString("hash", lastHash).asString();

                // Subscribe to only content-changes, containing a specific tag
            else
                httpReply = Unirest.get(serverUpdateUrl + "/" + filterTag).queryString("hash", lastHash).asString();

            // Extract and interpret response payload, in case the server sent an actual update, ignore otherwise
            returnCode = httpReply.getStatus();
            if (returnCode == 200) {

                // Compute json-string hash and convert the json string back to actual object, so we can print it
                // most recent message's hash
                String messageAsJsonString = httpReply.getBody();
                lastHash = DigestUtils.md5Hex(messageAsJsonString);
                System.out.println(new Gson().fromJson(messageAsJsonString, ChatMessage.class));
            }

            // If the server replied with an unexpected return code, abort.
            else if (returnCode != 408 && returnCode != 500)
                throw new RuntimeException("Unsupported Return-Code: " + returnCode);
        }
        System.out.println("Stopped polling, because server said there will be no more updates.");
    }
}
