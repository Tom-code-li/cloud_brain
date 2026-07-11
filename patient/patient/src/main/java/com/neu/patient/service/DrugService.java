package com.neu.patient.service;

import com.neu.patient.entity.Drug;
import java.util.List;

public interface DrugService {
    Drug getDrugById(Long drugId);
    List<Drug> searchDrugs(String keyword);
}
