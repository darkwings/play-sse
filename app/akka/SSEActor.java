package akka;


import akka.Protocol.MessageAccepted;
import akka.Protocol.NewMessage;
import akka.Protocol.Start;
import akka.Protocol.UserRegistration;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.stream.actor.AbstractActorPublisher;
import akka.stream.actor.ActorPublisherMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ftorriani
 */
public class SSEActor extends AbstractActorPublisher<String> {

//    private final LoggingAdapter log = Logging.getLogger( context().system(), this );

    private final List<String> buf = new ArrayList<>();

    public static Props props() {
        return Props.create( SSEActor.class );
    }

    public SSEActor() {
        System.out.println( "SSEActor: building actor" );

        receive( ReceiveBuilder.
                match( Start.class, msg -> {
                    System.out.println( "SSEActor: " + msg );

                    handleMessage( "Start" );
                } ).
                match( UserRegistration.class, msg -> {
                    System.out.println( "SSEActor: " + msg );

                    handleMessage( "Registration" );
                } ).
                match( NewMessage.class, msg -> {
                    System.out.println( "SSEActor: " + msg );

                    sender().tell( new MessageAccepted(), self() );

                    handleMessage( "NewMessage" );
                } ).
                match( ActorPublisherMessage.Request.class, msg -> {
                    System.out.println( "SSEActor: Request " + msg );

                    deliverBuf();

                } ).
                match( ActorPublisherMessage.Cancel.class, msg -> {
                    System.out.println( "SSEActor: Cancel " + msg );

                    context().stop( self() );

                } ).
                matchAny( msg -> {
                    System.out.println( "SSEActor: received (and ignored) " + msg );
                } ).
                build() );


        // TODO: fare in modo che questo attore si registri presso StreamMediator (inviando un messaggio con il path)
        System.out.println( "SSEActor: " + context().self() );
    }

    void handleMessage( String message ) {
        if ( buf.isEmpty() && totalDemand() > 0 ) {
            System.out.println( "calling onNext with '" + message + "'" );
            onNext( message );
        }
        else {
            System.out.println( "buffering " + message );
            buf.add( message );
            deliverBuf();
        }
    }

    void deliverBuf() {
        System.out.println( "totalDemand() " + totalDemand() );
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
