package edu.dlnu.health_platform.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务字典表实体（service_categories）
 */
@Data
public class Category {
    private Long id;
    /** 分类名称 */
    private String categoryName;
    /** 图标（emoji/图片地址） */
    private String icon;
    /** 排序 */
    private Integer sort;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
