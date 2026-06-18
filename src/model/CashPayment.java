package model;

import java.io.Serializable;
import java.math.BigDecimal;

public class CashPayment extends Payment implements Serializable {
    private BigDecimal receivedAmount;

    public CashPayment(BigDecimal amount, Rental rental, SettlementType settlementType, BigDecimal receivedAmount){
        super(amount, rental, settlementType);
        try {
            setReceivedAmount(receivedAmount);
        } catch (Exception e) {
            delete();
            throw e;
        }
    }

    public BigDecimal getReceivedAmount(){ return receivedAmount; }
    public BigDecimal getChange(){ return receivedAmount.subtract(getAmount()); }

    public void setReceivedAmount(BigDecimal receivedAmount){
        if(receivedAmount == null || receivedAmount.compareTo(getAmount()) < 0){
            throw new IllegalArgumentException("Received amount cannot be lower than payment amount");
        }
        this.receivedAmount = receivedAmount;
    }

    @Override
    public BigDecimal getSettlementFee(){
        return BigDecimal.ZERO;
    }
}