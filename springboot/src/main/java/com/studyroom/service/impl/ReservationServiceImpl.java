package com.studyroom.service.impl;

import com.studyroom.entity.Reservation;
import com.studyroom.entity.Seat;
import com.studyroom.entity.User;
import com.studyroom.mapper.ReservationMapper;
import com.studyroom.service.ReservationService;
import com.studyroom.service.SeatService;
import com.studyroom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Duration;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ReservationServiceImpl extends BaseServiceImpl<Reservation, ReservationMapper> implements ReservationService {
    
    @Autowired
    private SeatService seatService;
    
    @Autowired
    private UserService userService;
    
    @Override
    public List<Reservation> getByUserId(Long userId) {
        return baseMapper.selectByUserId(userId);
    }
    
    @Override
    public List<Reservation> getBySeatId(Long seatId) {
        return baseMapper.selectBySeatId(seatId);
    }
    
    @Override
    public List<Reservation> getByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }
    
    @Override
    public List<Reservation> getByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.selectByTimeRange(startTime, endTime);
    }
    
    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        baseMapper.updateStatus(id, status);
    }
    
    @Override
    @Transactional
    public Reservation createReservation(Reservation reservation) {
        // 检查用户是否存在
        User user = userService.getById(reservation.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查座位是否存在
        Seat seat = seatService.getById(reservation.getSeatId());
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }
        
        // 检查时间是否有效
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveMinutesAgo = now.minusMinutes(5);
        if (reservation.getStartTime().isBefore(fiveMinutesAgo)) {
            throw new RuntimeException("开始时间不能早于当前时间的前五分钟");
        }
        if (reservation.getEndTime().isBefore(reservation.getStartTime())) {
            throw new RuntimeException("结束时间不能早于开始时间");
        }
        
        // 检查时间冲突
        List<Reservation> existingReservations = getBySeatId(reservation.getSeatId());
        for (Reservation existing : existingReservations) {
            // 只检查状态为PENDING的预约
            if (!"PENDING".equals(existing.getStatus())) {
                continue;
            }
            
            // 如果是同一个用户，跳过冲突检查
            if (existing.getUserId().equals(reservation.getUserId())) {
                continue;
            }
            
            // 检查时间是否冲突
            if (isDateAndTimeOverlap(
                reservation.getStartTime(), 
                reservation.getEndTime(),
                existing.getStartTime(),
                existing.getEndTime()
            )) {
                throw new RuntimeException("该时间段已被其他用户预约");
            }
        }
        
        // 计算总价（每30分钟为一个单位）
        Duration duration = Duration.between(reservation.getStartTime(), reservation.getEndTime());
        long minutes = duration.toMinutes();
        // 计算30分钟的单位数，向上取整
        long units = (minutes + 29) / 30; // 加29是为了向上取整
        BigDecimal totalPrice = seat.getPrice().multiply(BigDecimal.valueOf(units))
            .setScale(2, RoundingMode.HALF_UP);
        reservation.setTotalPrice(totalPrice);
        
        // 设置初始状态
        reservation.setStatus("PENDING");
        
        // 创建预约
        return create(reservation);
    }
    
    @Override
    @Transactional
    public void cancelReservation(Long id) {
        Reservation reservation = getById(id);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }
        
        // 更新预约状态
        updateStatus(id, "CANCELLED");
    }
    
    /**
     * 检查两个时间段是否重叠
     */
    private boolean isDateAndTimeOverlap(
            LocalDateTime start1, 
            LocalDateTime end1, 
            LocalDateTime start2, 
            LocalDateTime end2) {
        // 检查时间重叠
        return !(end1.isBefore(start2) || start1.isAfter(end2));
    }
} 