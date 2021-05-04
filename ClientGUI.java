package info.client;

import info.dau.DauClass;
import info.model.ClientModel;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ClientGUI extends JFrame {
    private JTextField firstName;
    private JTextField lastName;
    private JTextField address;
    private JTextField age;
    private JTextField salary;
    private JButton button;

    private DefaultTableModel tableModel = new DefaultTableModel();
    private JTable table = new JTable(tableModel);

    public ClientGUI(){

    }

    //This method communicate with broker
    //publish the the query and also received message on subscribed topic
    public void publishQuery(ClientModel m){
        String broker = "tcp://broker.hivemq.com:1883"; //address of broker
        String clientId = "ChaudharyCient";  //Client ID

        try{
            //Create an object of MqttClient that is used to communicate with an Mqtt broker
            final MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());

            //Create an object MqttConnectOptions
            MqttConnectOptions conOptions = new MqttConnectOptions();

            //Establish non persistent session
            conOptions.setCleanSession(true);

            //Establish TCP connection with broker
            client.connect(conOptions);

            System.out.println("Connected to Broker" + broker);

            //listen the event that happens asynchronously
            //allocate an anonymous instance of anonymous class that implements MqttCallback interface
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String msg = new String(message.getPayload());  //Convert the payload to an object of String
                    System.out.println("\nReceived a Message!" +
                            "\n\tTopic:   " + topic +
                            "\n\tMessage: " + msg +
                            "\n\tQoS:     " + message.getQos() + "\n");

                    if(!msg.equals("Not found")) {
                        JSONParser parser = new JSONParser();       //Create an object of JSONParser to use its method
                        JSONObject obj = (JSONObject) parser.parse(msg);  //Parse the JSON formatted String into JSONObject
                        JSONArray jArr =  (JSONArray)obj.get("response"); //Retrieve the response from JSONObject which is list of json objects
                        for(int i = 0; i < jArr.size(); i++){
                            JSONObject j1 = (JSONObject)jArr.get(i);
                            //add the all data of a response in a rows of a table
                            tableModel.addRow(new Object[] { j1.get("firstName"),j1.get("lastName"), j1.get("address"), j1.get("age"), j1.get("salary")});
                        }

                    }

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });

            //subscribe the following topic
            client.subscribe("/DS341/ResultFrom/Chaudhary/#", 1);
            System.out.println("Subscribed");
            System.out.println("Listening");


            DauClass dau = new DauClass(); //create an object of DauClass
            if(!(m.getFristName().equals("None") && m.getLastName().equals("None") && m.getAddres().equals("None") && m.getAge() == 0 && m.getSalary() == 0)){
                JSONObject obj = dau.insertIntoJson(m);  //return an instance of JSON that contains the query

                String s = obj.toString();  //convert the Json object to an Object of String
                MqttMessage message = new MqttMessage(s.getBytes(StandardCharsets.UTF_8)); //build the message
                message.setQos(1); //set the Quality of Service as 1
                client.publish("/DS341/TaskTo/Chaudhary/Client", message); //publish the message on given topic
                System.out.println("published");
            }

        }catch(MqttException me){
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }

    //Display the user page and table
    public void displayPage(){

        // Retrieve the content-pane of the top-level container JFrame
        // All operations done on the content-pane
        Container cp = getContentPane();
        cp.setLayout(new GridLayout(6, 2, 5, 5));  // The content-pane sets its layout


        cp.add(new JLabel("Enter firstName:"));        //Containers adds an anonymous JLabel
        firstName = new JTextField(10);             //Construct JTextField
        cp.add(firstName);                                  //Container adds JTextField

        cp.add(new JLabel("Enter lastName:"));        //Containers adds an anonymous JLabel
        lastName = new JTextField(10);             //Construct JTextField
        cp.add(lastName);                                  //Container adds JTextField

        cp.add(new JLabel("Enter address:"));          //Containers adds an anonymous JLabel
        address = new JTextField(10);               //Construct JTextField
        cp.add(address);                                    //Container adds JTextField

        cp.add(new JLabel("Enter age:"));               //Containers adds an anonymous JLabel
        age = new JTextField("0", 10);                    //Construct JTextField
        cp.add(age);                                         //Container adds JTextField

        cp.add(new JLabel("Enter salary:"));            //Containers adds an anonymous JLabel
        salary = new JTextField("0", 10);                 //Construct JTextField
        cp.add(salary);

        button = new JButton("Search");                 //Create an object JButton
        cp.add(button);

        displayTable();                                      //display table

        // Allocate an anonymous instance of an anonymous inner class that
        //  implements ActionListener as ActionEvent listener
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {

                String fName = firstName.getText(); //get first name
                firstName.setText(""); //clear the TextField

                if(fName.equals("")){
                    fName = "None";
                }

                String lName = lastName.getText(); //get lastName
                lastName.setText("");              //clear the TextField

                if(lName.equals("")){
                    lName = "None";
                }

                String addr = address.getText();   //get address
                address.setText("");               //clear the TextField

                if(addr.equals("")){
                    addr = "None";
                }

                String ages = age.getText();       //get age
                age.setText("");                   //clear the TextField

                int age = 0;
                float salry = 0f;

                if(ages.equals("")){                //if age is empty, set default value 0
                    age = Integer.parseInt("0");
                }else{
                    age = Integer.parseInt(ages);
                }

                String sal = salary.getText();     //get salary
                salary.setText("");                //clear the TextField

                if(sal.equals("")){                //if salary is empty, set default value 0
                    salry = Float.parseFloat("0");
                }else{
                    salry = Float.parseFloat(sal);
                }

                ClientModel m = new ClientModel();  //create an object of ClientModel Class
                //set firstName, lastName, address, age, salary  in ClientModel
                m.setFristName(fName);
                m.setLastName(lName);
                m.setAddres(addr);
                m.setAge(age);
                m.setSalary(salry);

                try{
                    publishQuery(m);        //publish query and also received message on subscribed topic
                }catch(Exception e){
                    e.printStackTrace();
                }



            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Exit program if close-window button clicked
        setTitle("Search Page"); // "super" Frame sets title
        setSize(350, 300);  // "super" Frame sets initial size
        setVisible(true);   // "super" Frame shows



    }

    //Display the table
    public void displayTable(){
        tableModel.addColumn("firtName");         //add column firstName in table
        tableModel.addColumn("lastName");         //add column lastName in table
        tableModel.addColumn("address");          //add column address in table
        tableModel.addColumn("age");              //add column age in table
        tableModel.addColumn("salary");           //add column salary in table

        table.setBackground(Color.orange);  //set background color of table
        table.setForeground(Color.black);   //set foreground color of table
        Font font = new Font("Verdana", Font.CENTER_BASELINE, 12);
        table.setFont(font); //set font

        JFrame frame = new JFrame();           //Create an object of JFrame
        frame.setSize(600, 400);  //frame sets initial size
        frame.add(new JScrollPane(table));     //frame adds table on it
        frame.setVisible(true);

    }

    public static void main(String[] args){
        // Run the GUI construction in the Event-Dispatching thread for thread-safety
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ClientGUI gui = new ClientGUI();
                gui.displayPage();

            }
        });

    }
}
