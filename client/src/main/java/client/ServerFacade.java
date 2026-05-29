package client;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;

    private final Gson gson=new Gson();

    public ServerFacade(int port){
        this.serverUrl="http://localhost:"+port;
    }
    public AuthData register(String username, String password, String email) throws Exception {
        var body = Map.of("username", username, "password", password, "email", email);

        return makeRequest("POST", "/user", body, null, AuthData.class);
    }


    private <T> T makeRequest(String method, String path, Object body,
                              String authToken, Class<T> responseClass) throws Exception {//helper to take care of all the future functions

        URL url = new URI(serverUrl +  path).toURL();
        HttpURLConnection connection = ( HttpURLConnection ) url.openConnection();

        connection.setRequestMethod(method);

        connection.setRequestProperty("Content-Type", "application/json");

        if (authToken != null) {
            connection.setRequestProperty("authorization", authToken);
        }



        if (body != null) {
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {

                os.write(gson.toJson(body).getBytes());
            }
        }

        int status = connection.getResponseCode();

        if (status >= 400) {// 400 or over is a problem
            InputStream error = connection.getErrorStream();

            String message = "Request failed";

            if (error != null) {
                var errorBody = new String(error.readAllBytes());
                var errorMap = gson.fromJson(errorBody, Map.class);

                if (errorMap.containsKey("message")) {
                    message = (String) errorMap.get("message");
                }
            }
            throw new Exception(message);
        }

        if (responseClass == Void.class) {

            return null;
        }


        try (InputStream is = connection.getInputStream()) {

            return gson.fromJson(new String(is.readAllBytes()), responseClass);
        }
    }
}
