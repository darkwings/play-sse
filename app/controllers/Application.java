package controllers;

import akka.Protocol;
import akka.Protocol.Init;
import akka.Protocol.NewMessage;
import akka.Protocol.UserRegistration;
import akka.SSEActor;
import akka.StreamMediator;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.javadsl.Source;
import play.libs.EventSource;
import play.libs.EventSource.Event;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import javax.inject.Inject;
import javax.inject.Singleton;

import static play.mvc.Http.MimeTypes.EVENT_STREAM;

@Singleton
public class Application extends Controller {


    private ActorRef streamMediator;

    @Inject
    public Application( ActorSystem actorSystem ) {
        streamMediator = actorSystem.actorOf( Props.create( StreamMediator.class ), "StreamMediator" );
    }

    public Result index() {

        return ok( index.render( "ok" ) );
    }


    public Result createNewUser() {

        streamMediator.tell( new UserRegistration(), ActorRef.noSender() );
        return ok();
    }

    public Result sendMessage() {
        streamMediator.tell( new NewMessage(), ActorRef.noSender() );
        return ok();
    }

    public Result init() {

        streamMediator.tell( new Init(), ActorRef.noSender() );
        return ok();
    }

    public Result stream() {

        System.out.println( "Application::stream" );

        Source<Event, ?> eventSource =
                Source.actorPublisher( SSEActor.props() ).
                        map( msg -> Event.event( (String) msg ) );

        // Facendo riferimento allo schema Source -> Flow -> Sink di Akka Streams
        // Source -> l'attore SSEActor
        // Flow -> EventSource.flow()
        // Sink -> il canale SSE aperto dal client
        return ok().chunked( eventSource.via( EventSource.flow() ) ).as( EVENT_STREAM );
    }
}
