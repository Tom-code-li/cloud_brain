package com.doctor.platform.examlab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.doctor.platform.examlab.dto.ExamResultFeatureResponse;
import com.doctor.platform.examlab.dto.ExamLabReportResponse;
import com.doctor.platform.examlab.dto.LabResultItemResponse;
import com.doctor.platform.examlab.dto.ReportReviewRequest;
import com.doctor.platform.examlab.entity.ExamLabOrder;
import com.doctor.platform.examlab.entity.ExamLabOrderItem;
import com.doctor.platform.examlab.entity.ExamResultFeature;
import com.doctor.platform.examlab.entity.ExamLabReport;
import com.doctor.platform.examlab.entity.LabResultItem;
import com.doctor.platform.examlab.mapper.ExamLabOrderItemMapper;
import com.doctor.platform.examlab.mapper.ExamLabOrderMapper;
import com.doctor.platform.examlab.mapper.ExamResultFeatureMapper;
import com.doctor.platform.examlab.mapper.ExamLabReportMapper;
import com.doctor.platform.examlab.mapper.LabResultItemMapper;
import com.doctor.platform.modules.outpatient.entity.OutpatientVisit;
import com.doctor.platform.modules.outpatient.mapper.OutpatientVisitMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamLabReportService {

    private final ExamLabReportMapper examLabReportMapper;
    private final ExamLabOrderMapper examLabOrderMapper;
    private final ExamLabOrderItemMapper examLabOrderItemMapper;
    private final ExamResultFeatureMapper examResultFeatureMapper;
    private final LabResultItemMapper labResultItemMapper;
    private final OutpatientVisitMapper outpatientVisitMapper;

    public ExamLabReportService(ExamLabReportMapper examLabReportMapper,
                                ExamLabOrderMapper examLabOrderMapper,
                                ExamLabOrderItemMapper examLabOrderItemMapper,
                                ExamResultFeatureMapper examResultFeatureMapper,
                                LabResultItemMapper labResultItemMapper,
                                OutpatientVisitMapper outpatientVisitMapper) {
        this.examLabReportMapper = examLabReportMapper;
        this.examLabOrderMapper = examLabOrderMapper;
        this.examLabOrderItemMapper = examLabOrderItemMapper;
        this.examResultFeatureMapper = examResultFeatureMapper;
        this.labResultItemMapper = labResultItemMapper;
        this.outpatientVisitMapper = outpatientVisitMapper;
    }

    public List<ExamLabReportResponse> listReports(Long patientId, Long visitId, Long orderId) {
        LambdaQueryWrapper<ExamLabReport> query = new LambdaQueryWrapper<ExamLabReport>()
            .orderByDesc(ExamLabReport::getPublishedAt)
            .orderByDesc(ExamLabReport::getCreatedAt)
            .last("limit 50");
        if (patientId != null) {
            query.eq(ExamLabReport::getPatientId, patientId);
        }
        if (orderId != null) {
            query.eq(ExamLabReport::getOrderId, orderId);
        }

        return examLabReportMapper.selectList(query).stream()
            .map(this::toResponse)
            .filter(response -> visitId == null || belongsToVisit(response, visitId))
            .toList();
    }

    private boolean belongsToVisit(ExamLabReportResponse response, Long visitId) {
        ExamLabOrder order = response.getOrderId() == null ? null : examLabOrderMapper.selectById(response.getOrderId());
        return order != null && visitId.equals(order.getVisitId());
    }

    private ExamLabReportResponse toResponse(ExamLabReport report) {
        ExamLabOrder order = report.getOrderId() == null ? null : examLabOrderMapper.selectById(report.getOrderId());
        ExamLabOrderItem item = report.getOrderItemId() == null ? null : examLabOrderItemMapper.selectById(report.getOrderItemId());
        List<ExamResultFeatureResponse> examFeatures = loadExamFeatures(report.getReportId(), report.getOrderItemId());
        List<LabResultItemResponse> labResultItems = loadLabResultItems(report.getReportId(), report.getOrderItemId());

        return ExamLabReportResponse.builder()
            .reportId(report.getReportId())
            .orderId(report.getOrderId())
            .orderItemId(report.getOrderItemId())
            .orderNo(order == null ? null : order.getOrderNo())
            .orderType(order == null ? null : order.getOrderType())
            .itemName(item == null ? null : item.getItemName())
            .itemType(item == null ? null : item.getItemType())
            .resultSummary(item == null ? null : item.getResultSummary())
            .reportNo(report.getReportNo())
            .reportType(report.getReportType())
            .findings(report.getFindings())
            .conclusion(report.getConclusion())
            .aiDraft(report.getAiDraft())
            .doctorReview(report.getDoctorReview())
            .status(report.getStatus())
            .publishedAt(report.getPublishedAt())
            .examFeatures(examFeatures)
            .labResultItems(labResultItems)
            .build();
    }

    private List<ExamResultFeatureResponse> loadExamFeatures(Long reportId, Long orderItemId) {
        return examResultFeatureMapper.selectList(
            new LambdaQueryWrapper<ExamResultFeature>()
                .eq(reportId != null, ExamResultFeature::getReportId, reportId)
                .eq(orderItemId != null, ExamResultFeature::getOrderItemId, orderItemId)
                .orderByAsc(ExamResultFeature::getSortOrder)
                .orderByAsc(ExamResultFeature::getFeatureId)
        ).stream().map(item -> ExamResultFeatureResponse.builder()
            .featureId(item.getFeatureId())
            .featureName(item.getFeatureName())
            .featureValue(item.getFeatureValue())
            .unit(item.getUnit())
            .abnormalFlag(item.getAbnormalFlag())
            .sortOrder(item.getSortOrder())
            .build()).toList();
    }

    private List<LabResultItemResponse> loadLabResultItems(Long reportId, Long orderItemId) {
        return labResultItemMapper.selectList(
            new LambdaQueryWrapper<LabResultItem>()
                .eq(reportId != null, LabResultItem::getReportId, reportId)
                .eq(orderItemId != null, LabResultItem::getOrderItemId, orderItemId)
                .orderByAsc(LabResultItem::getSortOrder)
                .orderByAsc(LabResultItem::getResultItemId)
        ).stream().map(item -> LabResultItemResponse.builder()
            .resultItemId(item.getResultItemId())
            .itemCode(item.getItemCode())
            .indicatorCode(item.getIndicatorCode())
            .indicatorName(item.getIndicatorName())
            .resultValue(item.getResultValue())
            .unit(item.getUnit())
            .referenceRange(item.getReferenceRange())
            .abnormalFlag(item.getAbnormalFlag())
            .sortOrder(item.getSortOrder())
            .build()).toList();
    }

    public Long markReportReviewed(ReportReviewRequest request) {
        ExamLabReport report = examLabReportMapper.selectById(request.getReportId());
        report.setStatus("已回阅");
        examLabReportMapper.updateById(report);

        OutpatientVisit visit = outpatientVisitMapper.selectById(request.getVisitId());
        if (visit != null && "报告待回阅".equals(visit.getStatus())) {
            visit.setStatus("待确诊");
            outpatientVisitMapper.updateById(visit);
        }
        return report.getReportId();
    }
}
