import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class DataProducer {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {

        String requestUrl = "http://localhost:8080/distmarkets/data";
        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonArray = (JSONArray) parser.parse(new FileReader("/Users/tidwell/Downloads/iotsmall.json"));


            for (int i = 0 ; i < jsonArray.size(); i++) {
                JSONObject obj = (JSONObject) jsonArray.get(i);
                String objstr = obj.toString();
                sendPostRequest(requestUrl, objstr);
                System.out.println(objstr);
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