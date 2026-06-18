package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Postpaid extends SettlementType implements Serializable {
    private LocalDate dueDate;
    private BigDecimal lateFeeRate;

    public Postpaid(LocalDate dueDate, BigDecimal lateFeeRate){
        try {
            setDueDate(dueDate);
            setLateFeeRate(lateFeeRate);
        } catch (Exception e) {
            removeFromExtent();
            throw e;
        }
    }

    public LocalDate getDueDate(){ return dueDate; }
    public BigDecimal getLateFeeRate(){ return lateFeeRate; }

    public void setDueDate(LocalDate dueDate){
        if(dueDate == null || dueDate.isBefore(LocalDate.now())){
            throw new IllegalArgumentException("Due date cannot be in the past");
        }
        this.dueDate = dueDate;
    }

    public void setLateFeeRate(BigDecimal lateFeeRate){
        if(lateFeeRate == null || lateFeeRate.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("Late fee rate cannot be negative");
        }
        this.lateFeeRate = lateFeeRate;
    }

    public BigDecimal calculateLateFee(BigDecimal amount){
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Amount must be positive");
        }
        LocalDate today = LocalDate.now();
        if(!today.isAfter(dueDate)){
            return BigDecimal.ZERO;
        }
        long daysLate = ChronoUnit.DAYS.between(dueDate, today);
        return amount.multiply(lateFeeRate).multiply(BigDecimal.valueOf(daysLate));
    }

    @Override
    public BigDecimal upfrontRequired(BigDecimal totalAmount){
        return BigDecimal.ZERO;
    }
}
