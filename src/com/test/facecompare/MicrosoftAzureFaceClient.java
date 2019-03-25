package com.test.facecompare;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MicrosoftAzureFaceClient {

    private static String END_POINT_DETECT =
            "https://westeurope.api.cognitive.microsoft.com/face/v1.0/detect?overload=stream";
    private static String END_POINT_VERIFY =
            "https://westeurope.api.cognitive.microsoft.com/face/v1.0/verify";
    private static String ACCESS_KEY = "____________________________________";
    private HttpClient httpClient = HttpClientBuilder.create().build();

    public String getFacesSimilarity(File firstImage, File secondImage) throws IOException, URISyntaxException {

        if (firstImage == null) {
            return "First image is NULL";
        }
        if (secondImage == null) {
            return "Second image is NULL";
        }

        String firstFaceId = getFaceId(firstImage);
        String secondFaceId = getFaceId(secondImage);

        if (firstFaceId != null && secondFaceId != null) {
            String result = getVerifyResult(firstFaceId, secondFaceId);
            if (result != null && !result.isEmpty()) {
                return result;
            }
        } else {
            StringBuilder result = new StringBuilder("Not found face in images: \n");
            if (firstFaceId == null) {
                result.append(" - Image 1\n");
            }
            if (secondFaceId == null) {
                result.append(" - Image 2\n");
            }
            return result.toString();
        }

        return "Something went wrong... Please try again.";
    }

    private String getVerifyResult(String firstFaceId, String secondFaceId) throws URISyntaxException, IOException {

        URIBuilder builder = new URIBuilder(END_POINT_VERIFY);
        builder.setParameter("returnFaceId", "true");

        URI uri = builder.build();
        HttpPost request = new HttpPost(uri);
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Ocp-Apim-Subscription-Key", ACCESS_KEY);

        JSONObject requestBody = new JSONObject();
        requestBody.put("faceId1", firstFaceId);
        requestBody.put("faceId2", secondFaceId);

        StringEntity reqEntity = new StringEntity(requestBody.toString());
        request.setEntity(reqEntity);

        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();

        if (entity != null) {

            String jsonString = EntityUtils.toString(entity).trim();
            JSONObject jsonObject = new JSONObject(jsonString);
            StringBuilder result = new StringBuilder();
            result.append("Is identical result: ");
            result.append(jsonObject.getBoolean("isIdentical") + "\n");
            result.append("Confidence: ");
            result.append(jsonObject.getDouble("confidence"));

            return result.toString();

        }

        return null;
    }

    private String getFaceId(File imageFile) throws IOException, URISyntaxException {

        URIBuilder builder = new URIBuilder(END_POINT_DETECT);
        builder.setParameter("returnFaceId", "true");

        URI uri = builder.build();
        HttpPost request = new HttpPost(uri);
        request.setHeader("Content-Type", "application/octet-stream");
        request.setHeader("Ocp-Apim-Subscription-Key", ACCESS_KEY);

        ByteArrayEntity reqEntity = new ByteArrayEntity(
                Files.readAllBytes(Paths.get(imageFile.getPath())),
                ContentType.APPLICATION_OCTET_STREAM
        );
        request.setEntity(reqEntity);

        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();

        if (entity != null) {

            String jsonString = EntityUtils.toString(entity).trim();
            JSONArray jsonArray = new JSONArray(jsonString);
            if (jsonArray.length() > 0) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String faceId = jsonObject.getString("faceId");
                return faceId;
            }

        }

        return null;

    }

}
