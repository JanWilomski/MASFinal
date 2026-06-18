package model;

import java.io.Serializable;
import java.math.BigDecimal;

public class CardPayment extends Payment implements Serializable {
    private String cardNumber;
    private String transactionId;

    public CardPayment(BigDecimal amount, Rental rental, SettlementType settlementType, String cardNumber, String transactionId){
        super(amount, rental, settlementType);
        try {
            setCardNumber(cardNumber);
            setTransactionId(transactionId);
        } catch (Exception e) {
            delete();
            throw e;
        }
    }

    public String getCardNumber(){ return cardNumber; }
    public String getTransactionId(){ return transactionId; }

    public void setCardNumber(String cardNumber){
        if(cardNumber == null || cardNumber.isBlank()){
            throw new IllegalArgumentException("Card number is null or blank");
        }
        this.cardNumber = cardNumber;
    }

    public void setTransactionId(String transactionId){
        if(transactionId == null || transactionId.isBlank()){
            throw new IllegalArgumentException("Transaction id is null or blank");
        }
        this.transactionId = transactionId;
    }

    @Override
    public BigDecimal getSettlementFee(){
        return getAmount().multiply(BigDecimal.valueOf(0.018));
    }
}