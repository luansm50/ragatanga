package ragatanga.com.httpClient;

import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MultipartRequest {
    private MultipartEntityBuilder multipartEntityBuilder;
    private final List<NameValuePair> params = new ArrayList<>();

    private MultipartRequest(byte[] arquivoBytes, String nomeArquivo) {
        this.multipartEntityBuilder = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.STRICT)
                .addBinaryBody("file", arquivoBytes, ContentType.APPLICATION_OCTET_STREAM, nomeArquivo);
    }

    public static MultipartRequest create(String base64, String nomeArquivo) {
        byte[] arquivoBytes = Base64.getDecoder().decode(base64);
        return new MultipartRequest(arquivoBytes, nomeArquivo);
    }

    public MultipartRequest addTextBody(String name, String value) {
        this.params.add(new BasicNameValuePair(name, value));
        return this;
    }

    public HttpEntity build() {
        for (NameValuePair param : this.params) {
            this.multipartEntityBuilder.addTextBody(param.getName(), param.getValue(), ContentType.TEXT_PLAIN.withCharset(StandardCharsets.ISO_8859_1));
        }

        return this.multipartEntityBuilder.build();
    }
}
