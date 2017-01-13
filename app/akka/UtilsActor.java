package akka;

import akka.Protocol.LogActors;
import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import play.Logger;

/**
 * @author ftorriani
 */
public class UtilsActor extends AbstractActor {

    public UtilsActor() {
        receive( ReceiveBuilder.
                match( LogActors.class, msg -> {
                    ActorSelection selection =
                            getContext()
                                    .actorSelection( "/user/*" );
                    selection.tell( new Identify( "id" ), context().self() );

                    selection =
                            getContext()
                                    .actorSelection( "/system/*" );
                    selection.tell( new Identify( "id" ), context().self() );

                    selection =
                            getContext()
                                    .actorSelection( "/system/StreamSupervisor*/*" );

                    if ( selection != null ) {
                        selection.tell( new Identify( "id" ), context().self() );
                    }

                } ).
                match( ActorIdentity.class, msg -> {

                    ActorRef ref = msg.getRef();
                    if ( ref != null ) {
                        Logger.info( "UtilsActor: Available actor: {}", ref.toString() );
                    }

                } ).build());
    }
}
