package ma.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;


@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.data.elasticsearch.client.elasticsearch-rest.uris}")
    private String elasticsearchUri;

    @Value("${spring.data.elasticsearch.username}")
    private String username;

    @Value("${spring.data.elasticsearch.password}")
    private String password;


    @Override
    public ClientConfiguration clientConfiguration() {
        SSLContext sslContext = createTrustAllSSLContext();

        return ClientConfiguration.builder()
                // enlève le préfixe https:// pour connectedTo()
                .connectedTo(elasticsearchUri.replace("https://", "").replace("http://", ""))
                .usingSsl(sslContext) // ✅ plus propre et sans builder custom
                .withBasicAuth(username, password)
                .withConnectTimeout(5000)
                .withSocketTimeout(30000)
                .build();

    }

    private SSLContext createTrustAllSSLContext() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) { }
                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) { }
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la configuration SSL pour Elasticsearch", e);
        }
    }
}
