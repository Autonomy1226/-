package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.entity.UsageRecord;
import com.studyroom.service.UsageRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/usage-records")
public class UsageRecordController {
    
    @Autowired
    private UsageRecordService usageRecordService;
    
    @PostMapping
    public Result<UsageRecord> create(@RequestBody UsageRecord usageRecord) {
        return Result.success(usageRecordService.create(usageRecord));
    }
    
    @GetMapping("/{id}")
    public Result<UsageRecord> getById(@PathVariable Long id) {
        return Result.success(usageRecordService.getById(id));
    }
    
    @GetMapping("/reservation/{reservationId}")
    public Result<UsageRecord> getByReservationId(@PathVariable Long reservationId) {
        return Result.success(usageRecordService.getByReservationId(reservationId));
    }
    
    @GetMapping("/status/{status}")
    public Result<List<UsageRecord>> getByStatus(@PathVariable String status) {
        return Result.success(usageRecordService.getByStatus(status));
    }
    
    @GetMapping("/time-range")
    public Result<List<UsageRecord>> getByTimeRange(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        return Result.success(usageRecordService.getByTimeRange(startTime, endTime));
    }
    
    @GetMapping
    public Result<List<UsageRecord>> getAll() {
        return Result.success(usageRecordService.getAll());
    }
    
    @PutMapping("/{id}")
    public Result<UsageRecord> update(@PathVariable Long id, @RequestBody UsageRecord usageRecord) {
        usageRecord.setId(id);
        return Result.success(usageRecordService.update(usageRecord));
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        usageRecordService.updateStatus(id, status);
        return Result.success();
    }
    
    @PostMapping("/{id}/start")
    public Result<UsageRecord> startUsage(@PathVariable Long id) {
        return Result.success(usageRecordService.startUsage(id));
    }
    
    @PostMapping("/{id}/end")
    public Result<Void> endUsage(@PathVariable Long id) {
        usageRecordService.endUsage(id);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        usageRecordService.delete(id);
        return Result.success();
    }
} 