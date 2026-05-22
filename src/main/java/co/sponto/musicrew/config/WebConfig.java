package co.sponto.musicrew.config;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import co.sponto.musicrew.user.LastSeenInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String uploadDir;
    private final LastSeenInterceptor lastSeenInterceptor;

    public WebConfig(@Value("${musicrew.upload.dir}") String uploadDir, LastSeenInterceptor lastSeenInterceptor) {
        this.uploadDir = uploadDir;
        this.lastSeenInterceptor = lastSeenInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + Paths.get(uploadDir).toAbsolutePath() + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(lastSeenInterceptor)
                .excludePathPatterns(
                        "/css/**",
                        "/js/**",
                        "/webjars/**",
                        "/images/**",
                        "/uploads/**",
                        "/h2-console/**",
                        "/error",
                        "/favicon.ico");
    }

}
