package ch.bzz.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    private Long id;
    private LocalDate date;
    private String text;
    private Account debitAccount;
    private Account creditAccount;
    private BigDecimal amount;
    private Project project;
}
