package ragatanga.com.httpClient;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Base64;

public class HttpClient {

    private final CookieStore cookieStore;
    private final CloseableHttpClient client;

    public HttpClient() {
        this.cookieStore = new BasicCookieStore();

        RequestConfig requestConfig = RequestConfig.custom()
                .setExpectContinueEnabled(false)
                .setConnectionRequestTimeout(Timeout.ofSeconds(5))  // tempo máximo pra pegar conexão do pool
                .setResponseTimeout(Timeout.ofSeconds(30))          // tempo máximo pra receber resposta
                .setExpectContinueEnabled(false)
                .build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(20); // aumenta limite por host
        cm.setMaxTotal(100);

        this.client = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultCookieStore(this.cookieStore)
                .setDefaultRequestConfig(requestConfig)
                .disableAutomaticRetries()
                .build();
    }

    public HttpClient(String base64Cert, String certPassword) {
        this.cookieStore = new BasicCookieStore();

        SSLContext sslContext = createSSLContext(base64Cert, certPassword);

        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext)
                .setHostnameVerifier((hostname, session) -> true)
                .build();

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();

        connectionManager.setDefaultMaxPerRoute(20); // aumenta limite por host
        connectionManager.setMaxTotal(100);

        RequestConfig requestConfig = RequestConfig.custom()
                .setExpectContinueEnabled(false)
                .setConnectionRequestTimeout(Timeout.ofSeconds(5))  // tempo máximo pra pegar conexão do pool
                .setResponseTimeout(Timeout.ofSeconds(30))          // tempo máximo pra receber resposta
                .setExpectContinueEnabled(false)
                .build();

        this.client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .setDefaultCookieStore(this.cookieStore)
                .build();
    }

    private SSLContext createSSLContext(String base64Cert, String certPassword) {
        try {
            byte[] pfxBytes = Base64.getDecoder().decode(base64Cert);
            KeyStore keyStore = KeyStore.getInstance("PKCS12");

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(pfxBytes)) {
                keyStore.load(inputStream, certPassword.toCharArray());
            }

            return SSLContextBuilder.create()
                    .loadKeyMaterial(keyStore, certPassword.toCharArray())
                    .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao configurar SSL com certificado digital", e);
        }
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }

    public HttpResponse execute(HttpRequest request) {
        try {
            CloseableHttpResponse response = client.execute(request.build());
            return new HttpResponse(response, request.getUrl());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao executar requisição HTTP: " + e.getMessage(), e);
        }
    }

    public HttpResponse execute(ClassicHttpRequest request)  {
        try {
            CloseableHttpResponse response = client.execute(request);
            return new HttpResponse(response, "");
        } catch (IOException e) {
            throw new RuntimeException("Erro ao executar requisição HTTP: " + e.getMessage(), e);
        }

    }

}

