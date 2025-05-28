package com.studyroom.service;

import com.studyroom.entity.Reservation;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationService extends BaseService<Reservation> {
    // 根据用户ID查询预约列表
    List<Reservation> getByUserId(Long userId);
    
    // 根据座位ID查询预约列表
    List<Reservation> getBySeatId(Long seatId);
    
    // 根据状态查询预约列表
    List<Reservation> getByStatus(String status);
    
    // 查询指定时间段的预约
    List<Reservation> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    // 更新预约状态
    void updateStatus(Long id, String status);
    
    // 创建预约
    Reservation createReservation(Reservation reservation);
    
    // 取消预约
    void cancelReservation(Long id);
} 