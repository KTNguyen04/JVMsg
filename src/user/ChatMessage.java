package user;

public class ChatMessage {
    private String from;
    private String to;
    private String content;
    private String time_stamp;

    public ChatMessage(String from, String to, String content, String timeStamp) {
        this.from = from;
        this.to = to;
        this.content = content;
        this.time_stamp = timeStamp;
    }

    public String getFrom() {
        return from;
    }

    public String getTimeStamp() {
        return time_stamp;
    }

    public String getTo() {
        return to;
    }

    public String getContent() {
        return content;
    }

}
