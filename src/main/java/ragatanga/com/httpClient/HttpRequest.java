package ragatanga.com.httpClient;

import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.util.Timeout;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpRequest {
    private String method = "GET";
    private String url;
    private final List<NameValuePair> params = new ArrayList<>();
    private final List<NameValuePair> queries = new ArrayList<>();

    private final List<Header> headers = new ArrayList<>();
    private HttpEntity httpEntity;

    private boolean followRedirects = true;
    private int timeout = 30000;
    private boolean isMultipart = false;
    private final List<Map.Entry<String, File>> multipartFiles = new ArrayList<>();
    private Charset charset;

    private HttpRequest(String method) {
        this.method = method;
        this.charset = StandardCharsets.UTF_8;
    }

    public static HttpRequest get() {
        return new HttpRequest("GET");
    }

    public static HttpRequest post() {
        return new HttpRequest("POST");
    }

    public static HttpRequest put() {
        return new HttpRequest("PUT");
    }

    public static HttpRequest patch() {
        return new HttpRequest("PATCH");
    }

    public String getMethod() {
        return this.method;
    }

    public HttpRequest url(String url) {
        this.url = url;
        return this;
    }

    public String getUrl() {
        return this.url;
    }

    public HttpRequest addHeader(String name, String value) {
        this.headers.add(new BasicHeader(name, value));
        return this;
    }

    public HttpRequest addParameter(String name, String value, Charset charset) {
        try {
            String encoded = new String(value.getBytes(StandardCharsets.UTF_8), charset);
            this.params.add(new BasicNameValuePair(name, encoded));
        } catch (Exception e) {
            this.params.add(new BasicNameValuePair(name, value));
        }
        return this;
    }

    public HttpRequest addParameter(String name, String value) {
        return this.addParameter(name, value, StandardCharsets.UTF_8);
    }

    public HttpRequest addQueryParam(String name, String value) {
        this.queries.add(new BasicNameValuePair(name, value));
        return this;
    }

    public HttpRequest addParameter(Map<String, String> parameters, Charset charset) {
        parameters.forEach((name, value) -> addParameter(name, value, charset));
        return this;
    }

    public HttpRequest addParameter(Map<String, String> parameters) {
        parameters.forEach(this::addParameter);
        return this;
    }

    public HttpRequest ignoreRedirect() {
        this.followRedirects = false;
        return this;
    }

    public HttpRequest timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public HttpRequest setHttpEntity(HttpEntity entity) {
        this.httpEntity = entity;
        return this;
    }

    public HttpRequest setBinaryFile(File file) {
        this.httpEntity = new FileEntity(file, ContentType.APPLICATION_OCTET_STREAM);;
        return this;
    }

    public HttpRequest multipart() {
        this.isMultipart = true;
        return this;
    }

    public HttpRequest addFile(File file) {
        return addFile("file", file);
    }

    public HttpRequest addFile(String fieldName, File file) {
        this.multipartFiles.add(Map.entry(fieldName, file));
        return this;
    }

    public HttpRequest addEmptyFile() {
       return addEmptyFile("file");
    }

    public HttpRequest addEmptyFile(String fieldName) {
        try {
            File file = new File("arquivo.txt");
            file.createNewFile();
            this.multipartFiles.add(Map.entry(fieldName, file));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao adicionar arquivo vazio", e);
        }
        return this;
    }

    public HttpRequest charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public HttpUriRequestBase build() {
        if (url == null) throw new IllegalStateException("URL não definida");
        String query = "";
        if (!queries.isEmpty()) {
            query = "?" + queries.stream()
                    .map(p -> p.getName() + "=" + p.getValue())
                    .reduce((a, b) -> a + "&" + b)
                    .orElse("");
        }

        HttpUriRequestBase request;
        switch (method) {
            case "POST" -> request = new HttpPost(url + query);
            case "PATCH" -> request = new HttpPatch(url + query);
            case "PUT"  -> request = new HttpPut(url + query);
            case "DELETE" -> request = new HttpDelete(url + query);
            default -> request = new HttpGet(url + query);
        }

        if (isMultipart) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(StandardCharsets.UTF_8);

            for (NameValuePair param : this.params) {
                builder.addPart(param.getName(),
                        new StringBody(param.getValue(), ContentType.TEXT_PLAIN.withCharset(charset)));
            }

            for (Map.Entry<String, File> entry : this.multipartFiles) {
                String fieldName = entry.getKey();
                File file = entry.getValue();

                if (file == null) {
                    builder.addBinaryBody(fieldName, new byte[0], ContentType.APPLICATION_OCTET_STREAM, "");
                } else {
                    builder.addBinaryBody(fieldName, file, ContentType.APPLICATION_OCTET_STREAM, file.getName());
                }
            }

            this.httpEntity = builder.build();
        } else if (!this.params.isEmpty() && this.httpEntity == null) {
            this.httpEntity = new UrlEncodedFormEntity(this.params, charset);
        }

        headers.forEach(request::addHeader);

        RequestConfig config = RequestConfig.custom()
                .setRedirectsEnabled(followRedirects)
                .setResponseTimeout(Timeout.of(timeout, TimeUnit.SECONDS))
                .build();

        request.setConfig(config);

        if(httpEntity != null) request.setEntity(httpEntity);

        return request;
    }
}
