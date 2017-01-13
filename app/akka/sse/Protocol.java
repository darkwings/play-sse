package akka.sse;

/**
 * The protocol for SSE actors ({@link PublisherActor} and {@link PublishersManagerActor})
 *
 * @author ftorriani
 */
public class Protocol {


    /**
     * Registering message from a newly created instance of the publisher actor ({@link PublisherActor})
     * to {@link PublishersManagerActor}
     */
    public static class Register {

    }

    /**
     * Unregistering message from an instance of the publisher actor ({@link PublisherActor})
     * to {@link PublishersManagerActor}
     */
    public static class Unregister {

    }

    /**
     * A simple message sent by client and forwarded to the opened SSE streams
     */
    public static class Message {

        public final String text;

        public Message( String text ) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

}
