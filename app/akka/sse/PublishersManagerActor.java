package akka.sse;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import play.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * This actor manages the available {@link PublisherActor}s.
 * It gathers all the available publishers via
 * {@link Protocol.Register} message and push to all of them
 * the {@link Protocol.Message}s received.
 *
 * @author ftorriani
 */
public class PublishersManagerActor extends AbstractActor {

    /**
     * Cache of publishing actors.
     *
     * In a real-life use case, this cache will have to be periodically purged
     * of old elements.
     */
    private Map<String, ActorRef> publishers = new HashMap<>();

    public PublishersManagerActor() {

        receive( ReceiveBuilder.
                match( Protocol.Register.class, msg -> {
                    Logger.info( "PublishersManagerActor: registering {}", sender() );
                    publishers.put( sender().toString(), sender() );

                } ).
                match( Protocol.Unregister.class, msg -> {

                    Logger.info( "PublishersManagerActor: unregistering {}", sender() );
                    publishers.remove( sender().toString() );

                } ).
                match( Protocol.Message.class, msg -> {
                    // Push the message to all the available publisher actors
                    Logger.info( "PublishersManagerActor: received message. Forwarding to available publishers" );
                    publishers.values().forEach( ref -> ref.forward( msg, context() ) );

                } ).
                build() );
    }
}
