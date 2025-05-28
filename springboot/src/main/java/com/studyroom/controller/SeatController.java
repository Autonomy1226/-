package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.entity.Seat;
import com.studyroom.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
public class SeatController {
    
    @Autowired
    private SeatService seatService;
    
    @PostMapping
    public Result<Seat> create(@RequestBody Seat seat) {
        return Result.success(seatService.create(seat));
    }
    
    @GetMapping("/{id}")
    public Result<Seat> getById(@PathVariable Long id) {
        return Result.success(seatService.getById(id));
    }
    
    @GetMapping("/room/{roomId}")
    public Result<List<Seat>> getByRoomId(@PathVariable Long roomId) {
        return Result.success(seatService.getByRoomId(roomId));
    }
    
    @GetMapping("/status/{status}")
    public Result<List<Seat>> getByStatus(@PathVariable String status) {
        return Result.success(seatService.getByStatus(status));
    }
    
    @GetMapping("/room/{roomId}/status/{status}")
    public Result<List<Seat>> getByRoomIdAndStatus(
            @PathVariable Long roomId,
            @PathVariable String status) {
        return Result.success(seatService.getByRoomIdAndStatus(roomId, status));
    }
    
    @GetMapping
    public Result<List<Seat>> getAll() {
        return Result.success(seatService.getAll());
    }
    
    @PutMapping("/{id}")
    public Result<Seat> update(@PathVariable Long id, @RequestBody Seat seat) {
        seat.setId(id);
        return Result.success(seatService.update(seat));
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        seatService.updateStatus(id, status);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        seatService.delete(id);
        return Result.success();
    }
} 