package info.worker;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import info.dau.DauClass;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.simple.JSONObject;

public class Worker_Actor_2 extends UntypedActor {

    //print debugging message
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void preStart(){   //what to do when this worker is created and started
        log.info("Worker Actor2 starting");

    }

    @Override
    public void onReceive(Object message){ // what to do when message is arrived
        if(message instanceof JSONObject){
            log.info("Message Received: " + message.toString()); //print message
            log.info("Sender of message: " + getSender().path().name().toString()); //print the identity of sender
            JSONObject obj = (JSONObject)message; //type casting

            DauClass dau = new DauClass(); //create an object of DauClass
            JSONObject json = dau.reader_File(obj); //Read the file and return JSONObject that contains all the searched values

            getSender().tell(json, getSelf()); //send the response to the peer1 after completing its task


        }else if(message instanceof String && message.equals("done")){
            log.info("Message Received: " + message.toString());   //print the message
            log.info("Sender of message: " + getSender().path().name().toString()); //print the identity of sender
            getContext().stop(getSelf());   //terminate by itself

        }else{
            unhandled(message); //received undefined msg
        }
    }

    public void postStop(){ //what to when terminating
        log.info("terminating");
    }
}
