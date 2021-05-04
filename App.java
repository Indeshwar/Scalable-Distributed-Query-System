package info.main;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {

        // The ActorSystem extends “ActorRefFactory“ which is a hierarchical group of actors which share common configuration.
        // It is also the entry point for creating or looking up actors.
        ActorSystem system = ActorSystem.create("Hello");

        ActorRef bossActor1 = system.actorOf(Props.create(Peer_1.class), "PeerActor1"); //Create a Peer_1 actor
        ActorRef bossActor2 = system.actorOf(Props.create(Peer_2.class), "PeerActor2"); //Create a Peer_1 actor
    }
}
