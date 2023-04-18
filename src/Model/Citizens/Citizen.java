package Model.Citizens;


import Model.User;

public class Citizen {
    private int walkingSpeed;
    private String name;
    private User owner;
    
    public Citizen(String name, User owner) {
        this.name = name;
        this.owner= owner;
    }

    public void setWalkingSpeed(int walkingSpeed) {
        this.walkingSpeed = walkingSpeed;
    }

    public int getWalkingSpeed() {
        return walkingSpeed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getOwner() {
        return owner;
    }

    public static Citizen makeCitizen() {

    }

    private void setPeasantDefaultWalkingSpeed() {

    }
}
