package fi.livi.digitraffic.tie.data.jms;

public class JMSUnmarshalMessageException extends RuntimeException {

    public JMSUnmarshalMessageException(String message, Exception e) {
        super(message, e);
    }

    public JMSUnmarshalMessageException(String message) {
        super(message);
    }
}
