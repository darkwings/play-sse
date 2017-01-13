package akka;

import akka.actor.ActorRef;

/**
 * @author ftorriani
 */
public class Protocol {


    public static class LogActors {

    }

    /**
     * Registering message from ActorPublisher to StreamMediatorActor
     */
    public static class Register {

    }

    /**
     * Unregistering message from ActorPublisher to StreamMediatorActor
     */
    public static class Unregister {

    }

    public static class Message {

        public final String text;

        public Message( String text ) {
            this.text = text;
        }
    }

}
