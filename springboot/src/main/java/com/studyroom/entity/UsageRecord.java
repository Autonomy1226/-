package com.studyroom.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class UsageRecord extends BaseEntity {
    @NotNull(message = "预约ID不能为空")
    private Long reservationId;
    
    @NotNull(message = "实际开始时间不能为空")
    private LocalDateTime actualStartTime;
    
    private LocalDateTime actualEndTime;
    
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(IN_PROGRESS|COMPLETED|ABNORMAL)$", 
            message = "状态只能是IN_PROGRESS、COMPLETED或ABNORMAL")
    private String status; // IN_PROGRESS/COMPLETED/ABNORMAL
} 