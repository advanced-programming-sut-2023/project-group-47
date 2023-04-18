package Model.Citizens;

import Model.Buildings.Building;
import Model.User;

public class Worker extends Character {

    private Building workingPlace;

    public Building getWorkingPlace() {
        return workingPlace;
    }

    public void setWorkingPlace(Building workingPlace) {
        this.workingPlace = workingPlace;
    }

    public double getEfficiency() {
        //regarding popularity and fear
    }

    public Worker(int walkingSpeed, String name, User owningPlayer, Building workingPlace) {
        super(name, owningPlayer);
        this.workingPlace = workingPlace;
        setBasicWorkerFightingStats();
    }

    private void setBasicWorkerFightingStats() {

    }

    public static Worker makeWorkerForBuildingByName(String buildingName) {

    }
}