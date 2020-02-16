package eu.kartoffelquadrat.asyncrestdemo;

import eu.kartoffelquadrat.asyncrestlib.BroadcastContent;

/**
 * This class represents the server-side "state" that clients request updates for. Any ARL application must provide an
 * implementation of the BroadcastContent interface. Typically you have a bean with the exact same structure /
 * attributes on client side (can be in a different programming language though, e.g. in JavaScript, see
 * "resources/static" folder). In this case, the BroadcastContent is implemented as an immutable object (constructor is
 * the only way to set the "line"). Therefore BroadcastContentManager updates are effectuated with the
 * "BroadcastContentManager.updateBroadcastContent(...)" method. In case your BroadcastContent implementation is not
 * immutable and you with to modify the instance maintained by the BroadcastContentManager, you can indicate that there
 * was an update, bu manually calling the "BroadcastContentManager.touch()" method.
 *
 * @author Maximilian Schiedermeier
 */
public class ChatMessage implements BroadcastContent {

    // This implementation only has a single attribute: "line". A JSON-serialization eventually for a client could be:
    // { "line":"The ARL is awesome!"}
    private final String line;

    /**
     * Constructor based initialization of the maintained line, fir this immutable BroadcastContent implementation.
     *
     * @param line
     */
    public ChatMessage(String line) {
        this.line = line;
    }

    /**
     * Mandatory method, so the ARL can omit updates that are no actual updates.
     * @return true if the chat message is actually empty, non otherwise.
     */
    @Override
    public boolean isEmpty() {
        return line.isEmpty();
    }

    /**
     * Some custom method not required for the library, but used by our own transformer that we hook in. See javadoc in
     * CustomTransformer.
     */
    public boolean containsKeyword(String keyword) {
        return line.contains(keyword);
    }
}
