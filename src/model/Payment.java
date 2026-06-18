package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public abstract class Payment extends ObjectPlus implements Serializable {
    private BigDecimal amount;
    private LocalDate paymentDate;
    private Rental rental;
    private final SettlementType settlementType;

    public Payment(BigDecimal amount, Rental rental, SettlementType settlementType){
        try {
            setAmount(amount);
            this.paymentDate = LocalDate.now();
            if (rental == null) {
                throw new IllegalArgumentException("Rental is null");
            }
            if (settlementType == null) {
                throw new IllegalArgumentException("Settlement type is null");
            }
            this.rental = rental;
            this.settlementType = settlementType;
            rental.addPayment(this);
        } catch (Exception e) {
            removeFromExtent();
            throw e;
        }
    }

    public abstract BigDecimal getSettlementFee();

    public BigDecimal getTotalCost(){
        return amount.add(getSettlementFee());
    }

    public BigDecimal getUpfrontRequired(){
        return settlementType.upfrontRequired(amount);
    }

    public BigDecimal getAmount(){ return amount; }
    public LocalDate getPaymentDate(){ return paymentDate; }
    public Rental getRental(){ return rental; }
    public SettlementType getSettlementType(){ return settlementType; }

    public void setAmount(BigDecimal amount){
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.amount = amount;
    }

    public void delete(){
        Rental old = this.rental;
        this.rental = null;
        if(old != null){
            old.removePayment(this);
        }
        removeFromExtent();
    }

    @Override
    public String toString(){
        return getClass().getSimpleName() + "{amount=" + amount +
                ", settlement=" + settlementType.getClass().getSimpleName() +
                ", fee=" + getSettlementFee() + "}";
    }
}