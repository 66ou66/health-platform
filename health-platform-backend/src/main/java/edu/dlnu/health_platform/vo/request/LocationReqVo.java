package edu.dlnu.health_platform.vo.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 位置上报入参
 */
@Data
public class LocationReqVo {
    /** 经度 */
    private BigDecimal longitude;
    /** 纬度 */
    private BigDecimal latitude;
}
