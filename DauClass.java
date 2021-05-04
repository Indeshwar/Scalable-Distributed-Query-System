package info.dau;

import com.opencsv.CSVReader;
import info.model.ClientModel;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class DauClass {

    //Create pairs of keys and values
    public JSONObject insertIntoJson(ClientModel m){
        JSONObject json = new JSONObject();

        //inserting key value pairs in json object
        if(!m.getFristName().equals("None")){
            json.put("firstName", m.getFristName());
        }

        if(!m.getLastName().equals("None")){
            json.put("lastName", m.getLastName());
        }

        if(!m.getAddres().equals("None")){
            json.put("address", m.getAddres());
        }

        if(m.getAge() > 0){
            json.put("age", m.getAge());
        }

        if(m.getSalary() > 0){
            json.put("salary", "$" + m.getSalary());
        }

        return json; //return the json object
    }

    //return all the keys of json object
    public ArrayList<String> keyList(JSONObject obj){
        ArrayList<String> list = new ArrayList<String>();


        for(Iterator<String> s = obj.keySet().iterator(); s.hasNext();){
            list.add(s.next().toString());
        }
        return list;
    }

    //search the query
    public JSONObject search(JSONObject json, String[] nextLine){
        ArrayList<String> list = keyList(json);//keys of all value of query
        String[] attributesName = {"firstName", "lastName", "address", "salary", "age"}; //list of all the attribute of files

        int count = 0;
        for(int i = 0; i < list.size(); i++){

            for(int j = 0; j <attributesName.length; j++){
                if(list.get(i).equals(attributesName[j])){                      //check whether attributesName and keys are same
                    if(json.get(list.get(i)).toString().equalsIgnoreCase(nextLine[j])){  //check whether the value of query match the value of nextLine
                        count++;
                        break;
                    }
                }
            }

        }

        JSONObject json2 = new JSONObject(); //contains all the value of each line of file if query matches

        if(count == list.size()){ //if the value of query matches, store all the value of each line in json2
            for(int i = 0; i < nextLine.length; i++){
                json2.put(attributesName[i], nextLine[i]);
            }

        }

        return json2;  //return json2
    }

    //Read the file and return JSONObject that contains all the searched values
    public JSONObject reader_File(JSONObject obj)
    {
        CSVReader reader = null;
        try
        {

            String path = obj.get("path").toString();  //retrieve path of file from json obj
            JSONObject json = (JSONObject)obj.get("query"); //retrieve json object that has query from json obj

            reader = new CSVReader(new FileReader(path), ',');

            String [] nextLine;

            //Read one line at a time
            JSONObject json3 = new JSONObject();

            //need to use JSONArray
            JSONArray jArr = new JSONArray();   //create an object of JSONArray
            while ((nextLine = reader.readNext()) != null)
            {

                JSONObject json2 = search(json, nextLine); //return json object that contains all the value of each lines if query matches

                if(!json2.isEmpty()){                     //check whether json2 is empty
                    jArr.add(json2);                 //insert json2 in in jsonArray

                }

            }
            json3.put("response", jArr); //insert jArr in json3
            return json3;

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void print(MqttMessage message) throws ParseException {
        String msg = new String(message.getPayload());
        if(!msg.equals("Not found")){
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject)parser.parse(msg);
            JSONObject j1 = (JSONObject)obj.get("response");
            //System.out.printf("%-10s%-10s %-10s %-10s %-10s", "firstName", "lastName", "address", "age", "salary");
            //System.out.println();
            System.out.printf("%-10s%-10s %-10s %-10s %-10s", j1.get("firstName").toString(), j1.get("lastName").toString(), j1.get("address").toString(), j1.get("age").toString(), j1.get("salary").toString());
        }
    }

}
