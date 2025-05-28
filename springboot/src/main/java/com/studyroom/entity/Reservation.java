package com.studyroom.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class Reservation extends BaseEntity {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotNull(message = "座位ID不能为空")
    private Long seatId;
    
    @NotNull(message = "开始时间不能为空")
    @Future(message = "开始时间必须是将来时间")
    private LocalDateTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    @Future(message = "结束时间必须是将来时间")
    private LocalDateTime endTime;
    
    @NotNull(message = "总价不能为空")
    @DecimalMin(value = "0.0", message = "总价不能小于0")
    @DecimalMax(value = "999999.99", message = "总价不能超过999999.99")
    private BigDecimal totalPrice;
    
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(PENDING|IN_USE|COMPLETED|CANCELLED)$", 
            message = "状态只能是PENDING、IN_USE、COMPLETED或CANCELLED")
    private String status;
} 