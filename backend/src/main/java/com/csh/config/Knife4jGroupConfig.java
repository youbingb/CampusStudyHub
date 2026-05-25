package com.csh.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 接口分组：让 /doc.html 顶部出现按业务模块切换的下拉。
 * 与 SwaggerConfig（统一 Bearer JWT 安全方案）解耦，可单独维护分组规则。
 */
@Configuration
public class Knife4jGroupConfig {

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("00-全部接口")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authUserApi() {
        return GroupedOpenApi.builder()
                .group("01-鉴权与用户")
                .pathsToMatch("/api/auth/**", "/api/users/**", "/api/admin/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi roomSeatApi() {
        return GroupedOpenApi.builder()
                .group("02-自习室与座位")
                .pathsToMatch("/api/rooms/**", "/api/seats/**", "/api/admin/rooms/**", "/api/admin/seats/**")
                .build();
    }

    @Bean
    public GroupedOpenApi reservationApi() {
        return GroupedOpenApi.builder()
                .group("03-预约与推荐")
                .pathsToMatch("/api/reservations/**", "/api/admin/reservations/**", "/api/recommend/**")
                .build();
    }

    @Bean
    public GroupedOpenApi notificationApi() {
        return GroupedOpenApi.builder()
                .group("04-站内通知")
                .pathsToMatch("/api/notifications/**")
                .build();
    }

    @Bean
    public GroupedOpenApi reportApi() {
        return GroupedOpenApi.builder()
                .group("05-举报与信誉")
                .pathsToMatch("/api/reports/**", "/api/admin/reports/**")
                .build();
    }

    @Bean
    public GroupedOpenApi inspectionAnnouncementRuleApi() {
        return GroupedOpenApi.builder()
                .group("06-巡检/公告/规则")
                .pathsToMatch(
                        "/api/admin/inspections/**",
                        "/api/announcements/**", "/api/admin/announcements/**",
                        "/api/rules/**", "/api/admin/rules/**")
                .build();
    }

    @Bean
    public GroupedOpenApi statsLogApi() {
        return GroupedOpenApi.builder()
                .group("07-统计与日志")
                .pathsToMatch("/api/admin/stats/**", "/api/admin/logs/**")
                .build();
    }
}
