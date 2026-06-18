package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Prepaid extends SettlementType implements Serializable {
    private BigDecimal prepaidPercentage;
    private LocalDate refundableUntil;

    public Prepaid(BigDecimal prepaidPercentage, LocalDate refundableUntil){
        try {
            setPrepaidPercentage(prepaidPercentage);
            setRefundableUntil(refundableUntil);
        } catch (Exception e) {
            removeFromExtent();
            throw e;
        }
    }

    public BigDecimal getPrepaidPercentage(){ return prepaidPercentage; }
    public LocalDate getRefundableUntil(){ return refundableUntil; }

    public void setPrepaidPercentage(BigDecimal prepaidPercentage){
        if(prepaidPercentage == null
                || prepaidPercentage.compareTo(BigDecimal.ZERO) <= 0
                || prepaidPercentage.compareTo(BigDecimal.ONE) > 0){
            throw new IllegalArgumentException("Prepaid percentage must be in (0, 1]");
        }
        this.prepaidPercentage = prepaidPercentage;
    }

    public void setRefundableUntil(LocalDate refundableUntil){
        if(refundableUntil == null){
            throw new IllegalArgumentException("Refundable until date is null");
        }
        this.refundableUntil = refundableUntil;
    }

    public boolean isRefundable(){
        return !LocalDate.now().isAfter(refundableUntil);
    }

    @Override
    public BigDecimal upfrontRequired(BigDecimal totalAmount){
        return totalAmount.multiply(prepaidPercentage);
    }
}
