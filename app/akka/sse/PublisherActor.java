package akka.sse;


import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.sse.Protocol.Message;
import akka.sse.Protocol.Register;
import akka.stream.actor.AbstractActorPublisher;
import akka.stream.actor.ActorPublisherMessage;
import play.Logger;

import java.util.ArrayList;
import java.util.List;

import static services.Constants.PUBLISHERS_MANAGER_ACTOR;

/**
 * The publisher actor, that will be bound to an Akka stream via {@link play.api.libs.EventSource}
 *
 * @author ftorriani
 */
public class PublisherActor extends AbstractActorPublisher<String> {

    /**
     * Buffer of messages
     */
    private final List<String> buf = new ArrayList<>();

    public static Props props() {
        return Props.create( PublisherActor.class );
    }

    private ActorSelection publishersManager;

    public PublisherActor() {
        Logger.info( "PublisherActor: building actor" );

        receive( ReceiveBuilder.

                match( Message.class, msg -> {
                    Logger.info( "PublisherActor: received message: {}", msg.text );
                    handleMessage( msg.text );
                } ).
                match( ActorPublisherMessage.Request.class, request -> {
                    Logger.info( "PublisherActor: received request: {}", request );

                    deliverBuf();

                } ).
                match( ActorPublisherMessage.Cancel.class, cancel -> {
                    Logger.info( "PublisherActor: received cancel: {}. Unregistering to StreamMediator", cancel );
                    publishersManager.tell( new Protocol.Unregister(), context().self() );
                    context().stop( self() );

                } ).
                matchAny( msg -> {
                    Logger.info( "PublisherActor: received (and ignored) {}", msg );
                } ).
                build() );

        publishersManager = context().actorSelection( "/user/" + PUBLISHERS_MANAGER_ACTOR );
        Logger.debug( "PublisherActor: registering myself ({}) to PublishersManagerActor", context().self() );
        publishersManager.tell( new Register(), context().self() );
    }

    void handleMessage( String message ) {
        if ( buf.isEmpty() && totalDemand() > 0 ) {
            Logger.debug( "PublisherActor: calling onNext with '{}'", message );
            onNext( message );
        }
        else {
            Logger.debug( "PublisherActor: buffering {}", message );
            buf.add( message );
            deliverBuf();
        }
    }

    void deliverBuf() {
        Logger.debug( "PublisherActor: totalDemand() {}", totalDemand() );
        while ( totalDemand() > 0 ) {
            if ( totalDemand() <= Integer.MAX_VALUE ) {
                final List<String> took =
                        buf.subList( 0, Math.min( buf.size(), (int) totalDemand() ) );
                took.forEach( this::onNext );
                buf.removeAll( took );
                break;
            }
            else {
                final List<String> took =
                        buf.subList( 0, Math.min( buf.size(), Integer.MAX_VALUE ) );
                took.forEach( this::onNext );
                buf.removeAll( took );
            }
        }
    }
}
