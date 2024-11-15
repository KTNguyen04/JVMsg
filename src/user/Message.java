package user;

public class Message {
    private String from;
    private String to;
    private String content;
    private String timeStamp;

    public Message(String from, String to, String content, String timeStamp) {
        this.from = from;
        this.to = to;
        this.content = content;
        this.timeStamp = timeStamp;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getContent() {
        return content;
    }

}
