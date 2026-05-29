package com.csh.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** 同时接受 "yyyy-MM-dd HH:mm[:ss]" 和 "yyyy-MM-ddTHH:mm[:ss]"。 */
    private static final DateTimeFormatter DATE_TIME_FLEX = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart().appendLiteral('T').optionalEnd()
            .optionalStart().appendLiteral(' ').optionalEnd()
            .appendPattern("HH:mm")
            .optionalStart().appendPattern(":ss").optionalEnd()
            .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true).optionalEnd()
            .toFormatter();

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            SimpleModule m = new SimpleModule();
            m.addSerializer(LocalDateTime.class, new JsonSerializer<>() {
                @Override
                public void serialize(LocalDateTime v, JsonGenerator g, SerializerProvider p) throws IOException {
                    g.writeString(v.format(DATE_TIME_FMT));
                }
            });
            m.addDeserializer(LocalDateTime.class, new JsonDeserializer<>() {
                @Override
                public LocalDateTime deserialize(JsonParser p, DeserializationContext c) throws IOException {
                    String s = p.getText();
                    return (s == null || s.isBlank()) ? null : LocalDateTime.parse(s.trim(), DATE_TIME_FLEX);
                }
            });
            m.addSerializer(LocalDate.class, new JsonSerializer<>() {
                @Override
                public void serialize(LocalDate v, JsonGenerator g, SerializerProvider p) throws IOException {
                    g.writeString(v.format(DATE_FMT));
                }
            });
            m.addDeserializer(LocalDate.class, new JsonDeserializer<>() {
                @Override
                public LocalDate deserialize(JsonParser p, DeserializationContext c) throws IOException {
                    String s = p.getText();
                    return (s == null || s.isBlank()) ? null : LocalDate.parse(s.trim(), DATE_FMT);
                }
            });
            m.addSerializer(LocalTime.class, new JsonSerializer<>() {
                @Override
                public void serialize(LocalTime v, JsonGenerator g, SerializerProvider p) throws IOException {
                    g.writeString(v.format(TIME_FMT));
                }
            });
            m.addDeserializer(LocalTime.class, new JsonDeserializer<>() {
                @Override
                public LocalTime deserialize(JsonParser p, DeserializationContext c) throws IOException {
                    String s = p.getText();
                    return (s == null || s.isBlank()) ? null : LocalTime.parse(s.trim(), TIME_FMT);
                }
            });
            builder.modulesToInstall(m);
        };
    }
}
