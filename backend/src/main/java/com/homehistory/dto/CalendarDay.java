package com.homehistory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CalendarDay {
    private int day;
    private int count;
}
