package controllers;

import akka.Protocol.LogActors;
import akka.Protocol.Message;
import akka.SSEActor;
import akka.UtilsActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.javadsl.Source;
import play.Logger;
import play.libs.EventSource;
import play.libs.EventSource.Event;
import play.mvc.Controller;
import play.mvc.Result;
import services.Constants;
import views.html.dashboard;
import views.html.index;

import javax.inject.Inject;
import javax.inject.Singleton;

import static play.mvc.Http.MimeTypes.EVENT_STREAM;


@Singleton
public class Application extends Controller {

    private ActorSelection streamMediator;

    private ActorRef utils;

    @Inject
    public Application( ActorSystem actorSystem ) {
        streamMediator = actorSystem.actorSelection( "/user/" + Constants.STREAM_MEDIATOR_ACTOR_NAME );

        utils = actorSystem.actorOf( Props.create( UtilsActor.class ), "UtilsActor" );
    }

    /**
     * home page
     */
    public Result index() {
        Logger.debug( "Application: index" );
        return ok( index.render( "ok" ) );
    }

    /**
     * home page
     */
    public Result dashboard() {
        Logger.debug( "Application: dashboard" );
        return ok( dashboard.render() );
    }


    /**
     * Sends a message to all the registered SSE publishers
     * @param text the text to be sent
     * @return a result object
     */
    public Result message( String text ) {
        Logger.debug( "Application: message [{}]", text );
        streamMediator.tell( new Message( text ), ActorRef.noSender() );
        return ok();
    }

    /**
     * Logs the available actors in the log file
     *
     * @return a HTTP 200
     */
    public Result logActors() {
        Logger.debug( "Application: log" );
        utils.tell( new LogActors(), ActorRef.noSender() );
        return ok();
    }

    /**
     * Opens a SSE connection backed by an Akka stream
     *
     * @return a chunked result
     */
    public Result stream() {

        Logger.debug( "Application: start stream" );

        Source<Event, ?> eventSource =
                Source.actorPublisher( SSEActor.props() ).
                        map( msg -> Event.event( (String) msg ) );

        return ok().chunked( eventSource.via( EventSource.flow() ) ).as( EVENT_STREAM );
    }
}
