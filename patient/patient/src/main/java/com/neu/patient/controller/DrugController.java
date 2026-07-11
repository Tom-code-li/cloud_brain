package com.neu.patient.controller;

import com.neu.patient.common.Result;
import com.neu.patient.entity.Drug;
import com.neu.patient.service.DrugService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/drug")
public class DrugController {
    @Autowired private DrugService drugService;
    @GetMapping("/search")
    public Result<List<Drug>> search(@RequestParam String keyword) {
        return Result.ok(drugService.searchDrugs(keyword));
    }
    @GetMapping("/{drugId}")
    public Result<Drug> detail(@PathVariable Long drugId) {
        return Result.ok(drugService.getDrugById(drugId));
    }
}
