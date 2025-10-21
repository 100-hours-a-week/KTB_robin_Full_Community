package ktb3.fullstack.week4.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Server devServer = new Server();
        devServer.setUrl("/"); // API 서버 설정

        Info info = new Info()
                .title("Swagger API") // API 문서 제목
                .version("1.0.0") // API 문서 버전
                .description("Swagger API Description"); // API 문서 설명

        return new OpenAPI()
                .info(info)
                .addServersItem(devServer);
    }
}
