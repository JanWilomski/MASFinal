package model;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public abstract class Person extends ObjectPlus{
    private String name;
    private String surname;
    private String secondName;
    private LocalDate birthDate;
    private Address address;


    public abstract BigDecimal getDiscountRate();

    public Person(String name, String surname, LocalDate birthDate, Address address){
        this(name, null, surname, birthDate, address);
    }

    public Person(String name, String secondName, String surname, LocalDate birthDate, Address address){
        try {
            setSecondName(secondName);
            setName(name);
            setSurname(surname);
            setBirthDate(birthDate);
            setAddress(address);
        }catch (Exception e){
            removeFromExtent();
            throw e;
        }
    }


    public int getAge(){
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public Address getAddress(){
        return address;
    }

    public LocalDate getBirthDate(){
        return birthDate;
    }

    public String getSecondName(){
        return secondName;
    }

    public String getName(){
        return name;
    }

    public String getSurname(){
        return surname;
    }





    public void setAddress(Address address){
        if(address==null){
            throw new IllegalArgumentException("Address is null");
        }
        this.address = address;
    }

    public void setBirthDate(LocalDate birthDate){
        if(birthDate==null){
            throw new IllegalArgumentException("Birth date is null");
        }
        this.birthDate = birthDate;
    }

    public void setName(String name){
        if(name==null || name.isBlank()){
            throw new IllegalArgumentException("Name is null or empty");
        }
        this.name = name;
    }

    public void setSecondName(String secondName){
        if(secondName != null && secondName.isBlank()){
            throw new IllegalArgumentException("Second name cannot be blank");
        }
        this.secondName = secondName;
    }

    public void setSurname(String surname){
        if(surname == null || surname.isBlank()){
            throw new IllegalArgumentException("Surname is null or empty");
        }
        this.surname = surname;
    }


    @Override
    public String toString(){
        return "Person{" +
                    "name='" + name + '\'' +
                    ", surname='" + surname + '\'' +
                    ", secondName='" + secondName + '\'' +
                    ", birthDate=" + birthDate +
                    ", address=" + address +
                    '}';
    }

}
