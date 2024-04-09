package com.apocaly.apocaly_path_private.config;

// Spring Framework의 설정과 관련된 클래스를 불러옵니다.
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 이 클래스를 Spring의 설정 클래스로 선언합니다. 이 어노테이션을 사용함으로써,
// Spring은 이 클래스에 정의된 설정을 애플리케이션의 구성 요소로 자동으로 인식하게 됩니다.
@Configuration
// WebMvcConfigurer 인터페이스를 구현함으로써, Spring MVC의 CORS 설정을 커스터마이즈할 수 있습니다.
public class WebConfig implements WebMvcConfigurer {

    // WebMvcConfigurer 인터페이스에서 오버라이드한 메소드로,
    // CORS 관련 설정을 추가하는 데 사용됩니다.
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 모든 경로에 대해 CORS 설정을 추가합니다. (/**는 모든 경로를 의미합니다.)
        registry.addMapping("/**")
                // 오직 "http://localhost:3005" 이 오리진에서 오는 요청만을 허용합니다.
                // 개발 단계에서는 일반적으로 프론트엔드 서버의 주소가 됩니다.
                .allowedOrigins("http://localhost:3000")
                // 해당 오리진에서 허용할 HTTP 메소드를 지정합니다.
                // GET, POST, PUT, DELETE, HEAD, OPTIONS 메소드를 허용합니다.
                .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS")
                // 모든 HTTP 헤더를 요청에서 허용합니다.
                .allowedHeaders("*")
                // 쿠키나 인증과 관련된 정보를 포함한 요청을 허용합니다.
                .allowCredentials(true);
    }
}
