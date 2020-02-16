package eu.kartoffelquadrat.asyncrestdemo.client;

/**
 * This class is a "bean variant" of the application specific BroadcastContent implementation (in this case
 * ChatMessageBC). That is to say this class is the base class of ChatMessageBC, with everything removed but the ctr and
 * fields. This allows copy-pasting / reuse of this exact class on client side, to deserialize server-emitted messages.
 *
 * @author Maximilian Schiedermeier on 2019/09/06 maximilian.schiedermeier@mail.mcgill.ca
 * @project AsyncRestDemoKeyboardSender
 */
public class ChatMessage {

    private final String line;

    public ChatMessage() {
        line = "";
    }

    public ChatMessage(String line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return line;
    }
}
