package ru.practicum.explorewithme.stats.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EndpointHitDto {
    @NotBlank(message = "Идентификатор сервиса не может быть пустым")
    private String app;

    @NotBlank(message = "URI не может быть пустым")
    private String uri;

    @NotBlank(message = "IP-адрес не может быть пустым")
    private String ip;

    @NotNull(message = "Дата и время не могут быть пустыми")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
