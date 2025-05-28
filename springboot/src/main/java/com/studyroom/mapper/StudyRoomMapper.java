package com.studyroom.mapper;

import com.studyroom.entity.StudyRoom;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface StudyRoomMapper extends BaseMapper<StudyRoom> {
    // 根据状态查询自习室列表
    List<StudyRoom> selectByStatus(@Param("status") String status);
    
    // 更新自习室状态
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    // 根据容量范围查询自习室
    List<StudyRoom> selectByCapacityRange(@Param("minCapacity") Integer minCapacity, @Param("maxCapacity") Integer maxCapacity);
} 