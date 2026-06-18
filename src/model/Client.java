package model;

import enums.DrivingLicenseCategory;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Client extends Person implements Serializable {

    private static int minAge = 18;

    private List<DrivingLicenseCategory> drivingLicenseCategories; //atrybut powtarzalny
    private List<Rental> rentals = new ArrayList<>();
    private List<ServiceTicket> assignedTickets = new ArrayList<>();

    private List<Payment> payments = new ArrayList<>();

    public Client(String name, String surname, LocalDate birthDate, Address address, List<DrivingLicenseCategory> categories){
        this(name, null, surname, birthDate, address, categories);
    }

    public Client(String name, String secondName, String surname, LocalDate birthDate, Address address, List<DrivingLicenseCategory> categories){
        super(name, secondName, surname, birthDate, address);
        try{
            setDrivingLicenseCategories(categories);
        }catch (Exception e){
            removeFromExtent();
            throw e;
        }
    }


    public void addDrivingLicenseCategory(DrivingLicenseCategory drivingLicenseCategory){
        if(drivingLicenseCategory==null){
            throw new IllegalArgumentException("Category is null");
        }else if(drivingLicenseCategories.contains(drivingLicenseCategory)){
            throw new IllegalArgumentException("Client already has this category");
        }
        drivingLicenseCategories.add(drivingLicenseCategory);
    }

    public void removeDrivingLicenseCategory(DrivingLicenseCategory drivingLicenseCategory){
        if(drivingLicenseCategories.size()==1){
            throw new IllegalArgumentException("Client must have at least one driver's license category");
        }else if(!drivingLicenseCategories.contains(drivingLicenseCategory)){
            throw new IllegalArgumentException("Client doesn't have this category in the system");
        }
        drivingLicenseCategories.remove(drivingLicenseCategory);
    }



    public List<DrivingLicenseCategory> getDrivingLicenseCategories(){
        return Collections.unmodifiableList(drivingLicenseCategories);
    }

    public static int getMinAge(){
        return minAge;
    }

    public List<Rental> getRentals(){
        return Collections.unmodifiableList(rentals);
    }


    //setters

    public void setDrivingLicenseCategories(List<DrivingLicenseCategory> categories){
        if(categories == null||categories.isEmpty()){
            throw new IllegalArgumentException("Driving license categories list is null or empty");
        }else if(categories.size() != categories.stream().distinct().count()) {
            throw new IllegalArgumentException("Duplicate categories");
        }
        this.drivingLicenseCategories = new ArrayList<>(categories);
    }

    public static void setMinAge(int age){
        if (age < 18){
            throw new IllegalArgumentException("Minimal client age cannot be below 18");
        }
        minAge = age;
    }

    public void addRental(Rental rental){
        if(rental == null){
            throw new IllegalArgumentException("Rental is null");
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
        rentals.remove(rental);
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeInt(minAge);
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        minAge = ois.readInt();
    }

    @Override
    public BigDecimal getDiscountRate() {
        int completed = 0;
        for (Rental r : getRentals()) {
            if (r.getRentalEnd().isBefore(LocalDate.now())) {
                completed++;
            }
        }
        if (completed > 40) {
            completed = 40;
        }
        return BigDecimal.valueOf(0.01).multiply(BigDecimal.valueOf(completed));
    }



    @Override
    public String toString() {
        return "Client{" +
                "name='" + getName() + '\'' +
                ", surname='" + getSurname() + '\'' +
                ", secondName='" + getSecondName() + '\'' +
                ", birthDate=" + getBirthDate() +
                ", address=" + getAddress() +
                ", drivingLicenseCategories=" + drivingLicenseCategories +
                '}';
    }

    public void addServiceTicket(ServiceTicket serviceTicket) {
        if(serviceTicket == null){
            throw new IllegalArgumentException("Service ticket is null");
        }
        if(assignedTickets.contains(serviceTicket)){
            return;
        }
        assignedTickets.add(serviceTicket);
    }

    public List<ServiceTicket> getAssignedTickets() {
        return Collections.unmodifiableList(assignedTickets);
    }
}
