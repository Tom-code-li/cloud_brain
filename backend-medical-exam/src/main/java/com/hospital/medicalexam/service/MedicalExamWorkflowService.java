package com.hospital.medicalexam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hospital.medicalexam.ai.AiDraftRequest;
import com.hospital.medicalexam.ai.AiDraftService;
import com.hospital.medicalexam.common.BusinessException;
import com.hospital.medicalexam.domain.dto.ExamResultSaveRequest;
import com.hospital.medicalexam.domain.dto.LabResultSaveRequest;
import com.hospital.medicalexam.domain.dto.ReportDraftRequest;
import com.hospital.medicalexam.domain.dto.ReportPublishRequest;
import com.hospital.medicalexam.domain.dto.ReportRejectRequest;
import com.hospital.medicalexam.domain.dto.SampleConfirmRequest;
import com.hospital.medicalexam.domain.view.ExamLabReportView;
import com.hospital.medicalexam.domain.view.ExamLabTaskView;
import com.hospital.medicalexam.domain.view.ExamLabWorkbenchItemView;
import com.hospital.medicalexam.domain.view.ExamLabWorkbenchResponse;
import com.hospital.medicalexam.domain.view.ExamLabWorkbenchStatsView;
import com.hospital.medicalexam.domain.view.ItemFieldDefView;
import com.hospital.medicalexam.domain.view.ItemSchemaView;
import com.hospital.medicalexam.domain.view.OrderDetailView;
import com.hospital.medicalexam.entity.DepartmentEntity;
import com.hospital.medicalexam.entity.DoctorEntity;
import com.hospital.medicalexam.entity.ExamLabOrderEntity;
import com.hospital.medicalexam.entity.ExamLabOrderItemEntity;
import com.hospital.medicalexam.entity.ExamLabReportEntity;
import com.hospital.medicalexam.entity.ExamResultFeatureEntity;
import com.hospital.medicalexam.entity.LabResultItemEntity;
import com.hospital.medicalexam.entity.MedicalItemEntity;
import com.hospital.medicalexam.entity.OutpatientVisitEntity;
import com.hospital.medicalexam.mapper.DepartmentMapper;
import com.hospital.medicalexam.mapper.DoctorMapper;
import com.hospital.medicalexam.mapper.ExamLabOrderItemMapper;
import com.hospital.medicalexam.mapper.ExamLabOrderMapper;
import com.hospital.medicalexam.mapper.ExamLabReportMapper;
import com.hospital.medicalexam.mapper.ExamResultFeatureMapper;
import com.hospital.medicalexam.mapper.LabResultItemMapper;
import com.hospital.medicalexam.mapper.MedicalExamQueryMapper;
import com.hospital.medicalexam.mapper.MedicalItemMapper;
import com.hospital.medicalexam.mapper.OutpatientVisitMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MedicalExamWorkflowService {
    private static final String DOCTOR_TYPE_EXAM = "EXAM";
    private static final String DOCTOR_TYPE_LAB = "LAB";
    private static final String ITEM_TYPE_EXAM = "检查";
    private static final String ITEM_TYPE_LAB = "检验";
    private static final String PAID = "已支付";
    private static final String ORDER_PENDING_PAYMENT = "待缴费";
    private static final String ORDER_PENDING_EXECUTION = "待执行";
    private static final String ORDER_IN_PROGRESS = "执行中";
    private static final String EXECUTED = "已执行";
    private static final String COMPLETED = "已完成";
    private static final String DRAFT = "草稿";
    private static final String PUBLISHED = "已发布";
    private static final String REVIEWED = "已回阅";
    private static final String VISIT_STATUS_WAIT_EXAM_LAB = "待检查检验";
    private static final String VISIT_STATUS_EXAM_LAB_IN_PROGRESS = "检查检验中";
    private static final String VISIT_STATUS_REPORT_WAIT_REVIEW = "报告待回阅";
    private static final String WORKBENCH_ALL = "all";
    private static final String WORKBENCH_PENDING = "pending";
    private static final String WORKBENCH_PROGRESS = "progress";
    private static final String WORKBENCH_PUBLISHED = "published";
    private static final String DRAFT_STATUS = "草稿";
    private static final String NORMAL_FLAG = "NORMAL";
    private static final String ABNORMAL_FLAG = "ABNORMAL";
    private static final String HIGH_FLAG = "HIGH";
    private static final String LOW_FLAG = "LOW";

    /* ---------- 检查项目结构定义（心电图 / 胸部CT） ---------- */
    private static final Map<String, ItemSchemaDef> EXAM_SCHEMAS = new LinkedHashMap<>();

    /* ---------- 检验项目结构定义（参考范围） ---------- */
    private static final Map<String, List<IndicatorDef>> LAB_PANELS = new LinkedHashMap<>();

    static {
        EXAM_SCHEMAS.put("心电图", new ItemSchemaDef("ecg", List.of(
                new IndicatorDef("hr", "心率", "次/分", "60", "100", "96"),
                new IndicatorDef("pr", "PR 间期", "ms", "120", "200", "168"),
                new IndicatorDef("qrs", "QRS 时限", "ms", "80", "120", "92"),
                new IndicatorDef("qtc", "QTc", "ms", "350", "450", "432")
        ), null, List.of("窦性心律", "窦性心动过速", "窦性心动过缓", "心房颤动", "室性早搏"),
                List.of("正常电轴", "电轴左偏", "电轴右偏")));
        EXAM_SCHEMAS.put("胸部CT", new ItemSchemaDef("ct", null,
                List.of("双肺纹理清晰", "右肺上叶结节", "左肺下叶斑片渗出影", "胸腔积液", "纵隔淋巴结肿大", "心影增大"),
                null, null));

        LAB_PANELS.put("血常规", List.of(
                new IndicatorDef("WBC", "白细胞计数", "×10⁹/L", "4", "10", "6.2"),
                new IndicatorDef("RBC", "红细胞计数", "×10¹²/L", "3.5", "5.5", "4.6"),
                new IndicatorDef("HGB", "血红蛋白", "g/L", "110", "160", "138"),
                new IndicatorDef("PLT", "血小板计数", "×10⁹/L", "100", "300", "215"),
                new IndicatorDef("NEUT", "中性粒细胞百分比", "%", "40", "75", "58")
        ));
        LAB_PANELS.put("尿常规", List.of(
                new IndicatorDef("PRO", "尿蛋白", null, null, null, "阴性", "enum", List.of("阴性", "弱阳性", "1+", "2+", "3+"), "阴性"),
                new IndicatorDef("GLU_U", "尿糖", null, null, null, "阴性", "enum", List.of("阴性", "弱阳性", "1+", "2+", "3+"), "阴性"),
                new IndicatorDef("BLD", "尿潜血", null, null, null, "阴性", "enum", List.of("阴性", "弱阳性", "1+", "2+", "3+"), "阴性"),
                new IndicatorDef("PH", "pH 值", "", "4.5", "8.0", "6.2"),
                new IndicatorDef("SG", "尿比重", "", "1.003", "1.030", "1.018")
        ));
        LAB_PANELS.put("C反应蛋白", List.of(
                new IndicatorDef("CRP", "C反应蛋白(CRP)", "mg/L", "0", "10", "6.8")
        ));
        LAB_PANELS.put("肝功能", List.of(
                new IndicatorDef("ALT", "谷丙转氨酶(ALT)", "U/L", "7", "40", "32"),
                new IndicatorDef("AST", "谷草转氨酶(AST)", "U/L", "13", "35", "28"),
                new IndicatorDef("TBIL", "总胆红素", "μmol/L", "3.4", "20.4", "15.1"),
                new IndicatorDef("ALB", "白蛋白", "g/L", "40", "55", "44")
        ));
        LAB_PANELS.put("肾功能", List.of(
                new IndicatorDef("CR", "血肌酐", "μmol/L", "41", "111", "96"),
                new IndicatorDef("BUN", "尿素氮", "mmol/L", "2.9", "8.2", "6.1"),
                new IndicatorDef("UA", "尿酸", "μmol/L", "150", "420", "358")
        ));
        LAB_PANELS.put("血糖", List.of(
                new IndicatorDef("FBG", "空腹血糖", "mmol/L", "3.9", "6.1", "5.4")
        ));
    }

    private record ItemSchemaDef(String type, List<IndicatorDef> vitals, List<String> findingOptions,
                                  List<String> rhythmOptions, List<String> axisOptions) {
    }

    private record IndicatorDef(String key, String label, String unit, String low, String high, String def,
                                 String valueType, List<String> options, String normal) {
        IndicatorDef(String key, String label, String unit, String low, String high, String def) {
            this(key, label, unit, low, high, def, "numeric", null, null);
        }
        IndicatorDef(String key, String label, String unit, String low, String high, String def,
                      String valueType, List<String> options, String normal) {
            this.key = key;
            this.label = label;
            this.unit = unit;
            this.low = low;
            this.high = high;
            this.def = def;
            this.valueType = valueType;
            this.options = options;
            this.normal = normal;
        }
    }

    private final MedicalExamQueryMapper queryMapper;
    private final ExamLabOrderMapper orderMapper;
    private final ExamLabOrderItemMapper orderItemMapper;
    private final ExamLabReportMapper reportMapper;
    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;
    private final AiDraftService aiDraftService;
    private final ReportNoGenerator reportNoGenerator;
    private final ExamResultFeatureMapper examResultFeatureMapper;
    private final LabResultItemMapper labResultItemMapper;
    private final MedicalItemMapper medicalItemMapper;
    private final OutpatientVisitMapper outpatientVisitMapper;

    public MedicalExamWorkflowService(
            MedicalExamQueryMapper queryMapper,
            ExamLabOrderMapper orderMapper,
            ExamLabOrderItemMapper orderItemMapper,
            ExamLabReportMapper reportMapper,
            DoctorMapper doctorMapper,
            DepartmentMapper departmentMapper,
            AiDraftService aiDraftService,
            ReportNoGenerator reportNoGenerator,
            ExamResultFeatureMapper examResultFeatureMapper,
            LabResultItemMapper labResultItemMapper,
            MedicalItemMapper medicalItemMapper,
            OutpatientVisitMapper outpatientVisitMapper
    ) {
        this.queryMapper = queryMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.reportMapper = reportMapper;
        this.doctorMapper = doctorMapper;
        this.departmentMapper = departmentMapper;
        this.aiDraftService = aiDraftService;
        this.reportNoGenerator = reportNoGenerator;
        this.examResultFeatureMapper = examResultFeatureMapper;
        this.labResultItemMapper = labResultItemMapper;
        this.medicalItemMapper = medicalItemMapper;
        this.outpatientVisitMapper = outpatientVisitMapper;
    }

    public List<ExamLabTaskView> pending(Long deptId, String doctorType) {
        String normalizedDoctorType = normalizeDoctorType(doctorType);
        List<ExamLabTaskView> tasks = queryMapper.findPendingTasks(normalizedDoctorType, itemTypeOf(normalizedDoctorType));
        tasks.stream()
                .map(ExamLabTaskView::visitId)
                .distinct()
                .forEach(this::syncVisitStatus);
        return tasks;
    }

    public ExamLabWorkbenchResponse workbench(Long deptId, String doctorType, String status, String keyword) {
        return workbench(deptId, doctorType, status, keyword, null);
    }

    public ExamLabWorkbenchResponse workbench(Long deptId, String doctorType, String status, String keyword, String itemName) {
        String normalizedDoctorType = normalizeDoctorType(doctorType);
        String normalizedStatus = normalizeWorkbenchStatus(status);
        String normalizedItemName = normalizeKeyword(itemName);
        boolean itemFilterNeedsAliasMatch = isChestProjectionName(normalizedItemName);
        List<ExamLabWorkbenchItemView> allItems = queryMapper.findWorkbenchItems(
                normalizedDoctorType,
                itemTypeOf(normalizedDoctorType),
                normalizeKeyword(keyword),
                itemFilterNeedsAliasMatch ? null : normalizedItemName
        );
        if (itemFilterNeedsAliasMatch) {
            allItems = allItems.stream()
                    .filter(item -> isChestProjectionName(item.itemName()))
                    .toList();
        }
        allItems.stream()
                .map(ExamLabWorkbenchItemView::visitId)
                .distinct()
                .forEach(this::syncVisitStatus);
        ExamLabWorkbenchStatsView stats = buildWorkbenchStats(allItems);
        List<ExamLabWorkbenchItemView> items = allItems.stream()
                .filter(item -> WORKBENCH_ALL.equals(normalizedStatus)
                        || normalizedStatus.equals(item.workbenchStatus()))
                .toList();
        return new ExamLabWorkbenchResponse(stats, items);
    }

    @Transactional
    public ExamLabTaskView executeOrder(Long orderItemId, Long doctorId, Long deptId, String doctorType) {
        DoctorEntity doctor = validateDoctor(doctorId, doctorType);
        ExamLabOrderItemEntity item = requireOrderItem(orderItemId);
        ExamLabOrderEntity order = requireOrder(item.getOrderId());
        validateTaskBelongsToRole(order, item, doctorType);
        if (!PAID.equals(order.getFeeStatus()) || ORDER_PENDING_PAYMENT.equals(order.getStatus())) {
            throw new BusinessException(itemTypeOf(doctorType) + "项目未缴费，暂不可执行");
        }
        if (COMPLETED.equals(order.getStatus())) {
            throw new BusinessException("检查/检验项目已完成，不可重复执行");
        }
        if (ORDER_IN_PROGRESS.equals(order.getStatus())) {
            throw new BusinessException("检查/检验项目已执行，不可重复执行");
        }
        if (!ORDER_PENDING_EXECUTION.equals(order.getStatus())) {
            throw new BusinessException("检查/检验申请状态不允许执行");
        }

        LocalDateTime now = LocalDateTime.now();
        item.setExecutedAt(now);
        orderItemMapper.updateById(item);

        order.setStatus(ORDER_IN_PROGRESS);
        order.setExecutedAt(now);
        orderMapper.updateById(order);
        syncVisitStatus(order);

        return findTaskAfterWrite(orderItemId, doctor.getDoctorType());
    }

    @Transactional
    public ExamLabReportView createDraft(ReportDraftRequest request, Long doctorId, Long deptId, String doctorType) {
        DoctorEntity doctor = validateDoctor(doctorId, doctorType);
        if (request == null || request.orderItemId() == null) {
            throw new BusinessException("检查/检验项目不能为空");
        }
        ExamLabOrderItemEntity item = requireOrderItem(request.orderItemId());
        ExamLabOrderEntity order = requireOrder(item.getOrderId());
        validateTaskBelongsToRole(order, item, doctorType);
        ExamLabReportEntity report = findReportByOrderItem(item.getOrderItemId());
        if (report != null && isFinalReportStatus(report.getStatus())) {
            throw new BusinessException("报告已发布或回阅，无法重新生成AI解读");
        }
        validateOrderReadyForDraft(order);
        ExamLabTaskView task = findTaskAfterWrite(item.getOrderItemId(), doctorType);
        String aiDraft = buildAiDraft(request, order, item, task);
        if (report == null) {
            report = new ExamLabReportEntity();
            report.setOrderId(order.getOrderId());
            report.setOrderItemId(item.getOrderItemId());
            report.setPatientId(order.getPatientId());
            report.setReportNo(reportNoGenerator.next());
            report.setReportType(item.getItemType());
            report.setStatus(DRAFT);
        }
        report.setReportDoctorId(doctor.getDoctorId());
        report.setFindings(nullToBlank(request.resultDetail()));
        report.setConclusion("");
        report.setAiDraft(aiDraft);
        report.setDoctorReview("AI内容仅供辅助参考，请医生确认后发布。");
        report.setStatus(DRAFT);
        if (report.getReportId() == null) {
            reportMapper.insert(report);
        } else {
            reportMapper.updateById(report);
        }
        syncVisitStatus(order);

        return detail(report.getReportId());
    }

    @Transactional
    public ExamLabReportView publish(ReportPublishRequest request, Long doctorId, Long deptId, String doctorType) {
        DoctorEntity doctor = validateDoctor(doctorId, doctorType);
        if (request == null || request.reportId() == null) {
            throw new BusinessException("报告不能为空");
        }
        if (request.doctorConclusion() == null || request.doctorConclusion().isBlank()) {
            throw new BusinessException("请医生确认报告结论后再发布");
        }
        ExamLabReportEntity report = requireReport(request.reportId());
        if (PUBLISHED.equals(report.getStatus())) {
            throw new BusinessException("报告已发布，不可重复发布");
        }
        if (!DRAFT.equals(report.getStatus())) {
            throw new BusinessException("只有草稿报告可以发布");
        }
        ExamLabOrderItemEntity item = requireOrderItem(report.getOrderItemId());
        ExamLabOrderEntity order = requireOrder(report.getOrderId());
        validateTaskBelongsToRole(order, item, doctorType);

        LocalDateTime now = LocalDateTime.now();
        report.setReportDoctorId(doctor.getDoctorId());
        report.setConclusion(request.doctorConclusion().trim());
        report.setDoctorReview("");
        report.setStatus(PUBLISHED);
        report.setPublishedAt(now);
        reportMapper.updateById(report);

        item.setStatus(COMPLETED);
        orderItemMapper.updateById(item);
        completeParentOrderIfAllItemsCompleted(order, now);
        syncVisitStatus(order);

        return detail(report.getReportId());
    }

    public ExamLabReportView detail(Long reportId) {
        if (reportId == null) {
            throw new BusinessException("报告不能为空");
        }
        ExamLabReportView detail = queryMapper.findReportDetail(reportId);
        if (detail == null) {
            throw new BusinessException("报告不存在");
        }
        return detail;
    }

    public List<ExamLabReportView> reportsByRecord(Long recordId) {
        if (recordId == null) {
            throw new BusinessException("病历不能为空");
        }
        return queryMapper.findReportsByRecord(recordId);
    }

    @Transactional
    public void confirmSample(SampleConfirmRequest request, Long doctorId, Long deptId, String doctorType) {
        validateDoctor(doctorId, doctorType);
        if (request == null || request.orderItemId() == null) {
            throw new BusinessException("检查/检验项目不能为空");
        }
        if (request.sampleId() == null || request.sampleId().isBlank()) {
            throw new BusinessException("样本编号不能为空");
        }
        ExamLabOrderItemEntity item = requireOrderItem(request.orderItemId());
        ExamLabOrderEntity order = requireOrder(item.getOrderId());
        validateTaskBelongsToRole(order, item, doctorType);
        if (!ITEM_TYPE_LAB.equals(item.getItemType())) {
            throw new BusinessException("只有检验项目需要采集样本");
        }
        if (COMPLETED.equals(order.getStatus())) {
            throw new BusinessException("检查/检验项目已完成，不可重复操作");
        }
        if (ORDER_IN_PROGRESS.equals(order.getStatus())) {
            throw new BusinessException("样本已采集，不可重复操作");
        }

        String label = "样本已采集（编号 " + request.sampleId() + "）";
        item.setStatus(EXECUTED);
        item.setResultSummary(request.sampleId());
        item.setExecutedAt(LocalDateTime.now());
        orderItemMapper.updateById(item);

        order.setStatus(ORDER_IN_PROGRESS);
        order.setExecutedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        syncVisitStatus(order);
    }

    @Transactional
    public void saveExamResult(ExamResultSaveRequest request, Long doctorId, Long deptId, String doctorType) {
        DoctorEntity doctor = validateDoctor(doctorId, doctorType);
        if (request == null || request.orderItemId() == null) {
            throw new BusinessException("检查/检验项目不能为空");
        }
        ExamLabOrderItemEntity item = requireOrderItem(request.orderItemId());
        ExamLabOrderEntity order = requireOrder(item.getOrderId());
        validateTaskBelongsToRole(order, item, doctorType);
        validateOrderReadyForResult(order);
        if (request.resultData() == null || request.resultData().isEmpty()) {
            throw new BusinessException("检查结果不能为空");
        }

        Long orderItemId = item.getOrderItemId();
        String schemaItemName = normalizeSchemaItemName(request.itemName());
        ItemSchemaDef schema = EXAM_SCHEMAS.get(schemaItemName);
        if (schema == null) {
            throw new BusinessException("不支持的检查项目: " + request.itemName());
        }

        examResultFeatureMapper.delete(new LambdaQueryWrapper<ExamResultFeatureEntity>()
                .eq(ExamResultFeatureEntity::getOrderItemId, orderItemId));

        int sort = 1;
        Map<String, Object> data = request.resultData();
        if ("ecg".equals(schema.type()) && schema.vitals() != null) {
            for (IndicatorDef v : schema.vitals()) {
                ExamResultFeatureEntity f = new ExamResultFeatureEntity();
                f.setOrderItemId(orderItemId);
                f.setFeatureName(v.label());
                Object raw = data.get(v.key());
                String val = raw == null ? "" : raw.toString();
                f.setFeatureValue(val);
                f.setUnit(v.unit());
                f.setAbnormalFlag(resolveAbnormalFlag(val, v));
                f.setSortOrder(sort++);
                examResultFeatureMapper.insert(f);
            }
            // 保存 rhythm 和 axis
            for (String extra : List.of("rhythm", "axis")) {
                Object val = data.get(extra);
                if (val != null && !val.toString().isBlank()) {
                    String label = "rhythm".equals(extra) ? "心律" : "电轴";
                    ExamResultFeatureEntity f = new ExamResultFeatureEntity();
                    f.setOrderItemId(orderItemId);
                    f.setFeatureName(label);
                    f.setFeatureValue(val.toString());
                    f.setAbnormalFlag(NORMAL_FLAG);
                    f.setSortOrder(sort++);
                    examResultFeatureMapper.insert(f);
                }
            }
        } else if ("ct".equals(schema.type())) {
            @SuppressWarnings("unchecked")
            List<String> findings = (List<String>) data.get("findings");
            if (findings != null) {
                for (String finding : findings) {
                    ExamResultFeatureEntity f = new ExamResultFeatureEntity();
                    f.setOrderItemId(orderItemId);
                    f.setFeatureName(finding);
                    f.setFeatureValue("是");
                    f.setAbnormalFlag(ABNORMAL_FLAG);
                    f.setSortOrder(sort++);
                    examResultFeatureMapper.insert(f);
                }
            }
            Object notes = data.get("notes");
            if (notes != null && !notes.toString().isBlank()) {
                ExamResultFeatureEntity f = new ExamResultFeatureEntity();
                f.setOrderItemId(orderItemId);
                f.setFeatureName("补充描述");
                f.setFeatureValue(notes.toString());
                f.setAbnormalFlag(NORMAL_FLAG);
                f.setSortOrder(sort++);
                examResultFeatureMapper.insert(f);
            }
        }

        item.setStatus(EXECUTED);
        item.setResultSummary("检查结果已录入");
        orderItemMapper.updateById(item);
        keepOrderInProgress(order);
        syncVisitStatus(order);
    }

    @Transactional
    public void saveLabResult(LabResultSaveRequest request, Long doctorId, Long deptId, String doctorType) {
        DoctorEntity doctor = validateDoctor(doctorId, doctorType);
        if (request == null || request.orderItemId() == null) {
            throw new BusinessException("检查/检验项目不能为空");
        }
        ExamLabOrderItemEntity item = requireOrderItem(request.orderItemId());
        ExamLabOrderEntity order = requireOrder(item.getOrderId());
        validateTaskBelongsToRole(order, item, doctorType);
        validateOrderReadyForResult(order);
        if (request.values() == null || request.values().isEmpty()) {
            throw new BusinessException("检验结果不能为空");
        }

        String schemaItemName = normalizeSchemaItemName(request.itemName());
        List<IndicatorDef> panel = LAB_PANELS.get(schemaItemName);
        if (panel == null) {
            throw new BusinessException("不支持的检验项目: " + request.itemName());
        }

        Long orderItemId = item.getOrderItemId();
        String itemCode = resolveItemCode(item);
        labResultItemMapper.delete(new LambdaQueryWrapper<LabResultItemEntity>()
                .eq(LabResultItemEntity::getOrderItemId, orderItemId));

        int sort = 1;
        for (IndicatorDef ind : panel) {
            String raw = request.values().get(ind.key());
            if (raw == null || raw.isBlank()) {
                raw = ind.def();
            }
            LabResultItemEntity lr = new LabResultItemEntity();
            lr.setOrderItemId(orderItemId);
            lr.setItemCode(itemCode);
            lr.setIndicatorCode(ind.key());
            lr.setIndicatorName(ind.label());
            lr.setResultValue(raw);
            lr.setUnit(ind.unit());
            lr.setReferenceRange(buildReferenceRange(ind));
            lr.setAbnormalFlag(resolveAbnormalFlag(raw, ind));
            lr.setSortOrder(sort++);
            labResultItemMapper.insert(lr);
        }

        item.setStatus(EXECUTED);
        item.setResultSummary("检验结果已录入");
        orderItemMapper.updateById(item);
        keepOrderInProgress(order);
        syncVisitStatus(order);
    }

    @Transactional
    public void rejectReport(ReportRejectRequest request, Long doctorId, Long deptId, String doctorType) {
        DoctorEntity doctor = validateDoctor(doctorId, doctorType);
        if (request == null || request.orderItemId() == null) {
            throw new BusinessException("检查/检验项目不能为空");
        }
        ExamLabOrderItemEntity item = requireOrderItem(request.orderItemId());
        ExamLabOrderEntity order = requireOrder(item.getOrderId());
        validateTaskBelongsToRole(order, item, doctorType);

        List<ExamLabReportEntity> reports = reportMapper.selectList(
                new LambdaQueryWrapper<ExamLabReportEntity>()
                        .eq(ExamLabReportEntity::getOrderItemId, request.orderItemId())
                        .orderByDesc(ExamLabReportEntity::getCreatedAt));
        if (reports.isEmpty()) {
            throw new BusinessException("没有可退回的报告");
        }
        ExamLabReportEntity latest = reports.get(0);
        if (isFinalReportStatus(latest.getStatus()) || latest.getPublishedAt() != null) {
            throw new BusinessException("报告已发布或已回阅，无法退回重新录入");
        }
        if (!DRAFT.equals(latest.getStatus())) {
            throw new BusinessException("只有草稿报告可以退回");
        }

        clearResultRowsForReentry(item.getOrderItemId());
        reportMapper.deleteById(latest.getReportId());

        orderItemMapper.update(null, new LambdaUpdateWrapper<ExamLabOrderItemEntity>()
                .eq(ExamLabOrderItemEntity::getOrderItemId, item.getOrderItemId())
                .set(ExamLabOrderItemEntity::getStatus, EXECUTED)
                .set(ExamLabOrderItemEntity::getResultSummary, null));
        keepOrderInProgress(order);
        syncVisitStatus(order);
    }

    private void clearResultRowsForReentry(Long orderItemId) {
        examResultFeatureMapper.delete(new LambdaQueryWrapper<ExamResultFeatureEntity>()
                .eq(ExamResultFeatureEntity::getOrderItemId, orderItemId));
        labResultItemMapper.delete(new LambdaQueryWrapper<LabResultItemEntity>()
                .eq(LabResultItemEntity::getOrderItemId, orderItemId));
    }

    public ItemSchemaView getItemSchema(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            throw new BusinessException("项目名称不能为空");
        }
        String requestedItemName = itemName.trim();
        String schemaItemName = normalizeSchemaItemName(requestedItemName);
        ItemSchemaDef schema = EXAM_SCHEMAS.get(schemaItemName);
        if (schema != null) {
            List<ItemFieldDefView> fields = null;
            if (schema.vitals() != null) {
                fields = schema.vitals().stream()
                        .map(v -> new ItemFieldDefView(v.key(), v.label(), v.unit(), v.valueType(),
                                v.low(), v.high(), v.def(), v.options(), v.normal()))
                        .collect(Collectors.toList());
            }
            return new ItemSchemaView(requestedItemName, schema.type(), fields,
                    schema.findingOptions(), schema.rhythmOptions(), schema.axisOptions());
        }

        List<IndicatorDef> panel = LAB_PANELS.get(schemaItemName);
        if (panel != null) {
            List<ItemFieldDefView> fields = panel.stream()
                    .map(v -> new ItemFieldDefView(v.key(), v.label(), v.unit(), v.valueType(),
                            v.low(), v.high(), v.def(), v.options(), v.normal()))
                    .collect(Collectors.toList());
            return new ItemSchemaView(requestedItemName, "lab", fields, null, null, null);
        }

        throw new BusinessException("不支持的检查/检验项目: " + itemName);
    }

    private String normalizeSchemaItemName(String itemName) {
        if (itemName == null) {
            return null;
        }
        String name = itemName.trim();
        if (EXAM_SCHEMAS.containsKey(name) || LAB_PANELS.containsKey(name)) {
            return name;
        }
        if (name.contains("心电图")) {
            return "心电图";
        }
        if (name.contains("胸部CT")
                || name.contains("胸部DR")
                || name.contains("胸部正位片")
                || name.contains("DR正位片")) {
            return "胸部CT";
        }
        if (name.contains("血常规")) {
            return "血常规";
        }
        if (name.contains("C反应蛋白") || "CRP".equalsIgnoreCase(name)) {
            return "C反应蛋白";
        }
        if (name.contains("尿常规")) {
            return "尿常规";
        }
        if (name.contains("肝功能")) {
            return "肝功能";
        }
        if (name.contains("肾功能")) {
            return "肾功能";
        }
        if (name.contains("血糖")) {
            return "血糖";
        }
        return name;
    }

    private boolean isChestProjectionName(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return false;
        }
        String name = itemName.trim();
        return name.contains("胸部DR")
                || name.contains("胸部正位片")
                || name.contains("DR正位片");
    }

    public OrderDetailView getOrderItemDetail(Long orderItemId) {
        if (orderItemId == null) {
            throw new BusinessException("检查/检验项目不能为空");
        }
        Map<String, Object> row = queryMapper.findOrderItemDetail(orderItemId);
        if (row == null || row.isEmpty()) {
            throw new BusinessException("检查/检验项目不存在");
        }
        Long visitId = toLong(row.get("visitId"));
        syncVisitStatus(visitId);
        List<Map<String, Object>> features = queryMapper.findResultFeatures(orderItemId);
        List<Map<String, Object>> labItems = queryMapper.findLabResultItems(orderItemId);

        return new OrderDetailView(
                toLong(row.get("orderId")),
                toLong(row.get("orderItemId")),
                toLong(row.get("recordId")),
                toLong(row.get("patientId")),
                str(row.get("patientName")),
                str(row.get("gender")),
                toLocalDate(row.get("birthday")),
                str(row.get("applyDoctorName")),
                str(row.get("executeDeptName")),
                str(row.get("itemName")),
                str(row.get("itemType")),
                toBigDecimal(row.get("amount")),
                str(row.get("feeStatus")),
                str(row.get("orderStatus")),
                str(row.get("itemStatus")),
                str(row.get("clinicalDiagnosis")),
                str(row.get("purpose")),
                toLocalDateTime(row.get("appliedAt")),
                toLocalDateTime(row.get("executedAt")),
                str(row.get("resultSummary")),
                toLong(row.get("reportId")),
                str(row.get("reportNo")),
                str(row.get("reportStatus")),
                str(row.get("findings")),
                str(row.get("conclusion")),
                str(row.get("aiDraft")),
                str(row.get("doctorReview")),
                toLocalDateTime(row.get("publishedAt")),
                features,
                labItems
        );
    }

    private String resolveAbnormalFlag(String value, IndicatorDef def) {
        if (value == null || value.isBlank()) {
            return NORMAL_FLAG;
        }
        if ("enum".equals(def.valueType())) {
            return def.normal() != null && def.normal().equals(value) ? NORMAL_FLAG : ABNORMAL_FLAG;
        }
        try {
            double v = Double.parseDouble(value);
            if (def.low() != null && def.high() != null) {
                double low = Double.parseDouble(def.low());
                double high = Double.parseDouble(def.high());
                if (v < low) return LOW_FLAG;
                if (v > high) return HIGH_FLAG;
            }
            return NORMAL_FLAG;
        } catch (NumberFormatException e) {
            return NORMAL_FLAG;
        }
    }

    private String buildReferenceRange(IndicatorDef def) {
        if ("enum".equals(def.valueType())) {
            return def.normal();
        }
        if (def.low() != null && def.high() != null) {
            return def.low() + " - " + def.high();
        }
        return "";
    }

    private String resolveItemCode(ExamLabOrderItemEntity item) {
        if (item.getItemId() != null) {
            MedicalItemEntity medicalItem = medicalItemMapper.selectById(item.getItemId());
            if (medicalItem != null && medicalItem.getItemCode() != null && !medicalItem.getItemCode().isBlank()) {
                return medicalItem.getItemCode();
            }
        }
        return item.getItemName();
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(value.toString());
    }

    private String str(Object value) {
        return value == null ? null : value.toString();
    }

    private java.math.BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return java.math.BigDecimal.valueOf(n.doubleValue());
        return new java.math.BigDecimal(value.toString());
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime dt) return dt;
        return null;
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate d) return d;
        return null;
    }

    private String buildAiDraft(
            ReportDraftRequest request,
            ExamLabOrderEntity order,
            ExamLabOrderItemEntity item,
            ExamLabTaskView task
    ) {
        String generated = aiDraftService.generate(new AiDraftRequest(
                order.getOrderId(),
                item.getOrderItemId(),
                order.getPatientId(),
                item.getItemType(),
                item.getItemName(),
                task == null ? "" : task.patientName(),
                task == null ? "" : task.gender(),
                nullToBlank(request.resultDetail())
        ));
        if (generated == null || generated.isBlank()) {
            return nullToBlank(request.aiReportContent());
        }
        return generated;
    }

    private void completeParentOrderIfAllItemsCompleted(ExamLabOrderEntity order, LocalDateTime completedAt) {
        List<ExamLabOrderItemEntity> items = orderItemMapper.selectList(new LambdaQueryWrapper<ExamLabOrderItemEntity>()
                .eq(ExamLabOrderItemEntity::getOrderId, order.getOrderId()));
        if (!items.isEmpty() && allItemsHavePublishedReports(items)) {
            order.setStatus(COMPLETED);
            order.setCompletedAt(completedAt);
            orderMapper.updateById(order);
        }
    }

    private void keepOrderInProgress(ExamLabOrderEntity order) {
        if (order == null || COMPLETED.equals(order.getStatus()) || ORDER_PENDING_PAYMENT.equals(order.getStatus())) {
            return;
        }
        if (!ORDER_IN_PROGRESS.equals(order.getStatus())) {
            order.setStatus(ORDER_IN_PROGRESS);
            orderMapper.updateById(order);
        }
    }

    private void syncVisitStatus(ExamLabOrderEntity sourceOrder) {
        if (sourceOrder == null || sourceOrder.getVisitId() == null) {
            return;
        }
        syncVisitStatus(sourceOrder.getVisitId());
    }

    private void syncVisitStatus(Long visitId) {
        if (visitId == null) {
            return;
        }
        OutpatientVisitEntity visit = outpatientVisitMapper.selectById(visitId);
        if (visit == null || !isExamLabManagedVisitStatus(visit.getStatus())) {
            return;
        }

        String nextStatus = resolveVisitStatus(visitId);
        if (nextStatus != null && !nextStatus.equals(visit.getStatus())) {
            visit.setStatus(nextStatus);
            visit.setUpdatedAt(LocalDateTime.now());
            outpatientVisitMapper.updateById(visit);
        }
    }

    private boolean isExamLabManagedVisitStatus(String status) {
        return status == null
                || status.isBlank()
                || "接诊中".equals(status)
                || VISIT_STATUS_WAIT_EXAM_LAB.equals(status)
                || VISIT_STATUS_EXAM_LAB_IN_PROGRESS.equals(status)
                || VISIT_STATUS_REPORT_WAIT_REVIEW.equals(status);
    }

    private String resolveVisitStatus(Long visitId) {
        List<ExamLabOrderEntity> orders = orderMapper.selectList(new LambdaQueryWrapper<ExamLabOrderEntity>()
                .eq(ExamLabOrderEntity::getVisitId, visitId));
        if (orders.isEmpty()) {
            return null;
        }

        List<ExamLabOrderEntity> paidOrders = orders.stream()
                .filter(order -> PAID.equals(order.getFeeStatus()))
                .toList();
        if (paidOrders.isEmpty()) {
            return VISIT_STATUS_WAIT_EXAM_LAB;
        }

        boolean allCompleted = paidOrders.stream().allMatch(order -> COMPLETED.equals(order.getStatus()));
        if (allCompleted) {
            return VISIT_STATUS_REPORT_WAIT_REVIEW;
        }
        boolean anyStarted = paidOrders.stream()
                .anyMatch(order -> ORDER_IN_PROGRESS.equals(order.getStatus()) || COMPLETED.equals(order.getStatus()));
        return anyStarted ? VISIT_STATUS_EXAM_LAB_IN_PROGRESS : VISIT_STATUS_WAIT_EXAM_LAB;
    }

    private boolean allItemsHavePublishedReports(List<ExamLabOrderItemEntity> items) {
        for (ExamLabOrderItemEntity item : items) {
            Long publishedReportCount = reportMapper.selectCount(new LambdaQueryWrapper<ExamLabReportEntity>()
                    .eq(ExamLabReportEntity::getOrderItemId, item.getOrderItemId())
                    .eq(ExamLabReportEntity::getStatus, PUBLISHED));
            if (publishedReportCount == 0) {
                return false;
            }
        }
        return true;
    }

    private ExamLabTaskView findTaskAfterWrite(Long orderItemId, String doctorType) {
        String normalizedDoctorType = normalizeDoctorType(doctorType);
        return queryMapper.findPendingTasks(normalizedDoctorType, itemTypeOf(normalizedDoctorType)).stream()
                .filter(task -> orderItemId.equals(task.orderItemId()))
                .findFirst()
                .orElseGet(() -> {
                    ExamLabOrderItemEntity item = requireOrderItem(orderItemId);
                    ExamLabOrderEntity order = requireOrder(item.getOrderId());
                    return new ExamLabTaskView(
                            order.getOrderId(),
                            item.getOrderItemId(),
                            order.getVisitId(),
                            order.getRecordId(),
                            order.getPatientId(),
                            "",
                            "",
                            item.getItemName(),
                            item.getItemType(),
                            item.getAmount(),
                            order.getFeeStatus(),
                            order.getStatus(),
                            item.getStatus(),
                            order.getClinicalDiagnosis(),
                            order.getPurpose(),
                            order.getAppliedAt(),
                            item.getExecutedAt()
                    );
                });
    }

    private DoctorEntity validateDoctor(Long doctorId, String doctorType) {
        if (doctorId == null) {
            throw new BusinessException("医生不能为空");
        }
        String normalizedDoctorType = normalizeDoctorType(doctorType);
        DoctorEntity doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new BusinessException("医生不存在");
        }
        if (!normalizedDoctorType.equalsIgnoreCase(doctor.getDoctorType())) {
            throw new BusinessException("医生类型不匹配");
        }
        validateDoctorDepartmentType(doctor, normalizedDoctorType);
        return doctor;
    }

    private void validateDoctorDepartmentType(DoctorEntity doctor, String normalizedDoctorType) {
        DepartmentEntity department = departmentMapper.selectById(doctor.getDeptId());
        if (department == null) {
            throw new BusinessException("医生所属科室不存在");
        }
        if (!normalizedDoctorType.equalsIgnoreCase(department.getDeptType())) {
            throw new BusinessException("医生所属科室类型不匹配");
        }
    }

    private void validateTaskBelongsToRole(ExamLabOrderEntity order, ExamLabOrderItemEntity item, String doctorType) {
        String normalizedDoctorType = normalizeDoctorType(doctorType);
        DepartmentEntity executeDepartment = departmentMapper.selectById(order.getExecuteDeptId());
        if (executeDepartment == null) {
            throw new BusinessException("项目执行科室不存在");
        }
        if (!normalizedDoctorType.equalsIgnoreCase(executeDepartment.getDeptType())) {
            throw new BusinessException("项目执行科室类型不匹配");
        }
        if (!itemTypeOf(normalizedDoctorType).equals(item.getItemType())) {
            throw new BusinessException("医生类型不能处理该项目");
        }
    }

    private ExamLabOrderItemEntity requireOrderItem(Long orderItemId) {
        if (orderItemId == null) {
            throw new BusinessException("检查/检验项目不能为空");
        }
        ExamLabOrderItemEntity item = orderItemMapper.selectById(orderItemId);
        if (item == null) {
            throw new BusinessException("检查/检验项目不存在");
        }
        return item;
    }

    private ExamLabOrderEntity requireOrder(Long orderId) {
        ExamLabOrderEntity order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("检查/检验申请单不存在");
        }
        return order;
    }

    private ExamLabReportEntity requireReport(Long reportId) {
        ExamLabReportEntity report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("报告不存在");
        }
        return report;
    }

    private boolean isFinalReportStatus(String status) {
        return PUBLISHED.equals(status) || REVIEWED.equals(status);
    }

    private void validateOrderReadyForDraft(ExamLabOrderEntity order) {
        if (!PAID.equals(order.getFeeStatus()) || ORDER_PENDING_PAYMENT.equals(order.getStatus())) {
            throw new BusinessException("检查/检验项目未缴费，暂不可生成报告");
        }
        if (!ORDER_IN_PROGRESS.equals(order.getStatus())) {
            throw new BusinessException("检查/检验申请未执行，暂不可生成报告");
        }
    }

    private void validateOrderReadyForResult(ExamLabOrderEntity order) {
        if (!PAID.equals(order.getFeeStatus()) || ORDER_PENDING_PAYMENT.equals(order.getStatus())) {
            throw new BusinessException("检查/检验项目未缴费，不可录入结果");
        }
        if (!ORDER_IN_PROGRESS.equals(order.getStatus())) {
            throw new BusinessException("检查/检验申请未执行，不可录入结果");
        }
    }

    private ExamLabReportEntity findReportByOrderItem(Long orderItemId) {
        return reportMapper.selectOne(new LambdaQueryWrapper<ExamLabReportEntity>()
                .eq(ExamLabReportEntity::getOrderItemId, orderItemId)
                .last("LIMIT 1"));
    }

    private String itemTypeOf(String doctorType) {
        String normalized = normalizeDoctorType(doctorType);
        if (DOCTOR_TYPE_EXAM.equals(normalized)) {
            return ITEM_TYPE_EXAM;
        }
        return ITEM_TYPE_LAB;
    }

    private String normalizeDoctorType(String doctorType) {
        if (doctorType == null || doctorType.isBlank()) {
            throw new BusinessException("医生类型不能为空");
        }
        String normalized = doctorType.trim().toUpperCase();
        if (!DOCTOR_TYPE_EXAM.equals(normalized) && !DOCTOR_TYPE_LAB.equals(normalized)) {
            throw new BusinessException("医生类型不支持");
        }
        return normalized;
    }

    private ExamLabWorkbenchStatsView buildWorkbenchStats(List<ExamLabWorkbenchItemView> items) {
        long pendingCount = countWorkbenchStatus(items, WORKBENCH_PENDING);
        long progressCount = countWorkbenchStatus(items, WORKBENCH_PROGRESS);
        long publishedCount = countWorkbenchStatus(items, WORKBENCH_PUBLISHED);
        return new ExamLabWorkbenchStatsView(
                items.size(),
                pendingCount,
                progressCount,
                publishedCount,
                pendingCount + progressCount
        );
    }

    private long countWorkbenchStatus(List<ExamLabWorkbenchItemView> items, String status) {
        return items.stream()
                .filter(item -> status.equals(item.workbenchStatus()))
                .count();
    }

    private String normalizeWorkbenchStatus(String status) {
        if (status == null || status.isBlank()) {
            return WORKBENCH_ALL;
        }
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        if (WORKBENCH_ALL.equals(normalized)
                || WORKBENCH_PENDING.equals(normalized)
                || WORKBENCH_PROGRESS.equals(normalized)
                || WORKBENCH_PUBLISHED.equals(normalized)) {
            return normalized;
        }
        throw new BusinessException("工作台筛选状态不支持");
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value.trim();
    }
}
