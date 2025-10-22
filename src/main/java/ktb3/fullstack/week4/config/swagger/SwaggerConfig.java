package ktb3.fullstack.week4.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import ktb3.fullstack.week4.config.swagger.annotation.AccessTokenExpireResponse;
import ktb3.fullstack.week4.config.swagger.annotation.CommonErrorResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class SwaggerConfig {

    // OpenAPI: 최종 OpenAPI 스펙의 루트 객체(정보/보안/서버/컴포넌트 등을 담음).
    @Bean
    public OpenAPI openAPI() {

        // Server: 문서에서 사용할 서버(베이스 URL) 정의.
        Server devServer = new Server();
        devServer.setUrl("/"); // API 서버 설정

        // Info: API 문서의 제목, 버전, 설명 등 메타데이터.
        Info info = new Info()
                .title("Swagger API")
                .version("1.0.0")
                .description("Swagger API Description");

        String jwtSchemeName = "jwtAuth";
        // SecurityRequirement: 각 Operation에 적용할 보안 스키마 요구사항 지정.
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        // Components: 재사용 가능한 스키마/보안 스키마/응답/파라미터 등을 모아두는 영역.
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP) // SecurityScheme: 인증 스키마 정의(JWT Bearer 등).
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components)
                .addServersItem(devServer);
    }
    /**
     * @CommonErrorResponses 가 붙은 클래스/메소드에 한해,
     * 400/500 응답을 추가(없으면 생성)하고 examples를 병합합니다.
     */
    @Bean
    public OperationCustomizer addCommonErrorResponses() {
        // OperationCustomizer: 스캔된 각 엔드포인트(Operation)을 후처리해 응답/파라미터 등을 동적으로 수정할 수 있음.

        return (operation, handlerMethod) -> {

            if (!hasCommonErrorAnnotation(handlerMethod)) {
                return operation;
            }

            // Example: Swagger 예시 객체(summary/description/value).
            Example invalidReq = new Example()
                    .summary("Invalid Request")
                    .value(new ktb3.fullstack.week4.dto.common.ApiResponse<>("invalid_request", "null"));
            Example internalErr = new Example()
                    .summary("Internal Server Error")
                    .value(new ktb3.fullstack.week4.dto.common.ApiResponse<>("internal_server_error", null));

            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }

            // ApiResponses : 한 Operation의 응답 목록 컨테이너(코드별 ApiResponse를 담음).
            // ApiResponse(io.swagger.v3.oas.models.responses.ApiResponse): 특정 HTTP 상태 코드의 응답(설명/콘텐츠/예시 등).
            // 400 upsert + 예시 병합
            ApiResponse bad = responses.get("400");
            if (bad == null) {
                bad = new ApiResponse().description("Invalid Request");
                bad.setContent(new Content());
                responses.addApiResponse("400", bad);
            }

            // Content: 미디어 타입별 응답 콘텐츠 컨테이너.
            Content badContent = ensureContent(bad);
            // MediaType: 특정 미디어타입(application/json 등)에 대한 스키마/예시 등.
            MediaType badJson = ensureJson(badContent);
            ensureExamples(badJson).putIfAbsent("invalid_request", invalidReq);
            if (badJson.getExample() instanceof String) {
                badJson.setExample(new ktb3.fullstack.week4.dto.common.ApiResponse<>("invalid_request", null));
            }

            // 500 upsert + 예시 병합
            ApiResponse err = responses.get("500");
            if (err == null) {
                err = new ApiResponse().description("Internal Server Error");
                err.setContent(new Content());
                responses.addApiResponse("500", err);
            }

            Content errContent = ensureContent(err);
            MediaType errJson = ensureJson(errContent);
            ensureExamples(errJson).putIfAbsent("internal_server_error", internalErr);
            if (errJson.getExample() instanceof String) {
                errJson.setExample(new ktb3.fullstack.week4.dto.common.ApiResponse<>("internal_server_error", null));
            }

            return operation;
        };
    }

    /**
     * @AccessTokenExpireResponse 가 붙은 클래스/메소드에 한해,
     * 401 응답을 추가(없으면 생성)하고 examples를 병합합니다.
     */
    @Bean
    public OperationCustomizer addAccessTokenExpiredExample() {

        return (operation, handlerMethod) -> {
            if (!hasAccessTokenExpireAnnotation(handlerMethod)) {
                return operation;
            }

            Example accessTokenExpired = new Example()
                    .summary("Access token expired")
                    .description("액세스 토큰이 만료되었습니다.")
                    .value(new ktb3.fullstack.week4.dto.common.ApiResponse<>(null, "access_token_expired"));

            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }

            // 401 upsert + 예시 병합
            ApiResponse unauthorized = responses.get("401");
            if (unauthorized == null) {
                unauthorized = new ApiResponse().description("Unauthorized");
                unauthorized.setContent(new Content());
                responses.addApiResponse("401", unauthorized);
            }
            Content unauthorizedContent = ensureContent(unauthorized);
            MediaType unauthorizedJson = ensureJson(unauthorizedContent);
            ensureExamples(unauthorizedJson).putIfAbsent("access_token_expired", accessTokenExpired);
            if (unauthorizedJson.getExample() instanceof String) {
                unauthorizedJson.setExample(new ktb3.fullstack.week4.dto.common.ApiResponse<>(null, "access_token_expired"));
            }

            return operation;
        };
    }


    // HandlerMethod: 현재 문서화 중인 스프링 MVC 핸들러 메소드 정보(리플렉션으로 어노테이션 조회 가능).
    private boolean hasCommonErrorAnnotation(HandlerMethod handlerMethod) {
        return handlerMethod.hasMethodAnnotation(CommonErrorResponses.class)
                || handlerMethod.getBeanType().isAnnotationPresent(CommonErrorResponses.class);
    }

    private boolean hasAccessTokenExpireAnnotation(HandlerMethod handlerMethod) {
        return handlerMethod.hasMethodAnnotation(AccessTokenExpireResponse.class)
                || handlerMethod.getBeanType().isAnnotationPresent(AccessTokenExpireResponse.class);
    }


    private Content ensureContent(ApiResponse response) {
        Content content = response.getContent();
        if (content == null) {
            content = new Content();
            response.setContent(content);
        }
        return content;
    }

    private MediaType ensureJson(Content content) {
        MediaType json = content.get("application/json");
        if (json == null) {
            json = new MediaType();
            content.addMediaType("application/json", json);
        }
        return json;
    }

    private Map<String, Example> ensureExamples(MediaType mediaType) {
        Map<String, Example> examples = mediaType.getExamples();
        if (examples == null) {
            examples = new LinkedHashMap<>();
            mediaType.setExamples(examples);
        }
        return examples;
    }
}