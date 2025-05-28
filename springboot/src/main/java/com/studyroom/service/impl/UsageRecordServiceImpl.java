package com.studyroom.service.impl;

import com.studyroom.entity.Reservation;
import com.studyroom.entity.Seat;
import com.studyroom.entity.UsageRecord;
import com.studyroom.mapper.UsageRecordMapper;
import com.studyroom.service.ReservationService;
import com.studyroom.service.SeatService;
import com.studyroom.service.UsageRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsageRecordServiceImpl extends BaseServiceImpl<UsageRecord, UsageRecordMapper> implements UsageRecordService {
    
    @Autowired
    private ReservationService reservationService;
    
    @Autowired
    private SeatService seatService;
    
    @Override
    public UsageRecord getByReservationId(Long reservationId) {
        return baseMapper.selectByReservationId(reservationId);
    }
    
    @Override
    public List<UsageRecord> getByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }
    
    @Override
    public List<UsageRecord> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.selectByTimeRange(startTime, endTime);
    }
    
    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        baseMapper.updateStatus(id, status);
    }
    
    @Override
    @Transactional
    public UsageRecord startUsage(Long reservationId) {
        // 检查预约是否存在
        Reservation reservation = reservationService.getById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }
        
        // 检查是否已有使用记录
        UsageRecord existingRecord = getByReservationId(reservationId);
        if (existingRecord != null) {
            throw new RuntimeException("该预约已有使用记录");
        }
        
        // 创建使用记录
        UsageRecord usageRecord = new UsageRecord();
        usageRecord.setReservationId(reservationId);
        usageRecord.setActualStartTime(LocalDateTime.now());
        usageRecord.setStatus("IN_PROGRESS");
        
        // 更新预约状态
        reservationService.updateStatus(reservationId, "IN_USE");
        
        return create(usageRecord);
    }
    
    @Override
    @Transactional
    public void endUsage(Long id) {
        UsageRecord usageRecord = getById(id);
        if (usageRecord == null) {
            throw new RuntimeException("使用记录不存在");
        }
        
        // 更新使用记录
        usageRecord.setActualEndTime(LocalDateTime.now());
        usageRecord.setStatus("COMPLETED");
        update(usageRecord);
        
        // 获取预约信息
        Reservation reservation = reservationService.getById(usageRecord.getReservationId());
        
        // 更新预约状态
        reservationService.updateStatus(reservation.getId(), "COMPLETED");
    }
} 