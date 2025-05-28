package com.studyroom.service.impl;

import com.studyroom.entity.Seat;
import com.studyroom.mapper.SeatMapper;
import com.studyroom.service.SeatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class SeatServiceImpl extends BaseServiceImpl<Seat, SeatMapper> implements SeatService {
    
    @Override
    public List<Seat> getByRoomId(Long roomId) {
        return baseMapper.selectByRoomId(roomId);
    }
    
    @Override
    public List<Seat> getByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }
    
    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        baseMapper.updateStatus(id, status);
    }
    
    @Override
    public List<Seat> getByRoomIdAndStatus(Long roomId, String status) {
        return baseMapper.selectByRoomIdAndStatus(roomId, status);
    }
} 