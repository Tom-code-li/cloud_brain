package com.cloudBrain.pharmacy.dto;

import lombok.Data;

/**
 * 统计卡片数据传输对象
 */
@Data
public class DispenseStatsDTO {
    private Integer pending;
    private Integer today;
    private Integer lowStock;
    private Integer total;
}