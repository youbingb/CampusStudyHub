package com.csh.modules.statistics.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StatsQuery {
    private LocalDate from;
    private LocalDate to;
    /** top-N，可空，默认 10。 */
    private Integer topN;
}
