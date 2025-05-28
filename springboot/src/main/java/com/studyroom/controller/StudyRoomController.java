package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.entity.StudyRoom;
import com.studyroom.service.StudyRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study-rooms")
public class StudyRoomController {
    
    @Autowired
    private StudyRoomService studyRoomService;
    
    @PostMapping
    public Result<StudyRoom> create(@RequestBody StudyRoom studyRoom) {
        return Result.success(studyRoomService.create(studyRoom));
    }
    
    @GetMapping("/{id}")
    public Result<StudyRoom> getById(@PathVariable Long id) {
        return Result.success(studyRoomService.getById(id));
    }
    
    @GetMapping("/status/{status}")
    public Result<List<StudyRoom>> getByStatus(@PathVariable String status) {
        return Result.success(studyRoomService.getByStatus(status));
    }
    
    @GetMapping("/capacity")
    public Result<List<StudyRoom>> getByCapacityRange(
            @RequestParam Integer minCapacity,
            @RequestParam Integer maxCapacity) {
        return Result.success(studyRoomService.getByCapacityRange(minCapacity, maxCapacity));
    }
    
    @GetMapping
    public Result<List<StudyRoom>> getAll() {
        return Result.success(studyRoomService.getAll());
    }
    
    @PutMapping("/{id}")
    public Result<StudyRoom> update(@PathVariable Long id, @RequestBody StudyRoom studyRoom) {
        studyRoom.setId(id);
        return Result.success(studyRoomService.update(studyRoom));
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        studyRoomService.updateStatus(id, status);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        studyRoomService.delete(id);
        return Result.success();
    }
} 