package com.doctor.platform.pharmacy.controller;

import com.doctor.platform.shared.api.ApiResponse;
import com.doctor.platform.pharmacy.dto.DrugResponse;
import com.doctor.platform.pharmacy.service.DrugService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DrugController {

    private final DrugService drugService;

    public DrugController(DrugService drugService) {
        this.drugService = drugService;
    }

    @GetMapping("/api/drugs")
    public ApiResponse<List<DrugResponse>> listDrugs(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(drugService.listDrugs(keyword));
    }
}
