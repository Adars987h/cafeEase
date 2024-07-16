package com.inn.cafe.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inn.cafe.POJO.User;
import com.inn.cafe.utils.CustomLocalDateTimeDeserializer;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSearchRequest {

    private Integer orderId;

    @Builder.Default
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime startTime = LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.MIN);

    @Builder.Default
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime endTime = LocalDateTime.now();

    private User customer;

    public LocalDateTime getStartTime(){
        return Objects.isNull(startTime) ? LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.MIN) : startTime;
    }

    public LocalDateTime getEndTime(){
        return Objects.isNull(endTime) ? LocalDateTime.now() : endTime;
    }

}
