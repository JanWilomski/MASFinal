package model;

import enums.TicketState;
import enums.TicketPriority;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ServiceTicket extends ObjectPlus implements Serializable {

    private LocalDate creationDate;
    private String description;
    private Client client;// asocjacja dwukierunkowa
    private Car car;// asocjacja dwukierunkowa
    private TicketState state;

    //NEW
    private TicketPriority priority;

    //IN_PROGRESS
    private Employee assignedEmployee;   // asocjacja dwukierunkowa
    private LocalDate assignedDate;

    //CLOSED
    private LocalDate closedDate;
    private String solutionDescription;

    public ServiceTicket(String description, Client client, Car car, TicketPriority priority) {
        try {
            setDescription(description);
            setClient(client);
            setCar(car);
            this.state = TicketState.NEW;
            this.creationDate = LocalDate.now();
            setPriority(priority);

            client.addServiceTicket(this);   // druga strona asocjacji
            car.addServiceTicket(this);       // druga strona asocjacji
        } catch (Exception e) {
            removeFromExtent();
            throw e;
        }
    }


    private void requireState(TicketState required) {
        if (this.state != required) {
            throw new IllegalStateException(
                    "Operation requires state " + required + " but ticket is " + state);
        }
    }

    // getters
    public TicketState getState() { return state; }
    public LocalDate getCreationDate() { return creationDate; }
    public String getDescription() { return description; }
    public Client getClient() { return client; }
    public Car getCar() { return car; }


    public void setDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is null or blank");
        }
        this.description = description;
    }

    private void setClient(Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Client is null");
        }
        this.client = client;
    }

    private void setCar(Car car) {
        if (car == null) {
            throw new IllegalArgumentException("Car is null");
        }
        this.car = car;
    }

    private void setPriority(TicketPriority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Priority is null");
        }
        this.priority = priority;
    }

    //NEW
    public TicketPriority getPriority(){
        requireState(TicketState.NEW);
        return priority;
    }

    //IN_PROGRESS
    public Employee getAssignedEmployee(){
        requireState(TicketState.IN_PROGRESS);
        return assignedEmployee;
    }

    public LocalDate getAssignedDate(){
        requireState(TicketState.IN_PROGRESS);
        return assignedDate;
    }


    //CLOSED
    public LocalDate getClosedDate(){
        requireState(TicketState.CLOSED);
        return closedDate;
    }
    public String getSolutionDescription(){
        requireState(TicketState.CLOSED);
        return solutionDescription;
    }
    public long getResolutionDays(){
        requireState(TicketState.CLOSED);
        return ChronoUnit.DAYS.between(creationDate, closedDate);
    }

    private void setSolutionDescription(String solutionDescription){
        requireState(TicketState.CLOSED);
        if(solutionDescription == null || solutionDescription.isBlank()){
            throw new IllegalArgumentException("Solution description is null or blank");
        }
        this.solutionDescription = solutionDescription;
    }



    //STATE CHANGE
    //NEW -> IN_PROGRESS
    public void takeTicket(Employee employee){
        requireState(TicketState.NEW);
        if (employee == null) throw new IllegalArgumentException("Employee is null");
        this.priority = null;
        this.assignedEmployee = employee;
        this.assignedDate = LocalDate.now();
        this.state = TicketState.IN_PROGRESS;
        employee.addServiceTicket(this);
    }

    //IN_PROGRESS -> CLOSED
    public void closeTicket(String solution){
        requireState(TicketState.IN_PROGRESS);
        if (solution == null || solution.isBlank()) throw new IllegalArgumentException("Solution is null or blank");
        Employee old = this.assignedEmployee;
        this.assignedEmployee = null;
        this.assignedDate = null;
        if (old != null) old.removeServiceTicket(this);
        this.closedDate = LocalDate.now();
        this.state = TicketState.CLOSED;
        setSolutionDescription(solution);
    }

}