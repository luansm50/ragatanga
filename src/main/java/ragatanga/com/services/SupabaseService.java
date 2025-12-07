package ragatanga.com.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;
import ragatanga.com.model.FileModel;
import stefanini.intelligent.httpClient.HttpClient;
import stefanini.intelligent.httpClient.HttpRequest;
import stefanini.intelligent.httpClient.HttpResponse;
import stefanini.intelligent.utils.FileUtil;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class SupabaseService {
    private final String SUPABASE_URL = "https://peaiobkiqrjratcfyayn.supabase.co";
    private final String SUPABASE_API_KEY = "sb_secret_mkjlfU7L2rGwAxkbUo9dGA_1HgGPrT6";
    private final String BUCKET = "documents";

    public void uploadFile(String folderId, String filename, String base64) throws IOException {
        String url = SUPABASE_URL + "/storage/v1/object/" + BUCKET + "/" + folderId + "/" + filename;

        HttpClient client = new HttpClient();
        HttpResponse response = client.execute(HttpRequest
                .post()
                .url(url)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .multipart()
                .addFile("file_path", FileUtil.base64ToFile(base64, UUID.randomUUID().toString()))
        );

        if (response.getStatusCode() != HttpStatus.SC_OK)
            throw new RuntimeException("Failed to upload file to Supabase Storage. Status code: " + response.getStatusCode());
    }

    public byte[] downloadFile(String folderId, String id) throws IOException {
        String url = SUPABASE_URL + "/storage/v1/object/" + BUCKET + "/" + folderId + "/" + id;

        HttpClient httpClient = new HttpClient();
        return httpClient.execute(HttpRequest
                .get()
                .url(url)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
        ).getContentAsBytes();
    }

    public void insert(String tableName, Object payload) throws IOException {
        String url = SUPABASE_URL + "/rest/v1/" + tableName;

        HttpClient httpClient = new HttpClient();

        httpClient.execute(HttpRequest
                .post()
                .url(url)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .setHttpEntity(new StringEntity(new Gson().toJson(payload), ContentType.APPLICATION_JSON))
        );
    }

    public void update(String tableName, String id, Object payload) throws IOException {
        String url = SUPABASE_URL + "/rest/v1/" + tableName + "?id=eq." + id;

        HttpClient httpClient = new HttpClient();
        httpClient.execute(HttpRequest
                .patch()
                .url(url)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .setHttpEntity(new StringEntity(new Gson().toJson(payload), ContentType.APPLICATION_JSON))
        );
    }

    public JsonArray fetchRecords(String tableName, Map<String, String> filters) throws IOException {
        StringBuilder urlBuilder = new StringBuilder(SUPABASE_URL)
                .append("/rest/v1/")
                .append(tableName);

        if (filters != null && !filters.isEmpty()) {
            urlBuilder.append("?");
            filters.forEach((key, value) ->
                    urlBuilder.append(key).append("=eq.").append(value).append("&")
            );
            urlBuilder.setLength(urlBuilder.length() - 1);
        }

        HttpClient httpClient = new HttpClient();
        JsonElement response = httpClient.execute(HttpRequest
                .get()
                .url(urlBuilder.toString())
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
        ).getAsJson();

        return response.getAsJsonArray();
    }
}