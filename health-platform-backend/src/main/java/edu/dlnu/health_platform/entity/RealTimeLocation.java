package edu.dlnu.health_platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 实时位置心跳表实体（real_time_locations）
 */
@Data
public class RealTimeLocation {
    private Long id;
    /** 用户ID */
    private Long userId;
    private BigDecimal longitude;
    private BigDecimal latitude;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
