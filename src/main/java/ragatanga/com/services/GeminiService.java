package ragatanga.com.services;

import com.google.gson.*;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;
import ragatanga.com.httpClient.HttpClient;
import ragatanga.com.httpClient.HttpRequest;
import ragatanga.com.model.FileModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${GEMINI_API_KEY}")
    private String geminiApiKey;

    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";
    private final String prompt;

    public GeminiService() throws IOException {
        ClassPathResource resource = new ClassPathResource("prompt.txt");
        byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
        this.prompt = new String(bytes, StandardCharsets.UTF_8);
    }

    public Map<String, Object> generateContent(List<FileModel> files) throws IOException {
        ClassPathResource resource = new ClassPathResource("prompt.txt");
        byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
        String prompt = new String(bytes, StandardCharsets.UTF_8);

        Map<String, Object> responses = new HashMap<>();
        for (FileModel file : files) {
            Map mapResponse = generateContent(file);
            responses.put(file.name(), mapResponse);
        }

        return responses;
    }

    public Map generateContent(FileModel file) {
        JsonObject inlineData = new JsonObject();
        inlineData.addProperty("mime_type", "application/pdf");
        inlineData.addProperty("data", file.base64());

        JsonObject part1 = new JsonObject();
        part1.add("inline_data", inlineData);

        JsonObject part2 = new JsonObject();
        part2.addProperty("text", prompt);

        JsonArray partsArray = new JsonArray();
        partsArray.add(part1);
        partsArray.add(part2);

        JsonObject content = new JsonObject();
        content.add("parts", partsArray);

        JsonArray contentsArray = new JsonArray();
        contentsArray.add(content);

        JsonObject payload = new JsonObject();
        payload.add("contents", contentsArray);

        HttpClient httpClient = new HttpClient();
        JsonElement asJson = httpClient.execute(HttpRequest
                        .post()
                        .url(GEMINI_URL + geminiApiKey)
                        .addHeader("content-type", "application/json")
                        .setHttpEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON)))
                .getAsJson();

        String response = asJson
                .getAsJsonObject()
                .getAsJsonArray("candidates")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0)
                .getAsJsonObject()
                .get("text")
                .getAsString();

        response = response.replaceAll("(?i)```json", "").replaceAll("```", "").trim();
        if(response.startsWith("[")) {
           List<Map> res = new Gson().fromJson(response, List.class);
           return res.get(0);
        }
        return new Gson().fromJson(response, Map.class);
    }
}
