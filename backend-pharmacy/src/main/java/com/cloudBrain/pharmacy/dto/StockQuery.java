package com.cloudBrain.pharmacy.dto;

import lombok.Data;

@Data
public class StockQuery {
    private String keyword;
    private String status;
}
