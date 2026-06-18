package model;

import enums.InsuranceType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;

public class InsurancePolicy extends ObjectPlus implements Serializable {

    private final EnumSet<InsuranceType> coverages;

    private BigDecimal ocLiabilityLimit;// OC
    private BigDecimal acSumInsured;// AC
    private int assistanceMaxTowingKm;// ASSISTANCE
    private BigDecimal nnwSumPerPerson;// NNW
    private BigDecimal gapPercentage;// GAP

    private Car car;

    public InsurancePolicy(EnumSet<InsuranceType> coverages,
                           BigDecimal ocLiabilityLimit,
                           BigDecimal acSumInsured,
                           int assistanceMaxTowingKm,
                           BigDecimal nnwSumPerPerson,
                           BigDecimal gapPercentage) {
        try {
            if (coverages == null || coverages.isEmpty()) {
                throw new IllegalArgumentException("Policy must have at least OC");
            }else if(!coverages.contains(InsuranceType.OC)){
                throw new IllegalArgumentException("Policy must have OC");
            }
            this.coverages = EnumSet.copyOf(coverages);

            if (this.coverages.contains(InsuranceType.OC))setOcLiabilityLimit(ocLiabilityLimit);
            if (this.coverages.contains(InsuranceType.AC))setAcSumInsured(acSumInsured);
            if (this.coverages.contains(InsuranceType.ASSISTANCE))setAssistanceMaxTowingKm(assistanceMaxTowingKm);
            if (this.coverages.contains(InsuranceType.NNW))setNnwSumPerPerson(nnwSumPerPerson);
            if (this.coverages.contains(InsuranceType.GAP))setGapPercentage(gapPercentage);
        } catch (Exception e) {
            removeFromExtent();
            throw e;
        }
    }

    private void requireRole(InsuranceType role) {
        if (!coverages.contains(role)) {
            throw new IllegalStateException("Policy does not have coverage: " + role);
        }
    }

    public EnumSet<InsuranceType> getCoverages() {
        return EnumSet.copyOf(coverages);
    }

    // OC
    public BigDecimal getOcLiabilityLimit() {
        requireRole(InsuranceType.OC);
        return ocLiabilityLimit;
    }

    public void setOcLiabilityLimit(BigDecimal ocLiabilityLimit) {
        requireRole(InsuranceType.OC);
        if(ocLiabilityLimit == null || ocLiabilityLimit.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("OcLiabilityLimit must be positive");
        }
        this.ocLiabilityLimit = ocLiabilityLimit;
    }

    // AC
    public BigDecimal getAcSumInsured() {
        requireRole(InsuranceType.AC);
        return acSumInsured;
    }
    public void setAcSumInsured(BigDecimal acSumInsured) {
        requireRole(InsuranceType.AC);
        if(acSumInsured == null || acSumInsured.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("AcSumInsured must be positive");
        }
        this.acSumInsured = acSumInsured;
    }

    // ASSISTANCE
    public int getAssistanceMaxTowingKm(){
        requireRole(InsuranceType.ASSISTANCE);
        return assistanceMaxTowingKm;
    }
    public void setAssistanceMaxTowingKm(int assistanceMaxTowingKm){
        requireRole(InsuranceType.ASSISTANCE);
        if(assistanceMaxTowingKm <= 0){
            throw new IllegalArgumentException("AssistanceMaxTowingKm must be positive");
        }
        this.assistanceMaxTowingKm = assistanceMaxTowingKm;
    }

    // NNW
    public BigDecimal getNnwSumPerPerson(){
        requireRole(InsuranceType.NNW);
        return nnwSumPerPerson;
    }
    public void setNnwSumPerPerson(BigDecimal nnwSumPerPerson){
        requireRole(InsuranceType.NNW);
        if(nnwSumPerPerson == null || nnwSumPerPerson.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("NnwSumPerPerson must be positive");
        }
        this.nnwSumPerPerson = nnwSumPerPerson;
    }

    // GAP
    public BigDecimal getGapPercentage(){
        requireRole(InsuranceType.GAP);
        return gapPercentage;
    }
    public void setGapPercentage(BigDecimal gapPercentage){
        requireRole(InsuranceType.GAP);
        if(gapPercentage == null || gapPercentage.compareTo(BigDecimal.ZERO) <= 0 || gapPercentage.compareTo(BigDecimal.ONE) >= 0){
            throw new IllegalArgumentException("GapPercentage must be in between 0 and 1");
        }
        this.gapPercentage = gapPercentage;
    }



    public Car getCar() {
        return car;
    }
    public void setCar(Car newCar) {
        if (this.car == newCar) return;
        if (this.car != null) {
            Car old = this.car;
            this.car = null;
            old.setPolicy(null);
        }
        this.car = newCar;
        if (newCar != null) {
            newCar.setPolicy(this);
        }
    }

    public BigDecimal calculateTotalPremium() {
        BigDecimal total = BigDecimal.ZERO;
        if (coverages.contains(InsuranceType.OC))total = total.add(premiumOc());
        if (coverages.contains(InsuranceType.AC))total = total.add(premiumAc());
        if (coverages.contains(InsuranceType.ASSISTANCE))total = total.add(premiumAssistance());
        if (coverages.contains(InsuranceType.NNW))total = total.add(premiumNnw());
        if (coverages.contains(InsuranceType.GAP))total = total.add(premiumGap());

        return total;
    }

    private BigDecimal premiumOc() {
        return ocLiabilityLimit.multiply(BigDecimal.valueOf(0.0005));
    }

    private BigDecimal premiumAc() {
        BigDecimal base = acSumInsured.multiply(BigDecimal.valueOf(0.04));
        if (acSumInsured.compareTo(BigDecimal.valueOf(150000)) > 0) {
            base = base.add(acSumInsured.multiply(BigDecimal.valueOf(0.015)));
        }
        return base;
    }

    private BigDecimal premiumAssistance() {
        BigDecimal baseFee = BigDecimal.valueOf(150);
        BigDecimal perKm = BigDecimal.valueOf(assistanceMaxTowingKm).multiply(BigDecimal.valueOf(0.5));
        return baseFee.add(perKm);
    }

    private BigDecimal premiumNnw() {
        return nnwSumPerPerson.multiply(BigDecimal.valueOf(0.002))
                .setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal premiumGap() {
        BigDecimal gapBase = BigDecimal.valueOf(4000);
        return gapBase.multiply(gapPercentage);
    }


    @Override
    public String toString() {
        return "InsurancePolicy{coverages=" + coverages + '}';
    }
}