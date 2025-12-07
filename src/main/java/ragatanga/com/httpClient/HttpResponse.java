package ragatanga.com.httpClient;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public record HttpResponse(CloseableHttpResponse response, String url) {

    public String getContent(Charset charset){
        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity, StandardCharsets.ISO_8859_1).replace("\uFEFF", "").trim();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public Document getAsDocument(Charset charset){
        String content = getContent(charset);
        if(content == null) return null;

        return Jsoup.parse(content, url);
    }

    public Document getAsDocument(){
        return getAsDocument(StandardCharsets.UTF_8);
    }

    public JsonElement getAsJson(Charset charset) {
        String content = getContent(charset);
        if(content == null) return null;

        return JsonParser.parseString(content);
    }

    public JsonElement getAsJson() {
        return getAsJson(StandardCharsets.UTF_8);
    }

    public byte[] getContentAsBytes() {
        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toByteArray(entity);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public File saveToTempFile(String prefix, String suffix) {
        try {
            File tempFile = File.createTempFile(prefix, suffix);
            tempFile.deleteOnExit();

            byte[] data = getContentAsBytes();
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                out.write(data);
            }
            return tempFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getHeader(String header) {
        try {
            return response.getHeader(header).getValue();
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }
    }

    public int getStatusCode() {
        return response.getCode();
    }


}
