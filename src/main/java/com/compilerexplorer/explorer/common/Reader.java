package com.compilerexplorer.explorer.common;

import com.compilerexplorer.common.ExplorerUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.openapi.progress.ProgressIndicator;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.BiConsumer;

@SuppressWarnings("BlockingMethodInNonBlockingContext")
public class Reader {
    @NotNull
    public static String read(@NonNls @NotNull String endpoint, @NotNull CloseableHttpClient httpClient, @NotNull CloseableHttpResponse response, @NotNull ProgressIndicator indicator) throws IOException {
        if (response.getStatusLine().getStatusCode() != 200) {
            httpClient.close();
            response.close();
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode() + " from " + endpoint);
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            indicator.checkCanceled();
            output.append(line);
        }
        httpClient.close();
        response.close();
        indicator.checkCanceled();

        return output.toString();
    }

    @NotNull
    public static <T> String readObjects(@NonNls @NotNull String endpoint, @NotNull ProgressIndicator indicator, @NotNull Class<T> clazz, @NotNull BiConsumer<T, String> consumer) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet getRequest = new HttpGet(endpoint);
        getRequest.addHeader(ExplorerUtil.ACCEPT_HEADER, ExplorerUtil.JSON_MIME_TYPE);
        CloseableHttpResponse response = httpClient.execute(getRequest);

        String rawOutput = Reader.read(endpoint, httpClient, response, indicator);

        JsonArray array = JsonParser.parseString(rawOutput).getAsJsonArray();
        Gson gson = new Gson();
        for (JsonElement elem : array) {
            consumer.accept(gson.fromJson(elem, clazz), elem.toString());
        }
        indicator.checkCanceled();

        return rawOutput;
    }
}
