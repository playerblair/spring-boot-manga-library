package dev.playerblair.manga_library.config;

import dev.playerblair.manga_library.client.JikanClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfig {

    @Bean
    JikanClient jikanClient(RestClient.Builder builder) {
        RestClient client = builder
                .baseUrl("https://api.jikan.moe")
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(client);

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();

        return factory.createClient(JikanClient.class);
    }
}
