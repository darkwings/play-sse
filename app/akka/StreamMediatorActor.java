package akka;

import akka.Protocol.Message;
import akka.Protocol.Register;
import akka.Protocol.Unregister;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import play.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ftorriani
 */
public class StreamMediatorActor extends AbstractActor {

    /**
     * Cache of publishing actors
     */
    private Map<String, ActorRef> publishers = new HashMap<>();

    public StreamMediatorActor() {

        receive( ReceiveBuilder.
                match( Register.class, msg -> {
                    Logger.info( "StreamMediatorActor: registering {}", sender() );
                    publishers.put( sender().toString(), sender() );

                } ).
                match( Unregister.class, msg -> {

                    Logger.info( "StreamMediatorActor: unregistering {}", sender() );
                    publishers.remove( sender().toString() );

                } ).
                match( Message.class, msg -> {
                    Logger.info( "StreamMediatorActor: received message. Forwarding to available publishers" );
                    publishers.values().forEach( ref -> ref.forward( msg, context() ) );

                } ).
                build() );
    }
}
