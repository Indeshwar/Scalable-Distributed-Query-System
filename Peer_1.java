package info.main;


import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import info.worker.Worker_Actor_1;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.nio.charset.StandardCharsets;

public class Peer_1 extends UntypedActor{
    private static int start = 1;
    private static int end = 0;
    private static MqttClient peer1 = null;

    //print debugging message
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void preStart(){ //what to do when this worker is created and started

        String broker = "tcp://broker.hivemq.com:1883"; //address of broker
        String clientId = "ChaudharyPeer_1";  //Client ID

        log.info("Peer1 Boss Actor starting");

        try{
            //Create an object of MqttClient that is used to communicate with Mqtt broker
            peer1 = new MqttClient(broker, clientId, new MemoryPersistence());

            //Create an object MqttConnection to invoke the method setCleanSession
            MqttConnectOptions conOptions = new MqttConnectOptions();

            //establish non persistent session
            //it means broker won't save any message for new client
            conOptions.setCleanSession(true);

            //establish TCP connection with broker
            peer1.connect(conOptions);


            peer1.subscribe("/DS341/TaskTo/Chaudhary/Client", 1); //subscribe the topic
            System.out.println("Subscribed");
            System.out.println("Listening");

            //set callback listener to use for event that happens asynchronously
            peer1.setCallback(new MqttCallback() {

                @Override
                public void connectionLost(Throwable cause) {

                }


                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String msg = new String(message.getPayload());
                    System.out.println("\nReceived a Message!" +
                            "\n\tTopic:   " + topic +
                            "\n\tMessage: " + msg +
                            "\n\tQoS:     " + message.getQos() + "\n");

                    JSONParser parser = new JSONParser(); //create an object of JSONParser to invoke the method parse
                    JSONObject jsonObj1 = (JSONObject)parser.parse(msg); //parse the String into JSOnObject




                    end = start+4;
                    int fileNo = 1; //file number
                    while(start < end) {
                        JSONObject jsonObj2 = new JSONObject(); //create another JSONObject that will contain value as path and query
                        String path = "/Users/indeshwarchaudhary/Desktop/myfile/data_1/file" + fileNo + ".csv"; //path of the file
                        jsonObj2.put("path", path);      //put path in JsonObject2
                        jsonObj2.put("query", jsonObj1); //put query in jsonObect2
                        String w = "Worker" + start;     //identity of worker
                        ActorRef worker = getContext().actorOf(Props.create(Worker_Actor_1.class), w); //create a worker
                        worker.tell(jsonObj2, getSelf()); //Send the message to its worker with its identity
                        fileNo++;
                        start++;
                    }


                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });



        }catch(MqttException me){
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }

    }

    @Override
    public void onReceive(Object message) throws MqttException {  //what to do when message is received
      if(message instanceof JSONObject){
            log.info("Message arrived:" + message.toString());  //print the received message
            log.info("Sender of this message:" + getSender().path().name().toString()); //print the identity of sender
            String m;
            if(!((JSONObject) message).isEmpty()) { //if the message is not empty, do the following task
                m = message.toString();                                         //convert json object into String
                MqttMessage msg = new MqttMessage(m.getBytes(StandardCharsets.UTF_8));// build the message
                msg.setQos(1);                                                       // set Quality of Service 1
                peer1.publish("/DS341/ResultFrom/Chaudhary/peer1", msg);      //publish the message
                System.out.println("Published");

            }else{
                m  = "Not found";
                MqttMessage msg = new MqttMessage(m.getBytes(StandardCharsets.UTF_8));// build the message
                msg.setQos(1);                                                       // set Quality of Service 1
                peer1.publish("/DS341/ResultFrom/Chaudhary/peer1", msg);      //publish the message
                System.out.println("Published");
            }
            getSender().tell(new String("done"), getSelf());          //send the "done" message to the worker

        }else{
            unhandled(message); //received undefined msg
        }
    }

    public void postStop(){         //what to do when terminating
        log.info("terminating");
    }

}
