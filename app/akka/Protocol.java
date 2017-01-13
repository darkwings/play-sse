package akka;

/**
 * @author ftorriani
 */
public class Protocol {


    /**
     * Utility command, used by {@link UtilsActor} to log all
     * known actors
     */
    public static class LogActors {

    }

    /**
     * Registering message from a newly created instance of the publisher actor ({@link SSEActor})
     * to {@link StreamMediatorActor}
     */
    public static class Register {

    }

    /**
     * Unregistering message from an instance of the publisher actor ({@link SSEActor})
     * to {@link StreamMediatorActor}
     */
    public static class Unregister {

    }

    /**
     * The message sent by client and forwarded to the opened SSE streams
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
