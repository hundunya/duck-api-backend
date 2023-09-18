package cn.hdy.sdk.client.project;

import cn.hdy.sdk.client.project.client.ApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author 混沌鸭
 **/
@Configuration
@Data
@ConfigurationProperties("duck.api")
@ComponentScan
public class DuckApiClientConfig {
    private String accessKey;
    private String secretKey;

    @Bean
    public ApiClient apiClient(){
        ApiClient client = new ApiClient();
        client.setAccessKey(accessKey);
        client.setSecretKey(secretKey);
        return client;
    }
}
