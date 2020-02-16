package eu.kartoffelquadrat.asyncrestdemo;

import eu.kartoffelquadrat.asyncrestlib.Transformer;

/**
 * An application specific transformer. It intercepts and manipulates state-updates for clients which use the
 * ResponseGenerator.getTransformedUpdate(...) method. This transformer uses the provided tag, to nullify any message
 * not containing the tag. So this is a chat filter that omits all messages not containing the provided keyword.
 *
 * @author Maximilian Schiedermeier
 */
public class CustomTransformer implements Transformer<ChatMessage> {

    /**
     * This transform-method simply blanks out all content NOT containing the tag provided.
     */
    @Override
    public ChatMessage transform(ChatMessage broadcastContent, String keyword) {

        if (broadcastContent.containsKeyword(keyword))
            return broadcastContent;
        else
            return new ChatMessage("");
    }
}
