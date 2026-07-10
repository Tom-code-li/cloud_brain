package com.hospital.medicalexam.mapper;

import com.hospital.medicalexam.domain.view.ExamLabReportView;
import com.hospital.medicalexam.domain.view.ExamLabTaskView;
import com.hospital.medicalexam.domain.view.ExamLabWorkbenchItemView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface MedicalExamQueryMapper {
    @Select("""
            SELECT
              o.order_id AS orderId,
              i.order_item_id AS orderItemId,
              o.visit_id AS visitId,
              o.record_id AS recordId,
              o.patient_id AS patientId,
              p.patient_name AS patientName,
              p.gender AS gender,
              i.item_name AS itemName,
              i.item_type AS itemType,
              i.amount AS amount,
              o.fee_status AS feeStatus,
              o.status AS orderStatus,
              i.status AS itemStatus,
              o.clinical_diagnosis AS clinicalDiagnosis,
              o.purpose AS purpose,
              o.applied_at AS appliedAt,
              i.executed_at AS executedAt
            FROM exam_lab_order_item i
            JOIN exam_lab_order o ON o.order_id = i.order_id
            JOIN department d ON d.dept_id = o.execute_dept_id
            JOIN patient p ON p.patient_id = o.patient_id
            WHERE d.dept_type = #{doctorType}
              AND i.item_type = #{itemType}
              AND o.fee_status = '已支付'
              AND o.status <> '待缴费'
              AND o.status <> '已完成'
            ORDER BY o.applied_at DESC, i.order_item_id DESC
            """)
    List<ExamLabTaskView> findPendingTasks(
            @Param("doctorType") String doctorType,
            @Param("itemType") String itemType
    );

    @Select("""
            SELECT
              o.order_id AS orderId,
              i.order_item_id AS orderItemId,
              o.visit_id AS visitId,
              o.record_id AS recordId,
              o.patient_id AS patientId,
              p.patient_name AS patientName,
              p.gender AS gender,
              p.birthday AS birthday,
              u.real_name AS applyDoctorName,
              d.dept_name AS executeDeptName,
              i.item_name AS itemName,
              i.item_type AS itemType,
              i.amount AS amount,
              o.fee_status AS feeStatus,
              o.status AS orderStatus,
              i.status AS itemStatus,
              o.clinical_diagnosis AS clinicalDiagnosis,
              o.purpose AS purpose,
              o.applied_at AS appliedAt,
              i.executed_at AS executedAt,
              r.report_id AS reportId,
              r.status AS reportStatus,
              r.published_at AS publishedAt,
              CASE
                WHEN o.status = '已完成' THEN 'published'
                WHEN o.status = '执行中' THEN 'progress'
                ELSE 'pending'
              END AS workbenchStatus
            FROM exam_lab_order_item i
            JOIN exam_lab_order o ON o.order_id = i.order_id
            JOIN patient p ON p.patient_id = o.patient_id
            LEFT JOIN doctor ad ON ad.doctor_id = o.apply_doctor_id
            LEFT JOIN sys_user u ON u.user_id = ad.user_id
            LEFT JOIN department d ON d.dept_id = o.execute_dept_id
            LEFT JOIN exam_lab_report r ON r.report_id = (
              SELECT rr.report_id
              FROM exam_lab_report rr
              WHERE rr.order_item_id = i.order_item_id
              ORDER BY rr.created_at DESC, rr.report_id DESC
              LIMIT 1
            )
            WHERE d.dept_type = #{doctorType}
              AND i.item_type = #{itemType}
              AND o.fee_status = '已支付'
              AND o.status <> '待缴费'
              AND (#{keyword} IS NULL OR #{keyword} = '' OR p.patient_name LIKE CONCAT('%', #{keyword}, '%'))
              AND (#{itemName} IS NULL OR #{itemName} = '' OR i.item_name LIKE CONCAT('%', #{itemName}, '%'))
            ORDER BY
              CASE
                WHEN o.status = '已完成' THEN 3
                WHEN o.status = '执行中' THEN 2
                ELSE 1
              END,
              o.applied_at DESC,
              i.order_item_id DESC
            """)
    List<ExamLabWorkbenchItemView> findWorkbenchItems(
            @Param("doctorType") String doctorType,
            @Param("itemType") String itemType,
            @Param("keyword") String keyword,
            @Param("itemName") String itemName
    );

    @Select("""
            SELECT
              r.report_id AS reportId,
              r.order_id AS orderId,
              r.order_item_id AS orderItemId,
              o.record_id AS recordId,
              r.patient_id AS patientId,
              p.patient_name AS patientName,
              i.item_name AS itemName,
              r.report_type AS reportType,
              r.findings AS findings,
              r.conclusion AS conclusion,
              r.ai_draft AS aiDraft,
              r.doctor_review AS doctorReview,
              r.status AS status,
              r.created_at AS createdAt,
              r.published_at AS publishedAt
            FROM exam_lab_report r
            JOIN exam_lab_order o ON o.order_id = r.order_id
            LEFT JOIN exam_lab_order_item i ON i.order_item_id = r.order_item_id
            JOIN patient p ON p.patient_id = r.patient_id
            WHERE r.report_id = #{reportId}
            """)
    ExamLabReportView findReportDetail(@Param("reportId") Long reportId);

    @Select("""
            SELECT
              r.report_id AS reportId,
              r.order_id AS orderId,
              r.order_item_id AS orderItemId,
              o.record_id AS recordId,
              r.patient_id AS patientId,
              p.patient_name AS patientName,
              i.item_name AS itemName,
              r.report_type AS reportType,
              r.findings AS findings,
              r.conclusion AS conclusion,
              r.ai_draft AS aiDraft,
              r.doctor_review AS doctorReview,
              r.status AS status,
              r.created_at AS createdAt,
              r.published_at AS publishedAt
            FROM exam_lab_report r
            JOIN exam_lab_order o ON o.order_id = r.order_id
            LEFT JOIN exam_lab_order_item i ON i.order_item_id = r.order_item_id
            JOIN patient p ON p.patient_id = r.patient_id
            WHERE o.record_id = #{recordId}
            ORDER BY r.created_at DESC, r.report_id DESC
            """)
    List<ExamLabReportView> findReportsByRecord(@Param("recordId") Long recordId);

    @Select("""
            SELECT
              o.order_id AS orderId,
              i.order_item_id AS orderItemId,
              o.record_id AS recordId,
              o.patient_id AS patientId,
              p.patient_name AS patientName,
              p.gender AS gender,
              p.birthday AS birthday,
              ad.user_id AS applyDoctorUserId,
              u.real_name AS applyDoctorName,
              d.dept_name AS executeDeptName,
              i.item_id AS itemId,
              i.item_name AS itemName,
              i.item_type AS itemType,
              i.amount AS amount,
              i.result_summary AS resultSummary,
              o.fee_status AS feeStatus,
              o.status AS orderStatus,
              i.status AS itemStatus,
              o.clinical_diagnosis AS clinicalDiagnosis,
              o.purpose AS purpose,
              o.applied_at AS appliedAt,
              i.executed_at AS executedAt,
              r.report_id AS reportId,
              r.report_no AS reportNo,
              r.status AS reportStatus,
              r.findings AS findings,
              r.conclusion AS conclusion,
              r.ai_draft AS aiDraft,
              r.doctor_review AS doctorReview,
              r.published_at AS publishedAt
            FROM exam_lab_order_item i
            JOIN exam_lab_order o ON o.order_id = i.order_id
            JOIN patient p ON p.patient_id = o.patient_id
            LEFT JOIN doctor ad ON ad.doctor_id = o.apply_doctor_id
            LEFT JOIN sys_user u ON u.user_id = ad.user_id
            LEFT JOIN department d ON d.dept_id = o.execute_dept_id
            LEFT JOIN exam_lab_report r ON r.report_id = (
              SELECT rr.report_id
              FROM exam_lab_report rr
              WHERE rr.order_item_id = i.order_item_id
              ORDER BY rr.created_at DESC, rr.report_id DESC
              LIMIT 1
            )
            WHERE i.order_item_id = #{orderItemId}
            """)
    Map<String, Object> findOrderItemDetail(@Param("orderItemId") Long orderItemId);

    @Select("""
            SELECT feature_name AS featureName, feature_value AS featureValue,
                   unit, abnormal_flag AS abnormalFlag, sort_order AS sortOrder
            FROM exam_result_feature
            WHERE order_item_id = #{orderItemId}
            ORDER BY sort_order
            """)
    List<Map<String, Object>> findResultFeatures(@Param("orderItemId") Long orderItemId);

    @Select("""
            SELECT indicator_code AS indicatorCode, indicator_name AS indicatorName,
                   result_value AS resultValue, unit, reference_range AS referenceRange,
                   abnormal_flag AS abnormalFlag, sort_order AS sortOrder
            FROM lab_result_item
            WHERE order_item_id = #{orderItemId}
            ORDER BY sort_order
            """)
    List<Map<String, Object>> findLabResultItems(@Param("orderItemId") Long orderItemId);
}
