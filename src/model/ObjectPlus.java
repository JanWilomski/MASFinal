package model;

import java.io.*;
import java.util.*;

public abstract class ObjectPlus implements Serializable {
    private static Map<Class, List> extent = new HashMap<>();

    public ObjectPlus() {
        addToExtent();
    }

    protected void addToExtent(){
        List list = extent.computeIfAbsent(this.getClass(), k -> new ArrayList<>());
        list.add(this);
    }

    protected void removeFromExtent(){
        List list = extent.get(this.getClass());
        if(list != null) {
            list.remove(this);
        }
    }

    public static <T> List<T> getExtentFromClass(Class<T> c){
        extent.computeIfAbsent(c, k -> new ArrayList<>());
        return Collections.unmodifiableList(extent.get(c));
    }

    public static void saveExtent() throws IOException {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("extent"))) {
            oos.writeObject(extent);
        }
    }

    public static void loadExtent() throws IOException, ClassNotFoundException {
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream("extent"))) {
            extent = (Map<Class, List>) ois.readObject();
        }
    }

    public static void showExtent(Class c){
        List list = extent.get(c);
        if(list == null || list.isEmpty()){
            System.out.println("No instances of class " + c.getName() + " found.");
        } else {
            System.out.println("Extent of class " + c.getName() + ":");
            for(Object obj : list){
                System.out.println(obj);
            }
        }
    }
}
