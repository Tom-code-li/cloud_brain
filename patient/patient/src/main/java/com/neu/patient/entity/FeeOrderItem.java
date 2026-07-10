package com.neu.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("fee_order_item")
public class FeeOrderItem {
    @TableId(value = "fee_order_item_id", type = IdType.AUTO)
    private Long feeOrderItemId;
    private Long feeOrderId;
    private String itemType;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private String itemSpec;
    private BigDecimal unitPrice;
    private BigDecimal quantity;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;

    public Long getFeeOrderItemId() { return feeOrderItemId; }
    public void setFeeOrderItemId(Long feeOrderItemId) { this.feeOrderItemId = feeOrderItemId; }
    public Long getFeeOrderId() { return feeOrderId; }
    public void setFeeOrderId(Long feeOrderId) { this.feeOrderId = feeOrderId; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getItemSpec() { return itemSpec; }
    public void setItemSpec(String itemSpec) { this.itemSpec = itemSpec; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
