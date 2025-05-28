package com.studyroom.service;

import com.studyroom.entity.Seat;
import java.util.List;

public interface SeatService extends BaseService<Seat> {
    // 根据自习室ID查询座位列表
    List<Seat> getByRoomId(Long roomId);
    
    // 根据状态查询座位列表
    List<Seat> getByStatus(String status);
    
    // 更新座位状态
    void updateStatus(Long id, String status);
    
    // 根据自习室ID和状态查询座位
    List<Seat> getByRoomIdAndStatus(Long roomId, String status);
} 