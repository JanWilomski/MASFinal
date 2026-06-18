package model;

import java.io.Serializable;

public class Address implements Serializable {

    private String streetName;
    private String city;
    private String postalCode;

    public Address(String streetName, String city, String postalCode){
        setStreetName(streetName);
        setCity(city);
        setPostalCode(postalCode);
    }

    public String getStreetName(){
        return streetName;
    }

    public String getCity(){
        return city;
    }

    public String getPostalCode(){
        return postalCode;
    }


    //settery
    public void setStreetName(String streetName){
        if(streetName==null || streetName.isBlank()){
            throw new IllegalArgumentException("Street name is null or blank");
        }
        this.streetName = streetName;
    }

    public void setCity(String city){
        if(city==null || city.isBlank()){
            throw new IllegalArgumentException("City is null or blank");
        }
        this.city = city;
    }

    public void setPostalCode(String postalCode){
        if(postalCode==null || postalCode.isBlank()){
            throw new IllegalArgumentException("Postal code is null or blank");
        }
        this.postalCode = postalCode;
    }


    @Override
    public String toString(){
        return "Adres{" +
                "streetName='" + streetName + '\'' +
                ", city='" + city + '\'' +
                ", postalCode='" + postalCode + '\'' +
                '}';
    }
}
