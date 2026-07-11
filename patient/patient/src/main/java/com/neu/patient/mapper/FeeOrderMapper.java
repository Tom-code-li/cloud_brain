package com.neu.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.patient.entity.FeeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FeeOrderMapper extends BaseMapper<FeeOrder> {
    @Select("""
            SELECT fo.*,
                   (
                     SELECT foi.item_type
                     FROM fee_order_item foi
                     WHERE foi.fee_order_id = fo.fee_order_id
                     ORDER BY foi.fee_order_item_id ASC
                     LIMIT 1
                   ) AS item_type
            FROM fee_order fo
            WHERE fo.patient_id = #{patientId}
            ORDER BY fo.created_at DESC
            """)
    @Results(id = "FeeOrderWithItemType", value = {
            @Result(property = "itemType", column = "item_type")
    })
    List<FeeOrder> findByPatientId(Long patientId);

    @Select("""
            SELECT fo.*,
                   (
                     SELECT foi.item_type
                     FROM fee_order_item foi
                     WHERE foi.fee_order_id = fo.fee_order_id
                     ORDER BY foi.fee_order_item_id ASC
                     LIMIT 1
                   ) AS item_type
            FROM fee_order fo
            WHERE fo.patient_id = #{patientId} AND fo.status = '待支付'
            ORDER BY fo.created_at DESC
            """)
    @Results(id = "FeeOrderWithItemTypeUnpaid", value = {
            @Result(property = "itemType", column = "item_type")
    })
    List<FeeOrder> findUnpaidByPatientId(Long patientId);
}
