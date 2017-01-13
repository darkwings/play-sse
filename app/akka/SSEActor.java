package akka;


import akka.Protocol.Message;
import akka.Protocol.Register;
import akka.Protocol.Unregister;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.stream.actor.AbstractActorPublisher;
import akka.stream.actor.ActorPublisherMessage;
import play.Logger;
import services.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * The publisher actor, that will be bound to an Akka stream via {@link play.api.libs.EventSource}
 *
 * @author ftorriani
 */
public class SSEActor extends AbstractActorPublisher<String> {

    /**
     * Buffer of messages
     */
    private final List<String> buf = new ArrayList<>();

    public static Props props() {
        return Props.create( SSEActor.class );
    }

    private ActorSelection streamMediator;

    public SSEActor() {
        Logger.info( "SSEActor: building actor" );

        receive( ReceiveBuilder.

                match( Message.class, msg -> {
                    Logger.info( "SSEActor: received message: {}", msg.text );
                    handleMessage( msg.text );
                } ).
                match( ActorPublisherMessage.Request.class, request -> {
                    Logger.info( "SSEActor: received request: {}", request );

                    deliverBuf();

                } ).
                match( ActorPublisherMessage.Cancel.class, cancel -> {
                    Logger.info( "SSEActor: received cancel: {}. Unregistering to StreamMediator", cancel );
                    streamMediator.tell( new Unregister(), context().self() );
                    context().stop( self() );

                } ).
                matchAny( msg -> {
                    Logger.info( "SSEActor: received (and ignored) {}", msg );
                } ).
                build() );

        streamMediator = context().actorSelection( "/user/" + Constants.STREAM_MEDIATOR_ACTOR_NAME );
        Logger.debug( "SSEActor: registering myself ({}) to StreamMediator", context().self() );
        streamMediator.tell( new Register(), context().self() );
    }

    void handleMessage( String message ) {
        if ( buf.isEmpty() && totalDemand() > 0 ) {
            Logger.debug( "SSEActor: calling onNext with '{}'", message );
            onNext( message );
        }
        else {
            Logger.debug( "SSEActor: buffering {}", message );
            buf.add( message );
            deliverBuf();
        }
    }

    void deliverBuf() {
        Logger.debug( "SSEActor: totalDemand() {}", totalDemand() );
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
