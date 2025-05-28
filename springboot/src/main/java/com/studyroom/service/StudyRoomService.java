package com.studyroom.service;

import com.studyroom.entity.StudyRoom;
import java.util.List;

public interface StudyRoomService extends BaseService<StudyRoom> {
    // 根据状态查询自习室列表
    List<StudyRoom> getByStatus(String status);
    
    // 更新自习室状态
    void updateStatus(Long id, String status);
    
    // 根据容量范围查询自习室
    List<StudyRoom> getByCapacityRange(Integer minCapacity, Integer maxCapacity);
} 