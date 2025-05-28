package com.studyroom.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class Seat extends BaseEntity {
    @NotNull(message = "自习室ID不能为空")
    private Long roomId;
    
    @NotBlank(message = "座位号不能为空")
    @Size(max = 20, message = "座位号长度不能超过20")
    private String seatNumber;
    
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.0", message = "价格不能小于0")
    @DecimalMax(value = "999999.99", message = "价格不能超过999999.99")
    private BigDecimal price;
    
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(AVAILABLE|UNAVAILABLE)$", 
            message = "状态只能是AVAILABLE或UNAVAILABLE")
    private String status; // AVAILABLE/UNAVAILABLE
} 