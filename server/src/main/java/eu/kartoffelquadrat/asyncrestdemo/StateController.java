package eu.kartoffelquadrat.asyncrestdemo;

import eu.kartoffelquadrat.asyncrestlib.BroadcastContentManager;
import eu.kartoffelquadrat.asyncrestlib.ResponseGenerator;
import eu.kartoffelquadrat.asyncrestlib.Transformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * @author Maximilian Schiedermeier on 2019/09/06 maximilian.schiedermeier@mail.mcgill.ca
 * @project AsyncRestDemoKeyboardSender
 */
@RestController
public class StateController {

    // BCM and Transformer are set by constructor.
    private BroadcastContentManager<ChatMessage> broadcastContentManager;
    private Transformer<ChatMessage> transformer;

    // The default timeout is 30 seconds. This is a reasonable value, as it is also the default for many browsers.
    // If a different value is provided in the application.properties file, it will be injected here.
    @Value("#{new Integer('${long.poll.timeout}')}")
    private long longPollTimeout;

    /**
     * Provide a your customized BroadcastContentManager, to manage the server state you want to propagate. The
     * transformer additional argument is optional. If you do not specify a transformer, the library will apply an
     * identity transformer. That is to say client specific transformations are disabled. (See asyncHookedGetState)
     *
     * @param broadcastContentManager
     */
    public void init(BroadcastContentManager broadcastContentManager, Transformer transformer) {
        this.broadcastContentManager = broadcastContentManager;
        this.transformer = transformer;
    }

    /**
     * Synchronous methods for clients to check if server is online on startup
     *
     * @return The String: "online". No need to implement a negative answer, as a server which is down can not reply.
     */
    @GetMapping("/online")
    public String online() {
        return "online";
    }

    /**
     * Asynchronous Rest endpoint. Method to receive asynchronous messages when a server update occurs.  In case no
     * update occurred before the connection timeout (see injected variable), the server replied with HTTP code 408.
     * Clients who want to subscribe to server updates repeatedly call this method (single-threaded, blocking). The
     * client can optionally send a hash of the latest update it received. (hash-string is then the MD5 of the last
     * received message as a json string. On a hash mismatch the server sends an update instantly. Also the server omits
     * updates, that would collide in hash. (This is notably relevant, if a transformer is used.)
     *
     * @return a json string, encoding the update.
     */
    @GetMapping(value = "/getupdate", produces = "application/json; charset=utf-8")
    // (different clients may need different updates)
    public DeferredResult<ResponseEntity<String>> asyncGetState(@RequestParam(required = false) String hash) {

        DeferredResult<ResponseEntity<String>> result;

        // If no hash was provided, request an update on the next registered state change. (Ignoring the client's
        // current state)
        if (hash == null || hash.isEmpty())

            // send asynchronous update to client as soon as the manages state changes.
            result = ResponseGenerator.getAsyncUpdate(longPollTimeout, broadcastContentManager);

            // If a hash was provided, update only if the hash differs from the curretnly maintained object. Run an
            // initial check to provide a direct update in case the provided hash differs from the current server-side
            // hash.
        else
            // send potentially asynchronous update as soon as a broadcast content arises, having a hash
            // distinct to the provided hash
            result = ResponseGenerator.getHashBasedUpdate(longPollTimeout, broadcastContentManager, hash);

        return result;
    }

    /**
     * Asynchronous REST endpoint. Tag-specific version of the previous method. Demonstrates how async notifications can
     * be bound to user-provided stings. Any time a state change appears on server side, the server will run a custom
     * hook (CustomTransformer class) prior to a potential poll-unblocking. The Transformer class is a public ARL
     * interface. The same way this project provides a custom Transformer-implementation, ARL users can provide their
     * own Transformers where required.
     *
     * @param hash as the mandatory current hash of client state, that the transformed server state is compared to)
     * @param tag  as the tag to be passed to the transformer hook.
     * @return a json string, encoding the update.
     */
    @GetMapping(value = "/getupdate/{clienttag}", produces = "application/json; charset=utf-8")
    // version.
    // (different clients may need different updates)
    public DeferredResult<ResponseEntity<String>> asyncHookedGetState(String hash, @PathVariable(value = "clienttag")
            String tag) {

        // send potentially asynchronous update to client as soon as a broadcast content arises, having a
        // transformed version with a hash distinct to the provided hash.
        return ResponseGenerator.getTransformedUpdate(longPollTimeout, broadcastContentManager, hash, transformer,
                tag);
    }

    /**
     * Synchronous Rest endpoint. Callable by chat clients.<br/> curl --header "Content-Type: text/plain" --request POST
     * --data 'toto'   http://127.0.0.1:8446/sendMessage
     *
     * @param inboundMessage as the received message that shall be propagated to other chat participants.
     */
    @PostMapping(value = "/sendMessage", consumes = "application/json")
    public void sendMessage(@RequestBody InboundMessage inboundMessage) {

        // Print message in server log, then update internal broadcast content to notify all connected clients about
        // the new message
        System.out.println(inboundMessage);
        broadcastContentManager.updateBroadcastContent(new ChatMessage(inboundMessage.toString()));
    }
}
