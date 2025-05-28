package com.studyroom.mapper;

import com.studyroom.entity.UsageRecord;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface UsageRecordMapper extends BaseMapper<UsageRecord> {
    // 根据预约ID查询使用记录
    UsageRecord selectByReservationId(@Param("reservationId") Long reservationId);
    
    // 根据状态查询使用记录列表
    List<UsageRecord> selectByStatus(@Param("status") String status);
    
    // 查询指定时间段的使用记录
    List<UsageRecord> selectByTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    // 更新使用记录状态
    int updateStatus(@Param("id") Long id, @Param("status") String status);
} 