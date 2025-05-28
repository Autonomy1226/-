package com.studyroom.service;

import com.studyroom.entity.UsageRecord;
import java.time.LocalDateTime;
import java.util.List;

public interface UsageRecordService extends BaseService<UsageRecord> {
    // 根据预约ID查询使用记录
    UsageRecord getByReservationId(Long reservationId);
    
    // 根据状态查询使用记录列表
    List<UsageRecord> getByStatus(String status);
    
    // 查询指定时间段的使用记录
    List<UsageRecord> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    // 更新使用记录状态
    void updateStatus(Long id, String status);
    
    // 开始使用
    UsageRecord startUsage(Long reservationId);
    
    // 结束使用
    void endUsage(Long id);
} 