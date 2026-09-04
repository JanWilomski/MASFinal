package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class Car extends ObjectPlus implements Serializable {
    private String brand;
    private String model;
    private String licensePlate;
    private int productionYear;
    private BigDecimal costPerDay;
    private Branch branch;

    private List<Rental> rentals = new ArrayList<>();
    private List<ServiceTicket> serviceTickets = new ArrayList<>();
    private Integer parkingSpot;

    private InsurancePolicy policy;


    public Car(String brand, String model, String licensePlate, int productionYear, Branch branch, Integer parkingSpot, BigDecimal costPerDay){
        try {
            setBrand(brand);
            setModel(model);
            setLicensePlate(licensePlate);
            setProductionYear(productionYear);
            setCostPerDay(costPerDay);
            assignBranchAndSpot(branch, parkingSpot);
        } catch (Exception e) {
            if (this.branch != null) this.branch.removeCar(this);
            removeFromExtent();
            throw e;
        }
    }

    private void assignBranchAndSpot(Branch branch, Integer parkingSpot){
        if (branch == null && parkingSpot != null) {
            throw new IllegalArgumentException("Cannot set parking spot for car without branch");
        }
        if (branch != null && parkingSpot == null) {
            throw new IllegalArgumentException("Car in branch must have parking spot");
        }
        if (parkingSpot != null && parkingSpot < 1) {
            throw new IllegalArgumentException("Parking spot must be >= 1");
        }
        this.parkingSpot = parkingSpot;
        setBranch(branch);
    }



    //gettery
    public String getBrand(){
        return brand;
    }

    public String getModel(){
        return model;
    }

    public String getLicensePlate(){
        return licensePlate;
    }

    public int getProductionYear(){
        return productionYear;
    }

    public List<Rental> getRentals(){
        return Collections.unmodifiableList(rentals);
    }

    public Branch getBranch(){
        return branch;
    }

    public Integer getParkingSpot() {
        return parkingSpot;
    }

    public BigDecimal getCostPerDay() {
        return costPerDay;
    }




    //settery
    public void setBrand(String brand){
        if(brand==null || brand.isBlank()){
            throw new IllegalArgumentException("Brand is null or blank");
        }
        this.brand = brand;
    }

    public void setModel(String model){
        if(model==null){
            throw new IllegalArgumentException("Model is null");
        }
        this.model = model;
    }

    public void setLicensePlate(String licensePlate){
        if(licensePlate==null || licensePlate.isBlank()){
            throw new IllegalArgumentException("License plate is null or blank");
        }
        this.licensePlate = licensePlate;
    }

    public void setProductionYear(int productionYear){
        if(productionYear<1900 || productionYear>LocalDate.now().getYear()){
            throw new IllegalArgumentException("Production year is invalid");
        }
        this.productionYear = productionYear;
    }

    public void setCostPerDay(BigDecimal costPerDay){
        if(costPerDay == null || costPerDay.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Cost per day must be positive");
        }
        this.costPerDay = costPerDay;
    }

    public void setBranch(Branch newBranch){
        if (this.branch == newBranch) {
            return;
        }
        if (this.branch != null) {
            this.branch.removeCar(this);    // potrzebuje jeszcze starego parkingSpot do odnalezienia wpisu
        }
        this.branch = newBranch;
        if(newBranch != null){
            newBranch.addCar(this);
        } else {
            this.parkingSpot = null;        // auto bez oddziału nie ma miejsca parkingowego
        }
    }


    public void addRental(Rental rental){
        if(rental == null){
            throw new IllegalArgumentException("Rental is null");
        }
        if(rental.getCar() != this){
            throw new IllegalArgumentException("Rezerwacja nie należy do tego samochodu — powiązanie tworzy konstruktor Rental");
        }
        if(rentals.contains(rental)){
            return;
        }
        rentals.add(rental);
    }

    public void removeRental(Rental rental){
        if(rental == null){
            throw new IllegalArgumentException("Rental is null");
        }
        if(!rentals.contains(rental)){
            return;
        }
        if(rental.getCar() == this){
            throw new IllegalStateException("Rezerwacja wciąż powiązana z tym samochodem — użyj Rental.delete()");
        }
        rentals.remove(rental);
    }

    public List<ServiceTicket> getServiceTickets(){
        return Collections.unmodifiableList(serviceTickets);
    }

    public void addServiceTicket(ServiceTicket serviceTicket){
        if(serviceTicket == null){
            throw new IllegalArgumentException("Service ticket is null");
        }
        if(serviceTicket.getCar() != this){
            throw new IllegalArgumentException("Zgłoszenie nie dotyczy tego samochodu — powiązanie tworzy konstruktor ServiceTicket");
        }
        if(serviceTickets.contains(serviceTicket)){
            return;
        }
        serviceTickets.add(serviceTicket);
    }

    public void removeServiceTicket(ServiceTicket serviceTicket){
        if(serviceTicket == null){
            throw new IllegalArgumentException("Service ticket is null");
        }
        if(!serviceTickets.contains(serviceTicket)){
            return;
        }
        if(serviceTicket.getCar() == this){
            throw new IllegalStateException("Zgłoszenie wciąż dotyczy tego samochodu — asocjacja jest obowiązkowa i nie da się jej rozpiąć");
        }
        serviceTickets.remove(serviceTicket);
    }

    public void setParkingSpot(Integer newSpot) {
        if (this.branch != null && newSpot == null) {
            throw new IllegalArgumentException("Car in branch must have parking spot");
        }
        if (this.branch == null && newSpot != null) {
            throw new IllegalArgumentException("Cannot set parking spot for car without branch");
        }
        if (newSpot != null && newSpot < 1) {
            throw new IllegalArgumentException("Parking spot must be >= 1");
        }
        if (this.parkingSpot == null && newSpot == null) return;
        if (this.parkingSpot != null && this.parkingSpot.equals(newSpot)) return;

        // kolizję sprawdzamy PRZED mutacją, żeby syncCarSpot już nie mógł rzucić
        if (this.branch != null && newSpot != null) {
            Car occupant = this.branch.getCarAtSpot(newSpot);
            if (occupant != null && occupant != this) {
                throw new IllegalArgumentException("Spot " + newSpot + " is already taken");
            }
        }
        this.parkingSpot = newSpot;
        if (this.branch != null) {
            this.branch.syncCarSpot(this);
        }
    }

    // Car
    public void setPolicy(InsurancePolicy newPolicy) {
        if (this.policy == newPolicy) return;
        if (this.policy != null) {
            InsurancePolicy old = this.policy;
            this.policy = null;
            old.setCar(null);
        }
        this.policy = newPolicy;
        if (newPolicy != null) {
            newPolicy.setCar(this);
        }
    }

    @Override
    public String toString(){
        return "Car{" +
                "brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", licensePlate='" + licensePlate + '\'' +
                ", productionYear=" + productionYear +
                '}';
    }



}
