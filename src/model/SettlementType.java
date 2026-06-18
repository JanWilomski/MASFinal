package model;

import java.io.Serializable;
import java.math.BigDecimal;

public abstract class SettlementType extends ObjectPlus implements Serializable {
    public abstract BigDecimal upfrontRequired(BigDecimal totalAmount);
}