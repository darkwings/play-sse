package services;

import akka.sse.PublishersManagerActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import javax.inject.Inject;
import javax.inject.Singleton;

import play.Logger;
import play.inject.ApplicationLifecycle;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static services.Constants.PUBLISHERS_MANAGER_ACTOR;

/**
 * Application initializer, called by {@link Module}
 *
 * @author ftorriani
 */
@Singleton
public class ApplicationInitializer {

    private final ActorRef streamMediator;

    private final Clock clock;
    private final ApplicationLifecycle appLifecycle;
    private final Instant start;

    @Inject
    public ApplicationInitializer( ActorSystem actorSystem, Clock clock, ApplicationLifecycle appLifecycle ) {
        Logger.info( "Creating PublishersManagerActor" );
        streamMediator = actorSystem.actorOf( Props.create( PublishersManagerActor.class ), PUBLISHERS_MANAGER_ACTOR );

        this.clock = clock;
        this.appLifecycle = appLifecycle;
        // This code is called when the application starts.
        start = clock.instant();
        Logger.info( "ApplicationTimer demo: Starting application at " + start );

        // When the application starts, register a stop hook with the
        // ApplicationLifecycle object. The code inside the stop hook will
        // be run when the application stops.
        appLifecycle.addStopHook( () -> {
            Instant stop = clock.instant();
            Long runningTime = stop.getEpochSecond() - start.getEpochSecond();
            Logger.info( "ApplicationTimer demo: Stopping application at " + clock.instant() + " after " + runningTime + "s." );
            return CompletableFuture.completedFuture( null );
        } );
    }
}
