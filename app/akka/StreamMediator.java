package akka;

import akka.Protocol.Init;
import akka.Protocol.NewMessage;
import akka.Protocol.Start;
import akka.Protocol.UserRegistration;
import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ftorriani
 */
public class StreamMediator extends AbstractActor {

    private Map<String, ActorRef> publishers = new HashMap<>();

    public StreamMediator() {

        receive( ReceiveBuilder.
                match( Init.class, msg -> {
                    // Invio un messaggio Identify per capire il path dell'agent
                    // che ha il ruolo di Publisher
                    ActorSelection selection =
                            getContext()
                                    .actorSelection( "/user/StreamSupervisor*/*" );
                    selection.tell( new Identify( "id" ), context().self() );

                } ).
                match( ActorIdentity.class, msg -> {

                    // Intercetto la risposta al messaggio Identify
                    // Il publisher ha un path del tipo
                    // akka://application/user/StreamSupervisor*/flow*actorPublisherSource
                    ActorRef ref = msg.getRef();
                    if ( ref.toString().contains( "actorPublisherSource" ) ) {
                        publishers.put( ref.toString(), ref );
                        System.out.println( "Publisher is " + ref.toString() );
                    }

                } ).
                match( Start.class, msg -> {
                    System.out.println( "StreamMediator: Start" );
                    publishers.values().forEach( ref -> ref.forward( msg, context() ) );

                } ).
                match( Protocol.MessageAccepted.class, msg -> {
                    // From SSEActor
//                    System.out.println( "StreamMediator: MessageAccepted" );

                } ).
                match( UserRegistration.class, msg -> {
                    System.out.println( "StreamMediator: UserRegistration" );
                    publishers.values().forEach( ref -> ref.forward( msg, context() ) );

                } ).
                match( NewMessage.class, msg -> {
                    System.out.println( "StreamMediator: NewMessage" );
                    publishers.values().forEach( ref -> ref.forward( msg, context() ) );

                } ).
                build() );
    }
}
