package Model.Buildings;

import Model.Government;
import Model.Land;
import Model.Resources.Resource;

import java.util.ArrayList;

public abstract class Building {
    protected Resource resource;
    protected Government owner;
    protected String name;
    protected int xCoordinateLeft;
    protected int xCoordinateRight;
    protected int yCoordinateDown;
    protected int yCoordinateUp;
    protected ArrayList<String> lands;
    protected int hp;
    protected int numberOfResource;
    protected int cost;
    protected boolean canPass;

    // add each kind of building to users arraylist in database ! //TODO
    public Building(String name, int hp, Resource resource, int numberOfResource, int cost, boolean canPass) {
        this.name = name;
        this.hp = hp;
        this.resource = resource;
        this.numberOfResource = numberOfResource;
        this.cost = cost;
        this.canPass = canPass;
        lands = new ArrayList<>();
    }

    public void setCoordinate(int xCoordinateLeft, int xCoordinateRight, int yCoordinateDown, int yCoordinateUp) {
        this.xCoordinateLeft = xCoordinateLeft;
        this.xCoordinateRight = xCoordinateRight;
        this.yCoordinateDown = yCoordinateDown;
        this.yCoordinateUp = yCoordinateUp;
    }

    public int changeHP(int damage) {
        hp -= damage;
        return hp;
    }

    public Government getOwner() {
        return owner;
    }

    public void setOwner(Government owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public int getXCoordinateLeft() {
        return xCoordinateLeft;
    }

    public int getXCoordinateRight() {
        return xCoordinateRight;
    }

    public int getYCoordinateDown() {
        return yCoordinateDown;
    }

    public int getYCoordinateUp() {
        return yCoordinateUp;
    }

    public ArrayList<String> getLands() {
        return lands;
    }

    public void addLands(String  land) {
        this.lands.add(land);
    }

    public int getHp() {
        return hp;
    }

    public Resource getResource() {
        return resource;
    }

    public int getNumberOfResource() {
        return numberOfResource;
    }

    public int getCost() {
        return cost;
    }

    public boolean getCanPass() {
        return canPass;
    }
}
