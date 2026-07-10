package com.cloudBrain.pharmacy.service.impl;

import com.cloudBrain.pharmacy.dto.DispenseItemDTO;
import com.cloudBrain.pharmacy.dto.DispenseQuery;
import com.cloudBrain.pharmacy.dto.DispenseRecordDTO;
import com.cloudBrain.pharmacy.dto.DispenseStatsDTO;
import com.cloudBrain.pharmacy.dto.DrugStockDTO;
import com.cloudBrain.pharmacy.dto.PurchaseRequest;
import com.cloudBrain.pharmacy.dto.PurchaseResponse;
import com.cloudBrain.pharmacy.dto.RefundItemRequest;
import com.cloudBrain.pharmacy.dto.RefundRequest;
import com.cloudBrain.pharmacy.dto.RefundResponse;
import com.cloudBrain.pharmacy.dto.StockQuery;
import com.cloudBrain.pharmacy.dto.StockUpdateRequest;
import com.cloudBrain.pharmacy.service.DispenseService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DispenseServiceImpl implements DispenseService {

    private static final String STATUS_PENDING = "待发药";
    private static final String STATUS_DISPENSED = "已发药";
    private static final String STATUS_RETURNED = "已退药";
    private static final String REFUND_AVAILABLE = "可退药";
    private static final String BUSINESS_TYPE_DISPENSE = "发药出库";
    private static final String BUSINESS_TYPE_RETURN = "退药入库";
    private static final String BUSINESS_TYPE_PURCHASE = "采购入库";
    private static final String BUSINESS_TYPE_MANUAL = "手工调整";
    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final BeanPropertyRowMapper<DispenseRecordView> dispenseRowMapper = BeanPropertyRowMapper.newInstance(DispenseRecordView.class);
    private final BeanPropertyRowMapper<DispenseItemView> itemRowMapper = BeanPropertyRowMapper.newInstance(DispenseItemView.class);
    private final BeanPropertyRowMapper<DrugStockDTO> stockRowMapper = BeanPropertyRowMapper.newInstance(DrugStockDTO.class);

    @Override
    public List<DispenseRecordDTO> listQueue(DispenseQuery query) {
        return loadDispenseRecords(query, false);
    }

    @Override
    public Optional<DispenseRecordDTO> getDetail(Long dispenseId) {
        List<DispenseRecordView> rows = fetchDispenseRowsById(dispenseId);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        DispenseRecordView record = rows.get(0);
        record.setItems(loadItemsByPrescriptionIds(List.of(record.getPrescriptionId()))
                .getOrDefault(record.getPrescriptionId(), List.of()));
        return Optional.of(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markDispensed(Long dispenseId, String pharmacist) {
        DispenseRecordView record = fetchDispenseRowsById(dispenseId).stream().findFirst().orElse(null);
        if (record == null) {
            return false;
        }
        if (STATUS_DISPENSED.equals(record.getStatus())) {
            return true;
        }
        if (STATUS_RETURNED.equals(record.getStatus())) {
            return false;
        }

        List<DispenseItemView> items = loadItemViewsByPrescriptionIds(List.of(record.getPrescriptionId()));
        if (items.isEmpty()) {
            throw new IllegalStateException("处方没有药品明细");
        }

        LocalDateTime now = LocalDateTime.now();
        for (DispenseItemView item : items) {
            BigDecimal before = queryDrugStock(item.getDrugId());
            BigDecimal quantity = normalize(item.getQuantity());
            BigDecimal after = before.subtract(quantity);
            if (after.compareTo(BigDecimal.ZERO) < 0) {
                after = BigDecimal.ZERO;
            }
            updateDrugStock(item.getDrugId(), after, null, now);
            insertStockRecord(item.getDrugId(), BUSINESS_TYPE_DISPENSE, dispenseId, quantity.negate(), before, after,
                    record.getPharmacyDoctorId(), StringUtils.hasText(pharmacist) ? pharmacist : BUSINESS_TYPE_DISPENSE);
        }

        jdbcTemplate.update("UPDATE pharmacy_dispense SET status = :status, dispensed_at = :dispensedAt, updated_at = :updatedAt WHERE dispense_id = :dispenseId",
                new MapSqlParameterSource()
                        .addValue("status", STATUS_DISPENSED)
                        .addValue("dispensedAt", now)
                        .addValue("updatedAt", now)
                        .addValue("dispenseId", dispenseId));
        jdbcTemplate.update("UPDATE prescription SET status = :status, updated_at = :updatedAt WHERE prescription_id = :prescriptionId",
                new MapSqlParameterSource()
                        .addValue("status", STATUS_DISPENSED)
                        .addValue("updatedAt", now)
                        .addValue("prescriptionId", record.getPrescriptionId()));
        jdbcTemplate.update("UPDATE prescription_item SET status = :status WHERE prescription_id = :prescriptionId",
                new MapSqlParameterSource()
                        .addValue("status", STATUS_DISPENSED)
                        .addValue("prescriptionId", record.getPrescriptionId()));
        return true;
    }

    @Override
    public DispenseStatsDTO getStats() {
        DispenseStatsDTO dto = new DispenseStatsDTO();
        dto.setTotal(count("SELECT COUNT(*) FROM pharmacy_dispense", new MapSqlParameterSource()));
        dto.setPending(count("SELECT COUNT(*) FROM pharmacy_dispense WHERE status = :status", new MapSqlParameterSource().addValue("status", STATUS_PENDING)));
        dto.setToday(count("SELECT COUNT(*) FROM pharmacy_dispense WHERE status = :status AND dispensed_at >= :todayStart",
                new MapSqlParameterSource().addValue("status", STATUS_DISPENSED).addValue("todayStart", LocalDate.now().atStartOfDay())));
        dto.setLowStock(count("SELECT COUNT(*) FROM drug WHERE status = 1 AND stock_quantity <= warning_quantity", new MapSqlParameterSource()));
        return dto;
    }

    @Override
    public List<DispenseRecordDTO> listRefundRecords(DispenseQuery query) {
        return loadDispenseRecords(query, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundResponse submitRefund(RefundRequest request) {
        DispenseRecordView record = fetchDispenseRowsById(request.getDispenseId()).stream().findFirst().orElse(null);
        if (record == null) {
            throw new IllegalArgumentException("未找到发药记录");
        }
        if (STATUS_RETURNED.equals(record.getStatus())) {
            throw new IllegalStateException("该发药单已退药");
        }

        List<DispenseItemView> itemViews = loadItemViewsByPrescriptionIds(List.of(record.getPrescriptionId()));
        Map<Long, DispenseItemView> itemMap = itemViews.stream().collect(Collectors.toMap(DispenseItemView::getDrugId, it -> it, (a, b) -> a));
        LocalDateTime now = LocalDateTime.now();
        Long firstRefundId = null;

        for (RefundItemRequest item : request.getItems()) {
            if (item.getRefundQuantity() == null || item.getRefundQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            DispenseItemView original = itemMap.get(item.getDrugId());
            if (original == null) {
                throw new IllegalArgumentException("退药药品不属于该处方");
            }
            BigDecimal quantity = normalize(item.getRefundQuantity());
            if (quantity.compareTo(normalize(original.getQuantity())) > 0) {
                throw new IllegalArgumentException("退药数量不能超过发药数量");
            }

            BigDecimal before = queryDrugStock(item.getDrugId());
            BigDecimal after = before.add(quantity);
            updateDrugStock(item.getDrugId(), after, null, now);
            insertStockRecord(item.getDrugId(), BUSINESS_TYPE_RETURN, request.getDispenseId(), quantity, before, after,
                    request.getOperatorUserId() != null ? request.getOperatorUserId() : record.getPharmacyDoctorId(), request.getReason());

            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(
                    "INSERT INTO pharmacy_return (dispense_id, prescription_id, drug_id, return_no, return_quantity, return_amount, reason, status, operator_user_id, returned_at, created_at) VALUES (:dispenseId, :prescriptionId, :drugId, :returnNo, :returnQuantity, :returnAmount, :reason, :status, :operatorUserId, :returnedAt, :createdAt)",
                    new MapSqlParameterSource()
                            .addValue("dispenseId", request.getDispenseId())
                            .addValue("prescriptionId", record.getPrescriptionId())
                            .addValue("drugId", item.getDrugId())
                            .addValue("returnNo", generateNo("RET"))
                            .addValue("returnQuantity", quantity)
                            .addValue("returnAmount", normalize(original.getUnitPrice().multiply(quantity)))
                            .addValue("reason", StringUtils.hasText(request.getReason()) ? request.getReason() : "退药申请")
                            .addValue("status", STATUS_RETURNED)
                            .addValue("operatorUserId", request.getOperatorUserId() != null ? request.getOperatorUserId() : record.getPharmacyDoctorId())
                            .addValue("returnedAt", now)
                            .addValue("createdAt", now),
                    keyHolder);
            Number generatedKey = keyHolder.getKey();
            if (firstRefundId == null && generatedKey != null) {
                firstRefundId = generatedKey.longValue();
            }
        }

        jdbcTemplate.update("UPDATE pharmacy_dispense SET status = :status, updated_at = :updatedAt WHERE dispense_id = :dispenseId",
                new MapSqlParameterSource().addValue("status", STATUS_RETURNED).addValue("updatedAt", now).addValue("dispenseId", request.getDispenseId()));
        jdbcTemplate.update("UPDATE prescription SET status = :status, updated_at = :updatedAt WHERE prescription_id = :prescriptionId",
                new MapSqlParameterSource().addValue("status", STATUS_RETURNED).addValue("updatedAt", now).addValue("prescriptionId", record.getPrescriptionId()));
        jdbcTemplate.update("UPDATE prescription_item SET status = :status WHERE prescription_id = :prescriptionId",
                new MapSqlParameterSource().addValue("status", STATUS_RETURNED).addValue("prescriptionId", record.getPrescriptionId()));

        RefundResponse response = new RefundResponse();
        response.setRefundId(firstRefundId);
        response.setDispenseId(request.getDispenseId());
        response.setPrescriptionNo(record.getPrescriptionNo());
        response.setRefundedAt(now);
        return response;
    }

    @Override
    public List<DrugStockDTO> listStock(StockQuery query) {
        return loadStockRows(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugStockDTO updateStock(Long drugId, StockUpdateRequest request) {
        DrugStockDTO current = loadDrugStockRow(drugId);
        if (current == null) {
            throw new IllegalArgumentException("未找到药品");
        }
        BigDecimal after = normalize(request.getStock());
        BigDecimal before = normalize(current.getStock());
        LocalDateTime now = LocalDateTime.now();
        updateDrugStock(drugId, after, StringUtils.hasText(request.getSupplier()) ? request.getSupplier() : current.getDefaultSupplier(), now);
        insertStockRecord(drugId, BUSINESS_TYPE_MANUAL, null, after.subtract(before), before, after, null, request.getRemark());
        return Optional.ofNullable(loadDrugStockRow(drugId)).orElse(current);
    }

    @Override
    public List<DrugStockDTO> listPurchaseRequests(StockQuery query) {
        return loadStockRows(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseResponse submitPurchase(PurchaseRequest request) {
        DrugStockDTO current = loadDrugStockRow(request.getDrugId());
        if (current == null) {
            throw new IllegalArgumentException("未找到药品");
        }
        BigDecimal quantity = normalize(request.getPurchaseQuantity());
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("采购数量必须大于0");
        }
        BigDecimal before = normalize(current.getStock());
        BigDecimal after = before.add(quantity);
        LocalDateTime now = LocalDateTime.now();
        updateDrugStock(request.getDrugId(), after, StringUtils.hasText(request.getSupplier()) ? request.getSupplier() : current.getDefaultSupplier(), now);
        insertStockRecord(request.getDrugId(), BUSINESS_TYPE_PURCHASE, null, quantity, before, after, null, request.getRemark());

        PurchaseResponse response = new PurchaseResponse();
        response.setPurchaseId(System.currentTimeMillis());
        response.setDrugId(request.getDrugId());
        response.setDrugName(current.getDrugName());
        response.setPurchaseQuantity(quantity);
        response.setSupplier(StringUtils.hasText(request.getSupplier()) ? request.getSupplier() : current.getDefaultSupplier());
        response.setStockAfterPurchase(after);
        response.setCreatedAt(now);
        return response;
    }

    @Override
    public List<DispenseRecordDTO> listHistory(DispenseQuery query) {
        return loadDispenseRecords(query, false);
    }

    private List<DispenseRecordDTO> loadDispenseRecords(DispenseQuery query, boolean refundableOnly) {
        List<DispenseRecordView> rows = loadDispenseRows(query, refundableOnly);
        Map<Long, List<DispenseItemDTO>> items = loadItemsByPrescriptionIds(rows.stream().map(DispenseRecordView::getPrescriptionId).toList());
        Map<Long, Boolean> refundState = refundableOnly ? loadRefundState(rows.stream().map(DispenseRecordView::getDispenseId).toList()) : Map.of();
        Map<Long, LocalDateTime> refundTimes = refundableOnly ? loadRefundTime(rows.stream().map(DispenseRecordView::getDispenseId).toList()) : Map.of();

        List<DispenseRecordDTO> result = new ArrayList<>();
        for (DispenseRecordView row : rows) {
            row.setItems(items.getOrDefault(row.getPrescriptionId(), List.of()));
            if (refundableOnly) {
                boolean refunded = refundState.getOrDefault(row.getDispenseId(), false);
                row.setStatus(refunded ? STATUS_RETURNED : REFUND_AVAILABLE);
                row.setRefundedAt(refundTimes.get(row.getDispenseId()));
                if (!matchesStatusFilter(query == null ? null : query.getStatus(), row.getStatus())) {
                    continue;
                }
            }
            result.add(row);
        }
        return result;
    }

    private List<DispenseRecordView> loadDispenseRows(DispenseQuery query, boolean refundableOnly) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT pd.dispense_id AS dispenseId, pd.prescription_id AS prescriptionId, pr.prescription_no AS prescriptionNo, pd.patient_id AS patientId, ")
                .append("p.patient_name AS patientName, p.gender AS gender, CASE WHEN p.birthday IS NULL THEN NULL ELSE TIMESTAMPDIFF(YEAR, p.birthday, CURDATE()) END AS age, ")
                .append("dpt.dept_name AS department, su.real_name AS doctorName, pr.diagnosis AS diagnosis, pd.total_amount AS totalAmount, ")
                .append("pr.fee_status AS payStatus, pd.status AS status, pd.created_at AS createdAt, pd.dispensed_at AS dispensedAt, ")
                .append("(SELECT MAX(r.returned_at) FROM pharmacy_return r WHERE r.dispense_id = pd.dispense_id AND r.returned_at IS NOT NULL) AS refundedAt, ")
                .append("pd.pharmacy_doctor_id AS pharmacyDoctorId FROM pharmacy_dispense pd ")
                .append("JOIN prescription pr ON pd.prescription_id = pr.prescription_id ")
                .append("JOIN patient p ON pd.patient_id = p.patient_id ")
                .append("JOIN doctor doc ON pr.doctor_id = doc.doctor_id ")
                .append("JOIN sys_user su ON doc.user_id = su.user_id ")
                .append("JOIN department dpt ON doc.dept_id = dpt.dept_id WHERE 1=1 ");

        MapSqlParameterSource params = new MapSqlParameterSource();
        if (query != null && query.getPatientId() != null) {
            sql.append(" AND pd.patient_id = :patientId");
            params.addValue("patientId", query.getPatientId());
        }
        if (query != null && StringUtils.hasText(query.getKeyword())) {
            sql.append(" AND (p.patient_name LIKE :keyword OR pr.prescription_no LIKE :keyword OR su.real_name LIKE :keyword)");
            params.addValue("keyword", like(query.getKeyword()));
        }
        if (query != null && StringUtils.hasText(query.getDepartment())) {
            sql.append(" AND dpt.dept_name LIKE :department");
            params.addValue("department", like(query.getDepartment()));
        }
        if (query != null && StringUtils.hasText(query.getStatus()) && !refundableOnly) {
            sql.append(" AND pd.status = :status");
            params.addValue("status", query.getStatus());
        }
        if (refundableOnly) {
            sql.append(" AND (pd.status = :dispensedStatus OR EXISTS (SELECT 1 FROM pharmacy_return r WHERE r.dispense_id = pd.dispense_id))");
            params.addValue("dispensedStatus", STATUS_DISPENSED);
            sql.append(" ORDER BY (CASE WHEN EXISTS (SELECT 1 FROM pharmacy_return r WHERE r.dispense_id = pd.dispense_id) THEN 1 ELSE 0 END) ASC, ")
                    .append("pd.created_at DESC, pd.dispense_id DESC");
        } else {
            sql.append(" ORDER BY (CASE WHEN pd.status = :pendingStatus THEN 0 ELSE 1 END) ASC, ")
                    .append("pd.created_at DESC, pd.dispense_id DESC");
            params.addValue("pendingStatus", STATUS_PENDING);
        }
        return jdbcTemplate.query(sql.toString(), params, dispenseRowMapper);
    }

    private List<DispenseRecordView> fetchDispenseRowsById(Long dispenseId) {
        return jdbcTemplate.query(
                "SELECT pd.dispense_id AS dispenseId, pd.prescription_id AS prescriptionId, pr.prescription_no AS prescriptionNo, pd.patient_id AS patientId, p.patient_name AS patientName, p.gender AS gender, CASE WHEN p.birthday IS NULL THEN NULL ELSE TIMESTAMPDIFF(YEAR, p.birthday, CURDATE()) END AS age, dpt.dept_name AS department, su.real_name AS doctorName, pr.diagnosis AS diagnosis, pd.total_amount AS totalAmount, pr.fee_status AS payStatus, pd.status AS status, pd.created_at AS createdAt, pd.dispensed_at AS dispensedAt, (SELECT MAX(r.returned_at) FROM pharmacy_return r WHERE r.dispense_id = pd.dispense_id AND r.returned_at IS NOT NULL) AS refundedAt, pd.pharmacy_doctor_id AS pharmacyDoctorId FROM pharmacy_dispense pd JOIN prescription pr ON pd.prescription_id = pr.prescription_id JOIN patient p ON pd.patient_id = p.patient_id JOIN doctor doc ON pr.doctor_id = doc.doctor_id JOIN sys_user su ON doc.user_id = su.user_id JOIN department dpt ON doc.dept_id = dpt.dept_id WHERE pd.dispense_id = :dispenseId",
                new MapSqlParameterSource().addValue("dispenseId", dispenseId),
                dispenseRowMapper);
    }

    private Map<Long, List<DispenseItemDTO>> loadItemsByPrescriptionIds(Collection<Long> prescriptionIds) {
        if (CollectionUtils.isEmpty(prescriptionIds)) {
            return Map.of();
        }
        List<DispenseItemView> rows = loadItemViewsByPrescriptionIds(prescriptionIds);
        Map<Long, List<DispenseItemDTO>> grouped = new LinkedHashMap<>();
        for (DispenseItemView row : rows) {
            grouped.computeIfAbsent(row.getPrescriptionId(), key -> new ArrayList<>()).add(row);
        }
        return grouped;
    }

    private List<DispenseItemView> loadItemViewsByPrescriptionIds(Collection<Long> prescriptionIds) {
        if (CollectionUtils.isEmpty(prescriptionIds)) {
            return List.of();
        }
        return jdbcTemplate.query(
                "SELECT pi.prescription_id AS prescriptionId, pi.drug_id AS drugId, COALESCE(pi.drug_name, d.drug_name) AS drugName, pi.specification AS specification, pi.quantity AS quantity, d.unit AS unit, CONCAT_WS(' ', NULLIF(pi.usage_method, ''), NULLIF(pi.frequency, ''), NULLIF(pi.dosage, ''), CASE WHEN pi.days IS NULL THEN NULL ELSE CONCAT(pi.days, '天') END) AS `usage`, d.stock_quantity AS stock, pi.unit_price AS unitPrice, pi.amount AS amount FROM prescription_item pi LEFT JOIN drug d ON pi.drug_id = d.drug_id WHERE pi.prescription_id IN (:ids) ORDER BY pi.prescription_item_id ASC",
                new MapSqlParameterSource().addValue("ids", prescriptionIds),
                itemRowMapper);
    }

    private Map<Long, Boolean> loadRefundState(Collection<Long> dispenseIds) {
        if (CollectionUtils.isEmpty(dispenseIds)) {
            return Map.of();
        }
        List<Long> ids = jdbcTemplate.queryForList("SELECT DISTINCT dispense_id FROM pharmacy_return WHERE dispense_id IN (:ids)",
                new MapSqlParameterSource().addValue("ids", dispenseIds),
                Long.class);
        return ids.stream().collect(Collectors.toMap(id -> id, id -> true, (a, b) -> a, LinkedHashMap::new));
    }

    private Map<Long, LocalDateTime> loadRefundTime(Collection<Long> dispenseIds) {
        if (CollectionUtils.isEmpty(dispenseIds)) {
            return Map.of();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT dispense_id, MAX(returned_at) AS refundedAt FROM pharmacy_return WHERE dispense_id IN (:ids) AND returned_at IS NOT NULL GROUP BY dispense_id",
                new MapSqlParameterSource().addValue("ids", dispenseIds));
        Map<Long, LocalDateTime> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Object id = row.get("dispense_id");
            Object time = row.get("refundedAt");
            if (id instanceof Number number && time instanceof Timestamp timestamp) {
                result.put(number.longValue(), timestamp.toLocalDateTime());
            }
        }
        return result;
    }

    private List<DrugStockDTO> loadStockRows(StockQuery query) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT d.drug_id AS drugId, d.drug_code AS drugCode, d.drug_name AS drugName, d.specification AS specification, d.unit AS unit, ")
                .append("d.stock_quantity AS stock, d.warning_quantity AS warningQuantity, d.sale_price AS salePrice, ")
                .append("COALESCE(NULLIF(d.manufacturer, ''), '') AS defaultSupplier, ")
                .append("CASE WHEN d.stock_quantity <= 20 THEN 'danger' WHEN d.stock_quantity <= 50 THEN 'warning' ELSE 'success' END AS status, ")
                .append("d.updated_at AS updatedAt FROM drug d WHERE d.status = 1");
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (query != null && StringUtils.hasText(query.getKeyword())) {
            sql.append(" AND (d.drug_name LIKE :keyword OR d.drug_code LIKE :keyword)");
            params.addValue("keyword", like(query.getKeyword()));
        }
        if (query != null && StringUtils.hasText(query.getStatus())) {
            switch (query.getStatus()) {
                case "danger" -> sql.append(" AND d.stock_quantity <= 20");
                case "warning" -> sql.append(" AND d.stock_quantity > 20 AND d.stock_quantity <= 50");
                case "success" -> sql.append(" AND d.stock_quantity > 50");
                default -> {
                }
            }
        }
        sql.append(" ORDER BY d.updated_at DESC, d.drug_id DESC");
        return jdbcTemplate.query(sql.toString(), params, stockRowMapper);
    }

    private DrugStockDTO loadDrugStockRow(Long drugId) {
        List<DrugStockDTO> rows = jdbcTemplate.query(
                "SELECT d.drug_id AS drugId, d.drug_code AS drugCode, d.drug_name AS drugName, d.specification AS specification, d.unit AS unit, d.stock_quantity AS stock, d.warning_quantity AS warningQuantity, d.sale_price AS salePrice, COALESCE(NULLIF(d.manufacturer, ''), '') AS defaultSupplier, CASE WHEN d.stock_quantity <= 20 THEN 'danger' WHEN d.stock_quantity <= 50 THEN 'warning' ELSE 'success' END AS status, d.updated_at AS updatedAt FROM drug d WHERE d.drug_id = :drugId",
                new MapSqlParameterSource().addValue("drugId", drugId),
                stockRowMapper);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private BigDecimal queryDrugStock(Long drugId) {
        BigDecimal stock = jdbcTemplate.queryForObject("SELECT stock_quantity FROM drug WHERE drug_id = :drugId",
                new MapSqlParameterSource().addValue("drugId", drugId),
                BigDecimal.class);
        return stock == null ? BigDecimal.ZERO : stock;
    }

    private void updateDrugStock(Long drugId, BigDecimal stock, String supplier, LocalDateTime updatedAt) {
        jdbcTemplate.update(
                "UPDATE drug SET stock_quantity = :stock, manufacturer = COALESCE(NULLIF(:supplier, ''), manufacturer), updated_at = :updatedAt WHERE drug_id = :drugId",
                new MapSqlParameterSource()
                        .addValue("drugId", drugId)
                        .addValue("stock", normalize(stock))
                        .addValue("supplier", supplier)
                        .addValue("updatedAt", updatedAt == null ? LocalDateTime.now() : updatedAt));
    }

    private void insertStockRecord(Long drugId, String businessType, Long businessId, BigDecimal changeQuantity,
                                   BigDecimal beforeQuantity, BigDecimal afterQuantity, Long operatorUserId, String remark) {
        jdbcTemplate.update(
                "INSERT INTO drug_stock_record (drug_id, business_type, business_id, change_quantity, before_quantity, after_quantity, operator_user_id, remark, created_at) VALUES (:drugId, :businessType, :businessId, :changeQuantity, :beforeQuantity, :afterQuantity, :operatorUserId, :remark, :createdAt)",
                new MapSqlParameterSource()
                        .addValue("drugId", drugId)
                        .addValue("businessType", businessType)
                        .addValue("businessId", businessId)
                        .addValue("changeQuantity", normalize(changeQuantity))
                        .addValue("beforeQuantity", normalize(beforeQuantity))
                        .addValue("afterQuantity", normalize(afterQuantity))
                        .addValue("operatorUserId", operatorUserId)
                        .addValue("remark", StringUtils.hasText(remark) ? remark : businessType)
                        .addValue("createdAt", LocalDateTime.now()));
    }

    private boolean matchesStatusFilter(String expected, String actual) {
        return !StringUtils.hasText(expected) || Objects.equals(expected, actual);
    }

    private String like(String value) {
        return "%" + value.trim() + "%";
    }

    private int count(String sql, MapSqlParameterSource params) {
        Integer value = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return value == null ? 0 : value;
    }

    private String generateNo(String prefix) {
        return prefix + LocalDateTime.now().format(NO_FORMATTER);
    }

    private BigDecimal normalize(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class DispenseRecordView extends DispenseRecordDTO {
        private Long prescriptionId;
        private Long pharmacyDoctorId;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class DispenseItemView extends DispenseItemDTO {
        private Long prescriptionId;
        private BigDecimal unitPrice;
        private BigDecimal amount;
    }
}