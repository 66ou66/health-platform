package edu.dlnu.health_platform.vo.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * SOS 求助入参（经度/纬度必填，地址由后端逆地理编码解析）
 */
@Data
public class SosReqVo {
    /** 经度 */
    private BigDecimal longitude;
    /** 纬度 */
    private BigDecimal latitude;
    /** 地址（前端传了就用，否则后端解析） */
    private String address;
    /** 情况备注 */
    private String remark;
}
