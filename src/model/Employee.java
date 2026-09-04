package model;

import enums.TicketState;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Employee extends Person implements Serializable {
    private Branch branch;
    private double salary;
    private LocalDate hireDate;
    private List<ServiceTicket> assignedTickets = new ArrayList<>();

    public Employee(String name, String surname, LocalDate birthDate, LocalDate hireDate, Address address, Branch branch, double salary){
        this(name, null, surname, birthDate, hireDate, address, branch, salary);
    }

    public Employee(String name, String secondName, String surname, LocalDate birthDate, LocalDate hireDate, Address address, Branch branch, double salary){
        super(name, secondName, surname, birthDate, address);
        try {
            setHireDate(hireDate);
            setBranch(branch);
            setSalary(salary);
        }catch (Exception e){
            setBranch(null);
            removeFromExtent();
            throw e;
        }
    }

    //getters
    public Branch getBranch(){
        return branch;
    }
    public double getSalary(){
        return salary;
    }

    @Override
    public BigDecimal getDiscountRate() {
        int years = Period.between(hireDate, LocalDate.now()).getYears();
        if (years < 0) years = 0;
        BigDecimal baseDiscount = BigDecimal.valueOf(0.15);
        BigDecimal perYear = BigDecimal.valueOf(0.01);
        return baseDiscount.add(perYear.multiply(BigDecimal.valueOf(years)));
    }


    //setters
    public void setBranch(Branch newBranch) {
        if (this.branch == newBranch) return;
        if (this.branch != null) {
            this.branch.removeEmployee(this);
        }
        this.branch = newBranch;
        if (newBranch != null) {
            newBranch.addEmployee(this);
        }
    }

    public void setSalary(double salary){
        if(salary<0){
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        this.salary = salary;
    }
    public void setHireDate(LocalDate hireDate) {
        if (hireDate == null) {
            throw new IllegalArgumentException("Hire date is null");
        }else if(hireDate.isBefore(getBirthDate())){
            throw new IllegalArgumentException("Hire date cannot be before birth date");
        }
        this.hireDate = hireDate;
    }

    public void addServiceTicket(ServiceTicket serviceTicket) {
        if(serviceTicket == null){
            throw new IllegalArgumentException("Service ticket is null");
        }
        // zwarcie logiczne chroni przed wywołaniem getAssignedEmployee() w złym stanie
        if(serviceTicket.getState() != TicketState.IN_PROGRESS
                || serviceTicket.getAssignedEmployee() != this){
            throw new IllegalArgumentException("Zgłoszenie nie jest przypisane do tego pracownika — użyj ServiceTicket.takeTicket()");
        }
        if(assignedTickets.contains(serviceTicket)){
            return;
        }
        assignedTickets.add(serviceTicket);
    }
    public void removeServiceTicket(ServiceTicket serviceTicket) {
        if(serviceTicket == null){
            throw new IllegalArgumentException("Service ticket is null");
        }
        if(!assignedTickets.contains(serviceTicket)){
            return;
        }
        if(serviceTicket.getState() == TicketState.IN_PROGRESS
                && serviceTicket.getAssignedEmployee() == this){
            throw new IllegalStateException("Zgłoszenie wciąż przypisane do tego pracownika — użyj ServiceTicket.closeTicket()");
        }
        assignedTickets.remove(serviceTicket);
    }
    public List<ServiceTicket> getAssignedTickets() {
        return Collections.unmodifiableList(assignedTickets);
    }


    @Override
    public String toString() {
        return "Employee{" +
                "name='" + getName() + '\'' +
                ", surname='" + getSurname() + '\'' +
                ", secondName='" + getSecondName() + '\'' +
                ", birthDate=" + getBirthDate() +
                ", address=" + getAddress() +
                ", salary=" + salary +
                ", branch=" + (branch != null ? branch.getName() : "Brak") +
                '}';
    }


}