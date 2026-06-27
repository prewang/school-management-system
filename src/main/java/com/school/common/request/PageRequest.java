package com.school.common.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PageRequest {

    @Min(1)
    private int page = 1;

    @Min(1)
    @Max(100)
    private int size = 10;
}
