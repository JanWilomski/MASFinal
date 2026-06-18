package model;

import java.io.Serializable;
import java.math.BigDecimal;

public class TransferPayment extends Payment implements Serializable {
    private String iban;
    private int clearingDays;

    public TransferPayment(BigDecimal amount, Rental rental, SettlementType settlementType, String iban, int clearingDays){
        super(amount, rental, settlementType);
        try {
            setIban(iban);
            setClearingDays(clearingDays);
        } catch (Exception e) {
            delete();
            throw e;
        }
    }

    public String getIban(){ return iban; }
    public int getClearingDays(){ return clearingDays; }

    public void setIban(String iban){
        if(iban == null || iban.isBlank()){
            throw new IllegalArgumentException("IBAN is null or blank");
        }
        this.iban = iban;
    }

    public void setClearingDays(int clearingDays){
        if(clearingDays < 0){
            throw new IllegalArgumentException("Clearing days cannot be negative");
        }
        this.clearingDays = clearingDays;
    }

    @Override
    public BigDecimal getSettlementFee(){
        return BigDecimal.valueOf(2.50);
    }
}