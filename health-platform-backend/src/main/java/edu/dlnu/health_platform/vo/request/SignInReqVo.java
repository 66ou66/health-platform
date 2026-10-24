package edu.dlnu.health_platform.vo.request;

import lombok.Data;

/**
 * 签到/签退入参（志愿者）
 * 前端上报经纬度，后端通过高德逆地理编码解析地址
 */
@Data
public class SignInReqVo {
    private Double latitude;
    private Double longitude;
    /** 位置说明（选填，为空时由后端逆地理编码生成） */
    private String address;
}
