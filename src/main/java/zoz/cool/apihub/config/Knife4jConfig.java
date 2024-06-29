package zoz.cool.apihub.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {
    private static final String url = "https://zoz.cool";
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ApiHub 在线文档")
                        .description("ApiHub - 后端Api接口文档")
                        .version("v1.0.0")
                        .description("ApiHub api Documentation")
                        .contact(new Contact()
                                .name("zoz").url(url)
                                .email("1729465178@qq.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url(url)))
                .externalDocs(new ExternalDocumentation()
                        .url(url));
    }
}