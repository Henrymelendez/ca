package com.cau.visavau.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.util.Base64;

public class ApiClient {

    private static final String VISA_BASE_URL = "https://sandbox.api.visa.com";
    private static final String KEYSTORE_PATH = "/path/to/your/keystore.jks";
    private static final String KEYSTORE_PASSWORD = "your_keystore_password";
    private static final String USER_ID = "your_user_id";
    private static final String PASSWORD = "your_password";

    public static String invokeAPI(final String resourcePath, String httpMethod, String payload) throws Exception {
        String url = VISA_BASE_URL + resourcePath;
        System.out.println("Calling API: " + url);

        SSLContext sslContext = createSSLContext();
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
        }

        prepareConnection(connection, httpMethod);

        if (payload != null && !payload.trim().isEmpty()) {
            sendPayload(connection, payload);
        }

        return readResponse(connection);
    }

    private static SSLContext createSSLContext() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream keystoreStream = new FileInputStream(KEYSTORE_PATH)) {
            keyStore.load(keystoreStream, KEYSTORE_PASSWORD.toCharArray());
        }

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, new java.security.SecureRandom());

        return sslContext;
    }

    private static void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
        connection.setRequestMethod(httpMethod);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((USER_ID + ":" + PASSWORD).getBytes(StandardCharsets.UTF_8)));
        // Optional: Set connection timeout, read timeout, etc.
        // connection.setConnectTimeout(10000);
        // connection.setReadTimeout(10000);
    }

    private static void sendPayload(HttpURLConnection connection, String payload) throws IOException {
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

    private static String readResponse(HttpURLConnection connection) throws IOException {
        int status = connection.getResponseCode();
        System.out.println("Http Status: " + status);

        InputStream inputStream = (status >= 200 && status < 300) ? connection.getInputStream() : connection.getErrorStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }
}

