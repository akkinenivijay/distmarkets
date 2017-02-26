import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Random;

public class DataProducer {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {

        String file = args[0];
        //String file = "/Users/tidwell/Downloads/iotsmall.json";

        String requestUrl = "http://localhost:8080/distmarkets/data";
        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(file));
            for (int i = 0 ; i < jsonArray.size(); i++) {
                String id = java.util.UUID.randomUUID().toString();
                String timestamp = new Timestamp(System.currentTimeMillis()).toString();
                JSONObject obj = (JSONObject) jsonArray.get(i);
                obj.put("Timestamp", timestamp);
                obj.put("INSPECTION_ID", obj.get("INSPECTION_ID").toString()+"-"+id);
                String objstr = obj.toString();
                sendPostRequest(requestUrl, objstr);
                System.out.println(objstr);
                Random r = new Random();
                int Low = 1;
                int High = 7500;
                int Result = r.nextInt(High-Low) + Low;

                Thread.sleep(Result);
                //System.out.println(obj.toJSONString());
                //String A = (String) obj.get("LOCATION_QUALITY");
                //Long B = (Long) obj.get("INSPECTION_SCORE");
                //Double C =  (Double) obj.get("LONGITUDE");
                //System.out.println(A + " " + B + " " + C +" " );
            }
           // System.out.println(obj);


        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //String payload = "{\"jsonrpc\":\"2.0\",\"method\":\"changeDetail\",\"params\":[{\"id\":11376}],\"id\":2}";


    }


    public static String sendPostRequest(String requestUrl, String payload) {
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(payload);
            writer.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer jsonString = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
            br.close();
            connection.disconnect();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return "good";
    }
}