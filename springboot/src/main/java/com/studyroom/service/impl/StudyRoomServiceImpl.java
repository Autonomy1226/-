package com.studyroom.service.impl;

import com.studyroom.entity.StudyRoom;
import com.studyroom.mapper.StudyRoomMapper;
import com.studyroom.service.StudyRoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class StudyRoomServiceImpl extends BaseServiceImpl<StudyRoom, StudyRoomMapper> implements StudyRoomService {
    
    @Override
    public List<StudyRoom> getByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }
    
    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        baseMapper.updateStatus(id, status);
    }
    
    @Override
    public List<StudyRoom> getByCapacityRange(Integer minCapacity, Integer maxCapacity) {
        return baseMapper.selectByCapacityRange(minCapacity, maxCapacity);
    }
} 