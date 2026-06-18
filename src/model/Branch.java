package model;

import java.io.Serializable;
import java.util.*;

public class Branch extends ObjectPlus implements Serializable {
    private String name;
    private List<Employee> employees = new ArrayList<>();
    private Map<Integer, Car> cars = new HashMap<>();

    public Branch(String name){
        try{
            setName(name);
        }catch (Exception e){
            removeFromExtent();
            throw e;
        }
    }

    //setters
    public void setName(String name){
        if(name==null || name.isBlank()){
            throw new IllegalArgumentException("Name is null or empty");
        }
        this.name = name;
    }



    //getters
    public List<Employee> getEmployees(){
        return Collections.unmodifiableList(employees);
    }

    public String getName() {
        return name;
    }

    public Map<Integer, Car> getCars() {
        return Collections.unmodifiableMap(cars);
    }

    public void addCar(Car car){
        if(car == null){
            throw new IllegalArgumentException("Car is null");
        }
        Integer spot = car.getParkingSpot();
        if (spot == null){
            throw new IllegalArgumentException("Car must have parking spot");
        }
        Car existing = cars.get(spot);
        if (existing == car) {
            return;
        }
        if (existing != null) {
            throw new IllegalArgumentException("Car with parking spot " + car.getParkingSpot() + " already exists in this branch");
        }

        cars.put(spot, car);
        car.setBranch(this);
    }
    public void removeCar(Car car) {
        if (car == null) {
            throw new IllegalArgumentException("Car is null");
        }
        Integer spot = car.getParkingSpot();
        if (spot == null || !cars.containsKey(spot) || cars.get(spot) != car) {
            return;
        }
        cars.remove(spot);
        car.setBranch(null);
    }

    public void delete() {
        for (Car car : new ArrayList<>(cars.values())) {
            car.setBranch(null);
        }

        for (Employee emp : new ArrayList<>(employees)) {
            emp.setBranch(null);
        }

        removeFromExtent();
    }

    public void removeEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee is null");
        }
        if (!employees.contains(employee)) {
            return;
        }
        employees.remove(employee);
        employee.setBranch(null);
    }

    public void addEmployee(Employee employee) {
        if(employee == null) {
            throw new IllegalArgumentException("Employee is null");
        }else if(employees.contains(employee)) {
            return;
        }
        employees.add(employee);
        employee.setBranch(this);
    }

    public void updateCarParkingSpot(Integer newSpot, Car car) {
        if (car == null) {
            throw new IllegalArgumentException("Car is null");
        }
        Integer oldSpot = car.getParkingSpot();
        if (newSpot != null && cars.containsKey(newSpot) && cars.get(newSpot) != car) {
            throw new IllegalArgumentException("Spot " + newSpot + " is already taken");
        }
        if (oldSpot != null) {
            cars.remove(oldSpot);
        }
        if (newSpot != null) {
            cars.put(newSpot, car);
        }
    }

    public Car getCarAtSpot(int spotNumber) {
        if (spotNumber < 1) {
            throw new IllegalArgumentException("Spot must be bigger than 1");
        }
        return cars.get(spotNumber);
    }

    @Override
    public String toString(){
        return "Branch{" +
                "name='" + name + '\'' +
                ", employees=" + employees +
                '}';
    }
}
