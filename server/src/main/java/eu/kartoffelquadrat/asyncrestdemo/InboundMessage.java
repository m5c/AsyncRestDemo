package eu.kartoffelquadrat.asyncrestdemo;

public class InboundMessage {
    private String sender;
    private String line;

    public InboundMessage(String sender, String line) {
        this.sender = sender;
        this.line = line;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return sender + ": " + line;
    }
}
