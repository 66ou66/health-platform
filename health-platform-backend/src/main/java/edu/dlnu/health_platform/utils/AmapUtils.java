package edu.dlnu.health_platform.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 高德地图 Web 服务 API 工具类
 * 用途：逆地理编码（经纬度 → 地址），用于 SOS 定位解析和志愿者签到/签退
 */
@Component
public class AmapUtils {

    /** 高德 Web 服务 Key（application.yml 中配置） */
    @Value("${amap.web-service-key}")
    private String key;

    /**
     * 逆地理编码：经纬度 → 格式化地址
     * @return 地址字符串，失败时返回 null
     */
    public String reverseGeocode(Double longitude, Double latitude) {
        if (longitude == null || latitude == null) {
            return null;
        }
        try {
            String url = "https://restapi.amap.com/v3/geocode/regeo?key=" + key
                    + "&location=" + longitude + "," + latitude;
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> result = restTemplate.getForObject(url, Map.class);
            if (result == null || !"1".equals(String.valueOf(result.get("status")))) {
                return null;
            }
            // regeocode.formatted_address 即为格式化地址
            Map<String, Object> regeocode = (Map<String, Object>) result.get("regeocode");
            if (regeocode == null) {
                return null;
            }
            Object formattedAddress = regeocode.get("formatted_address");
            return formattedAddress == null ? null : formattedAddress.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
