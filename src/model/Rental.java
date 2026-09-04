package model;

import enums.RentalStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class  Rental extends ObjectPlus implements Serializable {
    private LocalDate rentalStart;
    private LocalDate rentalEnd;
    private Car car;
    private Client client;
    private RentalStatus status;

    private List<Payment> payments = new ArrayList<>();


    public Rental(LocalDate rentalStart, LocalDate rentalEnd, Car car, Client client){
        try{
            setRentalStart(rentalStart);
            setRentalEnd(rentalEnd);
            setCar(car);
            setClient(client);
            checkAvailability(car, rentalStart, rentalEnd, null);
            this.status = RentalStatus.RESERVED;

            car.addRental(this);
            client.addRental(this);
        }catch (Exception e){
            delete();
            throw e;
        }
    }

    private static void checkAvailability(Car car, LocalDate rentalStart, LocalDate rentalEnd, Rental exclude){
        for (Rental existingRental : car.getRentals()) {
            if (existingRental == exclude) {
                continue;
            }
            if (existingRental.getStatus() == RentalStatus.CANCELLED) {
                continue;
            }
            LocalDate existingStart = existingRental.getRentalStart();
            LocalDate existingEnd = existingRental.getRentalEnd();

            boolean overlaps = !rentalStart.isAfter(existingEnd)
                    && !existingStart.isAfter(rentalEnd);

            if (overlaps) {
                throw new IllegalArgumentException(
                        "Samochód " + car.getBrand() + " " + car.getModel() +
                                " jest już zarezerwowany w tym okresie"
                );
            }
        }
    }

    public void pickUp(){
        if (this.status != RentalStatus.RESERVED) {
            throw new IllegalStateException("Only a RESERVED rental can be picked up");
        }
        this.status = RentalStatus.ACTIVE;
    }

    public void returnCar(){
        if (this.status != RentalStatus.ACTIVE) {
            throw new IllegalStateException("Only an ACTIVE rental can be returned");
        }
        this.status = RentalStatus.CLOSED;
    }

    public void cancel(){
        if (this.status != RentalStatus.RESERVED) {
            throw new IllegalStateException("Only a RESERVED rental can be cancelled");
        }
        this.status = RentalStatus.CANCELLED;
    }

    public RentalStatus getStatus(){
        return status;
    }


    public static ArrayList<Rental> findRentalsByDate(LocalDate date){
        ArrayList<Rental> rentals = new ArrayList<>(ObjectPlus.getExtentFromClass(Rental.class));
        ArrayList<Rental> activeRentals = new ArrayList<>();
        for(Rental rental : rentals){
            if(!date.isBefore(rental.getRentalStart()) && date.isBefore(rental.getRentalEnd())){
                activeRentals.add(rental);
            }
        }
        return activeRentals;
    }

    public void delete(){
        Car oldCar = this.car;
        Client oldClient = this.client;
        this.car = null;
        this.client = null;
        if(oldCar != null){
            oldCar.removeRental(this);
        }
        if(oldClient != null){
            oldClient.removeRental(this);
        }
        for (Payment payment : new ArrayList<>(payments)) {
            payment.delete();
        }
        removeFromExtent();
    }

    //gettery
    public LocalDate getRentalStart(){
        return rentalStart;
    }

    public LocalDate getRentalEnd(){
        return rentalEnd;
    }

    public Car getCar(){
        return car;
    }

    public Client getClient(){
        return client;
    }

    /**
     * Jedyny publiczny sposób zmiany terminu. Settery dat są prywatne, bo omijały
     * checkAvailability i pozwalały doprowadzić do nakładających się rezerwacji.
     */
    public void changePeriod(LocalDate newStart, LocalDate newEnd){
        if(status != RentalStatus.RESERVED){
            throw new IllegalStateException("Termin można zmienić tylko w stanie RESERVED");
        }
        if(newStart == null || newEnd == null){
            throw new IllegalArgumentException("Daty nie mogą być null");
        }
        if(!newEnd.isAfter(newStart)){
            throw new IllegalArgumentException("Data zakończenia musi być po dacie rozpoczęcia");
        }
        checkAvailability(car, newStart, newEnd, this);
        this.rentalStart = newStart;
        this.rentalEnd = newEnd;
    }

    //settery
    private void setRentalStart(LocalDate rentalStart){
        if(rentalStart==null){
            throw new IllegalArgumentException("Rental start date is null");
        }else if(this.rentalEnd!=null){
            if(rentalStart.isAfter(this.rentalEnd)){
                throw new IllegalArgumentException("Rental start date cannot be after rental end date");
            }
        }
        this.rentalStart = rentalStart;
    }

    private void setRentalEnd(LocalDate rentalEnd){
        if (rentalEnd == null) {
            throw new IllegalArgumentException("Rental end date is null");
        }
        if (this.rentalStart != null && rentalEnd.isBefore(this.rentalStart)) {
            throw new IllegalArgumentException("Rental end date cannot be before rental start date");
        }
        this.rentalEnd = rentalEnd;
    }

    private void setCar(Car car){
        if(car==null){
            throw new IllegalArgumentException("Car is null");
        }
        this.car = car;
    }

    private void setClient(Client client){
        if(client==null){
            throw new IllegalArgumentException("Client is null");
        }
        this.client = client;
    }

    //Payment
    public List<Payment> getPayments(){
        return Collections.unmodifiableList(payments);
    }

    public void addPayment(Payment payment){
        if(payment == null){
            throw new IllegalArgumentException("Payment is null");
        }
        if(payment.getRental() != this){
            throw new IllegalArgumentException("Płatność nie należy do tej rezerwacji — powiązanie tworzy konstruktor Payment");
        }
        if(payments.contains(payment)){
            return;
        }
        payments.add(payment);
    }

    public void removePayment(Payment payment){
        if(payment == null){
            throw new IllegalArgumentException("Payment is null");
        }
        if(!payments.contains(payment)){
            return;
        }
        if(payment.getRental() == this){
            throw new IllegalStateException("Płatność wciąż powiązana z tą rezerwacją — użyj Payment.delete()");
        }
        payments.remove(payment);
    }


    @Override
    public String toString(){
        return "Rental{" +
                "rentalStart=" + rentalStart +
                ", rentalEnd=" + rentalEnd +
                ", car=" + (car != null ? car.toString() : "Brak") +
                ", client=" + (client != null ? client.getSurname() : "Brak") +
                '}';
    }
}
