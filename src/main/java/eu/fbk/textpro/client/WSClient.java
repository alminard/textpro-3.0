package eu.fbk.textpro.client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: cgirardi
 * Date: 03/07/14
 * Time: 00.45
 */
public class WSClient {
    static final String TEXTPROAPIURL = "http://hlt-services2.fbk.eu:8080/textpro/api/";

    public static void main(String[] argv) throws Exception {
        if (argv.length > 3) {
            //concat key and format type
            URL url = new URL(TEXTPROAPIURL+"?key=" + argv[0] + "&format=" +argv[1]);

            String params = argv[2];

            JSONObject jsonInput = new JSONObject();
            jsonInput.put("params",params);
            JSONArray content = new JSONArray();
            for (int i=3; i<argv.length; i++) {
                content.add(loadFile(argv[i]));
            }
            jsonInput.put("content",content);
            String jsonContent = jsonInput.toJSONString();

            //System.err.println(jsonContent.toString());

            System.err.println("Connecting... "+url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Content-Length", "" + Integer.toString(jsonContent.getBytes().length));
            connection.setUseCaches(false);


            OutputStreamWriter  writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            writer.write(jsonContent);
            writer.close();


            InputStream response;

            // Check for error , if none store response
            if(connection.getResponseCode() == 200){
                response = connection.getInputStream();
            }else{
                response = connection.getErrorStream();
                System.err.println("> " +"Response code: "+connection.getResponseCode()+" and mesg:"+connection.getResponseMessage());
            }
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(response));
            String read;
            while((read = br.readLine()) != null){
                sb.append(read+"\n");
            }
            // Print the TextPro annotations
            System.out.println(sb.toString());

            connection.disconnect();
        } else {
            System.err.println("Input paramaters must be: [KEY] [txp|json] [TEXTPRO OPTIONS] <FILE1 ... FILEn>");
        }

    }


    /** Carica i dati da file
     * @param fileName del file
     * @return text
     * @exception IOException se il file non e` presente.
     */
    public static String loadFile(String fileName) throws IOException {
        System.err.println("Loading... " + fileName);

        //creazione del flusso di lettura con buffer
        BufferedReader buff = null;
        StringBuffer text = new StringBuffer();
        File file = new File(fileName);
        if (file.exists()) {
            try {
                InputStreamReader reader = new InputStreamReader(new FileInputStream(fileName), "UTF8");
                buff = new BufferedReader(reader);
                String line;
                while ((line = buff.readLine()) != null) {
                    text.append(line+"\n");
                }
                buff.close();
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            } finally { buff.close(); }
        }
        return text.toString();

    }

}
