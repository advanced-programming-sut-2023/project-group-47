package Controller;

import java.util.ArrayList;

import Model.*;
import Model.Buildings.Defence;
import Model.Buildings.*;
import Model.Units.Troop;
import Model.Units.*;
import View.Enums.Messages.GameMenuMessages;

import java.util.HashMap;
import java.util.Objects;

public class GameMenuController {
    private static Government currentGovernment;
    private static int turnsPassed = 0;
    private static Building selectedBuilding = null;
    private static Map currentMap;
    private static ArrayList<Building> allBuildings;
    private static ArrayList<Unit> allUnits;
    private static ArrayList<ArrayList<Square>> allWays;
    private static ArrayList<Square> squares;
    private static HashMap<Square, String> buildSiege;

    static {
        buildSiege = new HashMap<>();
        allWays = new ArrayList<>();
        squares = new ArrayList<>();
        allUnits = new ArrayList<>();
        allBuildings = new ArrayList<>();
    }

    public static GameMenuMessages nextTurnController() {
        //remove tent and add sieges
        for (HashMap.Entry<Square, String> entry : buildSiege.entrySet()) {
            Siege siege = Siege.createSiege(currentGovernment, entry.getValue(), entry.getKey().getX(), entry.getKey().getY());
            for (int i = 0; i < siege.getEngineerNeed(); i++) {
                entry.getKey().removeUnit(Engineer.createEngineer(currentGovernment, -1, -1));
            }
            entry.getKey().getBuilding().changeHP(-10000);
            DataBase.removeDestroyedBuildings(entry.getKey().getBuilding());
        }
        if (checkForEnd())
            return GameMenuMessages.END;
        //automatic fights
        ArrayList<Government> governments = DataBase.getGovernments();
        int index = governments.indexOf(currentGovernment);
        if (index == governments.size() - 1)
            DataBase.handleEndOfTurnFights();
        //change government
        if (index == governments.size() - 1)
            currentGovernment = governments.get(0);
        else
            currentGovernment = governments.get(index + 1);

        DataBase.setCurrentGovernment(currentGovernment);
        selectedBuilding = null;
        //work with government
        currentGovernment.addAndRemoveFromGovernment();
        DataBase.generateResourcesForCurrentGovernment();
        DataBase.setSelectedUnit(new ArrayList<>());

        allWays = new ArrayList<>();
        squares = new ArrayList<>();
        buildSiege = new HashMap<>();

        if (checkForEnd())
            return GameMenuMessages.END;
        return GameMenuMessages.SUCCESS;
    }

    public static boolean checkForEnd() {
        ArrayList<Government> governments = DataBase.getGovernments();
        Square[][] allSquares = DataBase.getSelectedMap().getSquares();
        for (int i = 0; i < governments.size(); i++) {
            if (governments.get(i).getLord().getHitPoint() <= 0) {
                //destroy every thing for this government
                for (int j = 0; j < allSquares.length; j++) {
                    for (int k = 0; k < allSquares[0].length; k++) {
                        if (allSquares[j][k].getBuilding() != null
                                && allSquares[j][k].getBuilding().getOwner().equals(governments.get(i))) {
                            allSquares[j][k].getBuilding().changeHP(-100000);
                            DataBase.removeDestroyedBuildings(allSquares[j][k].getBuilding());
                        }
                        for (int l = 0; l < allSquares[j][k].getUnits().size(); l++) {
                            if (allSquares[j][k].getUnits().get(l).getOwner().equals(governments.get(i))) {
                                allSquares[j][k].removeUnit(allSquares[j][k].getUnits().get(l));
                                l--;
                            }
                        }
                    }
                }
                governments.remove(i);
                i--;
            }
        }
        allUnits = new ArrayList<>();
        for (int j = 0; j < allSquares.length; j++) {
            for (int k = 0; k < allSquares[0].length; k++) {
                for (Unit unit : allSquares[j][k].getUnits()) {
                    allUnits.add(unit);
                    unit.setDidFight(false);
                }
            }
        }
        //if both lord die in one turn !!!
        if (governments.size() == 1) return true;
        return false;
    }

    public static void setCurrentGovernment(Government government) {
        currentGovernment = government;
    }

    public static GameMenuMessages userLogout() {
        return null;
    }

    public static void addToGameBuildings(Building building) {
        allBuildings.add(building);
    }

    public static GameMenuMessages putBuildingController(String x, String y, String buildingType) {
        if (!Orders.isInputInteger(x) || !Orders.isInputInteger(y))
            return GameMenuMessages.WRONG_FORMAT_COORDINATE;

        int xInNum = Integer.parseInt(x);
        int yInNum = Integer.parseInt(y);
        Building targetBuilding = getBuildingByName(buildingType);

        if (!currentMap.isCoordinationValid(xInNum, yInNum))
            return GameMenuMessages.INVALID_COORDINATE;

        if (targetBuilding == null)
            return GameMenuMessages.DROPBUILDING_INVALID_BUILDINGNAME;

        if (!currentMap.canConstructBuildingInPlace(targetBuilding, xInNum, yInNum))
            return GameMenuMessages.DROPBUILDING_INVALID_PLACE;

        int buildingCost = targetBuilding.getCost();
        if (buildingCost > currentGovernment.getMoney())
            return GameMenuMessages.INSUFFICIENT_GOLD;

        int resourceCount = targetBuilding.getNumberOfResource();
        if (targetBuilding.getResource() != null)
            if (currentGovernment.getResourceInStockpiles(targetBuilding.getResource()) < resourceCount)
                return GameMenuMessages.INSUFFICIENT_RESOURCES;
        if (targetBuilding instanceof Generator && ((Generator) targetBuilding).getNumberOfWorker() > currentGovernment.getFreeWorker())
            return GameMenuMessages.NOT_ENOUGH_FREE_WORKER;

        currentGovernment.changeMoney(-buildingCost);

        if (targetBuilding.getResource() != null)
            currentGovernment.removeFromStockpile(targetBuilding.getResource(), resourceCount);

        targetBuilding = constructBuildingForPlayer(buildingType, xInNum, yInNum);
        currentMap.constructBuilding(targetBuilding, xInNum, yInNum);

        return GameMenuMessages.SUCCESS;
    }

    public static Building constructBuildingForPlayer(String buildingName, int x, int y) {
        String buildingCategory = Building.getBuildingCategoryByName(buildingName);
        switch (buildingCategory) {
            case "Generator" -> {
                Generator generator = Generator.createGenerator(currentGovernment, x, y, buildingName);
                currentGovernment.changePopulation(generator.getNumberOfWorker());
                currentGovernment.changeFreeWorkers(-generator.getNumberOfWorker());
                currentGovernment.addToGenerationRate(generator.getResourceGenerate().getName(), generator.getGeneratingRate());
                currentGovernment.applyOxEffectOnStoneGeneration();
                return generator;
            }
            case "TownBuilding" -> {
                TownBuilding townBuilding = TownBuilding.createTownBuilding(currentGovernment, x, y, buildingName);
                currentGovernment.addToMaxPopulation(townBuilding.getCapacity());
                currentGovernment.updateBuildingPopularity();
                return townBuilding;
            }
            case "Stockpile" -> {
                return Stockpile.createStockpile(currentGovernment, x, y, buildingName);
            }
            case "Barrack" -> {
                return Barrack.createBarrack(currentGovernment, x, y, buildingName);
            }
            default -> {
                return Defence.createDefence(currentGovernment, x, y, buildingName);
            }
        }
    }

    public static GameMenuMessages selectBuildingController(String x, String y) {

        if (!Orders.isInputInteger(x) || !Orders.isInputInteger(y))
            return GameMenuMessages.WRONG_FORMAT_COORDINATE;

        int xInNum = Integer.parseInt(x);
        int yInNum = Integer.parseInt(y);

        if (!currentMap.isCoordinationValid(xInNum, yInNum))
            return GameMenuMessages.INVALID_COORDINATE;

        Building targetBuilding = currentMap.getSquareFromMap(xInNum, yInNum).getBuilding();
        if (targetBuilding == null)
            return GameMenuMessages.SELECTBUILDING_EMPTY_SQUARE;

        if (!targetBuilding.getOwner().getOwner().getUsername().equals(currentGovernment.getOwner().getUsername()))
            return GameMenuMessages.SELECTBUILDING_UNOWNED_BUILDING;

        selectedBuilding = targetBuilding;
        return GameMenuMessages.SUCCESS;
    }

    public static GameMenuMessages createUnitController(String type, String count) {
        if (!(selectedBuilding instanceof Barrack))
            return GameMenuMessages.CREATE_UNIT_WRONG_SELECTED_BUILDING;

        if (!Orders.isInputInteger(count))
            return GameMenuMessages.CREATEUNIT_WRONG_NUMBERFORMAT;

        int countInNum = Integer.parseInt(count);
        if (countInNum <= 0)
            return GameMenuMessages.CREATEUNIT_WRONG_NUMBERFORMAT;

        Barrack selectedBarrack = (Barrack) selectedBuilding;
        if (!selectedBarrack.canBuildTroopByName(type))
            return GameMenuMessages.CREATEUNIT_UNMATCHING_BARRACK;

        Troop targetTroop = Troop.getTroopByName(type);

        int totalCost = targetTroop.getCost() * countInNum;
        if (currentGovernment.getMoney() < totalCost)
            return GameMenuMessages.INSUFFICIENT_GOLD;

        if (!doesHaveUnitsWeapons(countInNum, targetTroop))
            return GameMenuMessages.INSUFFICIENT_RESOURCES;

        if (currentGovernment.getFreeWorker() < countInNum)
            return GameMenuMessages.CREATEUNIT_INSUFFICIENT_FREEPOP;

        trainTroopsForGovernment(countInNum, targetTroop, selectedBarrack);
        return GameMenuMessages.SUCCESS;
    }

    private static boolean doesHaveUnitsWeapons(int count, Troop targetTroop) {
        ArrayList<Resource> neededWeapons = new ArrayList<Resource>(targetTroop.getWeapons());

        for (Resource resource : neededWeapons) {
            resource.changeCount(count - resource.getCount());
        }

        for (Resource resource : neededWeapons) {
            if (currentGovernment.getResourceInStockpiles(resource) < resource.getCount())
                return false;
        }

        return true;
    }

    private static void trainTroopsForGovernment(int count, Troop targetTroop, Barrack selectedBarrack) {

        int barrackX = selectedBarrack.getXCoordinateLeft();
        int barrackY = selectedBarrack.getYCoordinateUp();

        currentGovernment.changeMoney(-count * targetTroop.getCost());

        for (Resource weapon : targetTroop.getWeapons()) {
            currentGovernment.removeFromStockpile(weapon, count);
        }

        for (int i = 0; i < count; i++) {
            Troop newTroop = Troop.createTroop(currentGovernment, targetTroop.getName(), barrackX, barrackY);
            addToAllUnits(newTroop);
        }

        currentGovernment.changeFreeWorkers(-count);
    }

    public static GameMenuMessages repairController() {

        if (selectedBuilding == null)
            return GameMenuMessages.EMPTY_INPUT_FIELDS_ERROR;

        if (!(selectedBuilding instanceof Defence))
            return GameMenuMessages.REPAIR_UNREPAIRABLE_SELECTED_BUILDING;

        int hpToRecover = selectedBuilding.getMaximumHp() - selectedBuilding.getHp();
        int stepsToRepair = hpToRecover / 50 + 1;
        Resource stone = Resource.getResourceByName("Stone");

        for (int i = 0; i < stepsToRepair + 1; i++) {
            if (currentGovernment.getResourceInStockpiles(stone) < 5)
                return GameMenuMessages.INSUFFICIENT_RESOURCES;

            currentGovernment.removeFromStockpile(stone, 5);

            if (selectedBuilding.changeHP(-50) > selectedBuilding.getMaximumHp()) {
                selectedBuilding.changeHP(selectedBuilding.getHp() - selectedBuilding.getMaximumHp());
                break;
            }
        }
        return GameMenuMessages.SUCCESS;
    }

    public static GameMenuMessages selectUnitController(String option) {
        String x = Orders.findFlagOption("-x", option);
        String y = Orders.findFlagOption("-y", option);
        String type = Orders.findFlagOption("-type", option);

        assert x != null;
        if (!x.matches("^\\d+$") || !Objects.requireNonNull(y).matches("^\\d+$"))
            return GameMenuMessages.WRONG_FORMAT_COORDINATE;

        if (!Unit.getAllUnits().contains(type))
            return GameMenuMessages.INVALID_TROOP_TYPE;

        int xCoordinate = Integer.parseInt(x);
        int yCoordinate = Integer.parseInt(y);

        if (DataBase.getSelectedMap().getLength() < xCoordinate
                || DataBase.getSelectedMap().getWidth() < yCoordinate)
            return GameMenuMessages.INVALID_COORDINATE;

        Square square = DataBase.getSelectedMap().getSquareFromMap(yCoordinate, xCoordinate);
        ArrayList<Unit> selectedUnit = new ArrayList<>();

        for (Unit unit : square.getUnits()) {
            if (unit.getName().equals(type) && unit.getOwner().equals(currentGovernment)) {
                selectedUnit.add(unit);
            }
        }

        if (selectedUnit.size() == 0) return GameMenuMessages.THERE_IS_NO_UNIT;
        DataBase.setSelectedUnit(selectedUnit);
        return GameMenuMessages.SUCCESS;
    }

    public static GameMenuMessages moveUnitController(String coordinate) {
        String x = Orders.findFlagOption("-x", coordinate);
        String y = Orders.findFlagOption("-y", coordinate);
        assert x != null;
        if (!x.matches("^\\d+$") || !Objects.requireNonNull(y).matches("^\\d+$"))
            return GameMenuMessages.WRONG_FORMAT_COORDINATE;
        int xCoordinate = Integer.parseInt(x);
        int yCoordinate = Integer.parseInt(y);
        if (DataBase.getSelectedMap().getLength() <= xCoordinate || DataBase.getSelectedMap().getWidth() <= yCoordinate
                || xCoordinate < 0 || yCoordinate < 0)
            return GameMenuMessages.INVALID_COORDINATE;

        if (DataBase.getSelectedUnit().size() == 0) return GameMenuMessages.CHOSE_UNIT_FIRST;

        if (!DataBase.getSelectedMap().getSquareFromMap(xCoordinate, yCoordinate).canPass())
            return GameMenuMessages.CANT_GO_THERE;

        if (moveUnit(xCoordinate, yCoordinate)) return GameMenuMessages.SUCCESS;
        else return GameMenuMessages.CANT_GO_THERE;
    }

    private static boolean moveUnit(int x, int y) {
        ArrayList<Unit> unit = DataBase.getSelectedUnit();
        Map map = DataBase.getSelectedMap();

        int speed = unit.get(0).getMoveLeft();
        int firstX = unit.get(0).getXCoordinate();
        int firstY = unit.get(0).getYCoordinate();
        boolean up = false;

        squares = new ArrayList<>();
        allWays = new ArrayList<>();
        if (move(unit.get(0), map, firstX, firstY, x, y, speed, up)) {
            int size = 1000;
            int index = 0;
            int i = 0;
            for (ArrayList<Square> array : allWays) {
                if (array.size() < size) {
                    index = i;
                    size = array.size();
                }
                i++;
            }

            for (Square square : allWays.get(index)) {
                if (unit.size() != 0 && square.getBuilding().getName().equals("Trap")) {
                    unit.remove(0);
                    square.setBuilding(null);
                }
            }
            boolean check = false;
            for (Unit unitSelected : unit) {
                if (unitSelected.getMoveLeft() >= size) {
                    unitSelected.setMoveLeft(speed - size);
                    unitSelected.setCoordinate(x, y);
                    check = true;
                }
            }
            return check;
        } else return false;
    }

    private static boolean move(Unit unit, Map map, int x, int y, int xFin, int yFin, int speed, boolean up) {
        //conditions
        if (x < 0 || y < 0 || x >= map.getLength() || y >= map.getWidth()) return false;
        if (!map.getSquareFromMap(y, x).canPass()) return false;
        if (map.getSquareFromMap(y, x).getBuilding() != null) {
            if (unit instanceof Siege || unit.getName().equals("Knight") || unit.getName().equals("HorseArcher")) {
                if (!map.getSquareFromMap(y, x).getBuilding().getCanPass(up)
                        || map.getSquareFromMap(y, x).getBuilding().getName().equals("Stair"))
                    return false;
            } else if (!unit.getName().equals("Assassin")) {
                if (!(map.getSquareFromMap(y, x).getBuilding() instanceof Defence
                        || map.getSquareFromMap(y, x).getBuilding().getCanPass(up)))
                    return false;

                LadderMan ladderMan = LadderMan.createLadderMan(DataBase.getCurrentGovernment(), -1, -1);
                if (map.getSquareFromMap(y, x).getBuilding().getName().equals("Stair")) up = !up;
                else if (map.getSquareFromMap(y, x).getUnits().contains(ladderMan)
                        && unit instanceof Troop && ((Troop) unit).isClimbLadder()) up = !up;

                if (up && !(map.getSquareFromMap(y, x).getBuilding() instanceof Defence)) return false;

                if (!up && (map.getSquareFromMap(y, x).getBuilding() instanceof Defence
                        && !map.getSquareFromMap(y, x).getBuilding().getCanPass(false)))
                    return false;
            }
        }
        if (speed >= 0 && x == xFin && y == yFin) {
            if (map.getSquareFromMap(y, x).getBuilding() != null
                    && map.getSquareFromMap(y, x).getBuilding() instanceof Defence
                    && map.getSquareFromMap(y, x).getUnits().size() >= ((Defence) map.getSquareFromMap(y, x).getBuilding()).getCapacity())
                return false;
//            if (map.getSquareFromMap(x,y).getBuilding() != null
//                    && map.getSquareFromMap(x,y).getBuilding().getCanPass(up))
//                return false;

            allWays.add(squares);
            return true;
        }
        if (speed == 0) return false;

        if (x <= map.getLength() - 1) {
            squares.add(map.getSquareFromMap(y, x+1));
            move(unit, map, x + 1, y, xFin, yFin, speed - 1, up);
            squares.remove(map.getSquareFromMap(y, x + 1));
        }
        if (x > 1) {
            squares.add(map.getSquareFromMap(y, x - 1));
            move(unit, map, x - 1, y, xFin, yFin, speed - 1, up);
            squares.remove(map.getSquareFromMap(y, x - 1));
        }
        if (y <= map.getWidth() - 1) {
            squares.add(map.getSquareFromMap(y + 1, x));
            move(unit, map, x, y + 1, xFin, yFin, speed - 1, up);
            squares.remove(map.getSquareFromMap(y + 1, x));
        }
        if (y > 1) {
            squares.add(map.getSquareFromMap(y - 1, x));
            move(unit, map, x, y - 1, xFin, yFin, speed - 1, up);
            squares.remove(map.getSquareFromMap(y - 1, x));
        }

        return allWays.size() != 0;
    }

    public static GameMenuMessages setUnitModeController(String option) {
        String x = Orders.findFlagOption("-x", option);
        String y = Orders.findFlagOption("-y", option);
        String state = Orders.findFlagOption("-s", option);
        assert x != null;
        if (!x.matches("^\\d+$") || !Objects.requireNonNull(y).matches("^\\d+$"))
            return GameMenuMessages.WRONG_FORMAT_COORDINATE;
        int xCoordinate = Integer.parseInt(x);
        int yCoordinate = Integer.parseInt(y);
        if (DataBase.getSelectedMap().getLength() < xCoordinate
                || DataBase.getSelectedMap().getWidth() < yCoordinate)
            return GameMenuMessages.INVALID_COORDINATE;

        StateUnits newState;
        switch (Objects.requireNonNull(state)) {
            case "Standing" -> newState = StateUnits.Stan_Ground;
            case "Defensive" -> newState = StateUnits.Defensive;
            case "Offensive" -> newState = StateUnits.Aggressive;
            default -> {
                return GameMenuMessages.INVALID_STATE;
            }
        }

        Square square = DataBase.getSelectedMap().getSquareFromMap(xCoordinate, yCoordinate);
        for (Unit unit : square.getUnits()) {
            if (unit.getOwner().equals(DataBase.getCurrentGovernment())) {
                unit.changeState(newState);
            }
        }
        return GameMenuMessages.SUCCESS;
    }

    public static GameMenuMessages attackController(String enemyX, String enemyY) {
        if (!Orders.isInputInteger(enemyY) || !Orders.isInputInteger(enemyX))
            return GameMenuMessages.WRONG_FORMAT_COORDINATE;

        if (DataBase.getSelectedUnit().size() == 0) return GameMenuMessages.CHOSE_UNIT_FIRST;

        int targetXInNum = Integer.parseInt(enemyX);
        int targetYInNum = Integer.parseInt(enemyY);

        if (!currentMap.isCoordinationValid(targetXInNum, targetYInNum))
            return GameMenuMessages.INVALID_COORDINATE;

        int targetType = currentMap.getSquareUnfriendlyBelongingsType(currentGovernment, targetXInNum, targetYInNum);
        if (targetType == 0)
            return GameMenuMessages.ATTACK_NO_ENEMY_IN_AREA;

        ArrayList<Unit> currentUnits = DataBase.getSelectedUnit();
        int currentUnitsX = currentUnits.get(0).getXCoordinate();
        int currentUnitsY = currentUnits.get(0).getYCoordinate();
        int unitRange = currentUnits.get(0).getAttackRange();

        if(unitRange > currentMap.getDistance(currentUnitsX, currentUnitsY, targetXInNum, targetYInNum)){
            rangedAttackController(enemyX, enemyY);
            return GameMenuMessages.SUCCESS;
        }

        int unitsZoneFromTarget = Map.getCartesianZone(targetXInNum, targetYInNum, currentUnitsX, currentUnitsY);
        ArrayList<int[]> squaresOptimalForFight = currentMap.getSquaresWithinRange(targetXInNum, targetYInNum, unitRange, unitsZoneFromTarget);

        boolean result = false;
        for (int[] validCoord : squaresOptimalForFight) {
            if (moveUnit(validCoord[0], validCoord[1])) {
                if (targetType == 1) {
                    double distance = Map.getDistance(targetXInNum, targetYInNum, validCoord[0], validCoord[1]);
                    DataBase.attackEnemyByselectedUnits(distance, targetXInNum, targetYInNum);
                } else
                    DataBase.attackBuildingBySelectedUnits(targetXInNum, targetYInNum);

                result = true;
                break;
            }
        }
        if (result)
            return GameMenuMessages.SUCCESS;

        return GameMenuMessages.NORMALATTACK_TARGET_NOT_IN_RANGE;
    }

    public static GameMenuMessages rangedAttackController(String enemyX, String enemyY) {
        if (!Orders.isInputInteger(enemyY) || !Orders.isInputInteger(enemyX))
            return GameMenuMessages.WRONG_FORMAT_COORDINATE;

        int targetXInNum = Integer.parseInt(enemyX);
        int targetYInNum = Integer.parseInt(enemyY);

        if (DataBase.getSelectedUnit().size() == 0)
            return GameMenuMessages.CHOSE_UNIT_FIRST;

        if (!currentMap.isCoordinationValid(targetXInNum, targetYInNum))
            return GameMenuMessages.INVALID_COORDINATE;

        if (!currentMap.doesSquareContainEnemyUnits(targetYInNum, targetXInNum, currentGovernment))
            return GameMenuMessages.ATTACK_NO_ENEMY_IN_AREA;

        if (!DataBase.areSelectedUnitsRanged())
            return GameMenuMessages.RANGEDATTACK_NON_ARCHER_SELECTION;

        int currentUnitsX = DataBase.getSelectedUnit().get(0).getXCoordinate();
        int currentUnitsY = DataBase.getSelectedUnit().get(0).getYCoordinate();

        double distance = Map.getDistance(targetXInNum, targetYInNum, currentUnitsX, currentUnitsY);
        int unitRange = DataBase.getSelectedUnit().get(0).getAttackRange();

        if (unitRange < distance)
            return GameMenuMessages.RANGEDATTACK_TARGET_NOT_IN_RANGE;

        DataBase.attackEnemyByselectedUnits(distance, targetXInNum, targetYInNum);
        return GameMenuMessages.SUCCESS;
    }

    public static GameMenuMessages patrolController(String coordinates) {
        return null;
    }

    public static GameMenuMessages pourOilController(String direction) {
        if (DataBase.getSelectedUnit().size() == 0)
            return GameMenuMessages.CHOSE_UNIT_FIRST;
        if (!(DataBase.getSelectedUnit().get(0) instanceof Engineer))
            return GameMenuMessages.UNIT_ISNT_ENGINEER;
        return null;
    }

    public static GameMenuMessages digTunnelController(String coordinate) {
        String x = Orders.findFlagOption("-x", coordinate);
        String y = Orders.findFlagOption("-y", coordinate);
        assert x != null;
        if (!x.matches("^\\d+$") || !Objects.requireNonNull(y).matches("^\\d+$"))
            return GameMenuMessages.WRONG_FORMAT_COORDINATE;
        int xCoordinate = Integer.parseInt(x);
        int yCoordinate = Integer.parseInt(y);
        if (DataBase.getSelectedMap().getLength() < xCoordinate || DataBase.getSelectedMap().getWidth() < yCoordinate)
            return GameMenuMessages.INVALID_COORDINATE;

        if (DataBase.getSelectedUnit().size() == 0) return GameMenuMessages.CHOSE_UNIT_FIRST;
        if (!(DataBase.getSelectedUnit().get(0) instanceof Tunneler)) return GameMenuMessages.NOT_TUNNELER;
        if (!DataBase.getSelectedMap().getSquareFromMap(xCoordinate, yCoordinate).canPass())
            return GameMenuMessages.CANT_GO_THERE;

        if (moveUnit(xCoordinate, yCoordinate)) {
            int[] coord = DataBase.getEnemyClosestBuilding(xCoordinate, yCoordinate);
            Building targetBuilding = currentMap.getSquareFromMap(coord[0], coord[1]).getBuilding();
            if (!targetBuilding.getName().equals("Keep") && !targetBuilding.getName().equals("SquareTower")
                    && !targetBuilding.getName().equals("CircularTower") && !targetBuilding.getName().equals("BigStoneGate")) {
                targetBuilding.changeHP(999999);
                DataBase.removeDestroyedBuildings(targetBuilding);
            }
            return GameMenuMessages.SUCCESS;
        } else return GameMenuMessages.CANT_GO_THERE;
    }

    public static GameMenuMessages buildEquipmentController(String siegeName) {
        if (!Siege.getSiegesName().contains(siegeName)) return GameMenuMessages.WRONG_NAME;
        Siege siege = Siege.createSiege(currentGovernment, siegeName, -1, -1);
        if (DataBase.getSelectedUnit().size() == 0) return GameMenuMessages.CHOSE_UNIT_FIRST;
        if (!(DataBase.getSelectedUnit().get(0) instanceof Engineer)) return GameMenuMessages.UNIT_ISNT_ENGINEER;
        assert siege != null;
        if (DataBase.getSelectedUnit().size() < siege.getEngineerNeed()) return GameMenuMessages.NOT_ENOUGH_ENGINEER;
        if (siege.getCost() > currentGovernment.getMoney()) return GameMenuMessages.NOT_ENOUGH_BALANCE;

        int xCoordinate = DataBase.getSelectedUnit().get(0).getXCoordinate();
        int yCoordinate = DataBase.getSelectedUnit().get(0).getYCoordinate();
        if (DataBase.getSelectedMap().getSquareFromMap(xCoordinate, yCoordinate).getBuilding() == null)
            return GameMenuMessages.CANT_BUILD_HERE;

        currentGovernment.changeMoney(siege.getCost());
        Defence siegeTent = Defence.createDefence(currentGovernment, xCoordinate, yCoordinate, "SiegeTent");
        buildSiege.put(DataBase.getSelectedMap().getSquareFromMap(xCoordinate, yCoordinate), siegeName);

        return GameMenuMessages.SUCCESS;
    }

    public static GameMenuMessages disbandUnitController() {
        ArrayList<Unit> units = DataBase.getSelectedUnit();
        int xCoordinate = DataBase.getSelectedUnit().get(0).getXCoordinate();
        int yCoordinate = DataBase.getSelectedUnit().get(0).getYCoordinate();
        if (units.size() == 0) return GameMenuMessages.CHOSE_UNIT_FIRST;

        currentGovernment.changeFreeWorkers(units.size());
        DataBase.getSelectedMap().getSquareFromMap(xCoordinate, yCoordinate).removeAllUnit(units.get(0));

        return GameMenuMessages.SUCCESS;
    }

    public static GameMenuMessages showMapController(String x, String y) {
        return null;
    }

    public static void setCurrentMap(Map map) {
        currentMap = map;
    }

    public static int getTurnsPassed() {
        return turnsPassed;
    }

    public static String getCurrentGovernmentUsername() {
        return currentGovernment.getOwner().getUsername();
    }

    public static ArrayList<String> getBuildingValidLandsByName(String buildingName) {

        for (Building building : allBuildings)
            if (building.getName().equals(buildingName))
                return building.getLands();

        return null;
    }

    public static Building getBuildingByName(String buildingName) {
        for (Building building : allBuildings)
            if (building.getName().equals(buildingName))
                return building;
        return null;
    }

    public static ArrayList<Unit> getAllUnits() {
        return allUnits;
    }

    public static void addToAllUnits(Unit unit) {
        allUnits.add(unit);
    }

    public static GameMenuMessages setTaxRateController(int rateNumber) {
        if (rateNumber > 8 || rateNumber < -3 || (rateNumber % 1) != 0)
            return GameMenuMessages.INVALID_TAX_RATE;
        else {
            DataBase.getCurrentGovernment().setTax(rateNumber);
            return GameMenuMessages.SET_TAX_SUCCESS;
        }
    }

    public static String showFoodListController() {
        Government myGovernment = DataBase.getCurrentGovernment();
        String toReturn = "";
        Resource apple = Resource.createResource("Apples");
        Resource meat = Resource.createResource("Meat");
        Resource cheese = Resource.createResource("Cheese");
        Resource bread = Resource.createResource("Bread");

        toReturn += "Apples: " + myGovernment.getResourceInStockpiles(apple) + "\n";
        toReturn += "Meat: " + myGovernment.getResourceInStockpiles(meat) + "\n";
        toReturn += "Cheese: " + myGovernment.getResourceInStockpiles(cheese) + "\n";
        toReturn += "Bread: " + myGovernment.getResourceInStockpiles(bread);

        return toReturn;
    }

    public static GameMenuMessages setFoodRateController(int rateNumber) {
        if (rateNumber > 2 || rateNumber < -2 || (rateNumber % 1) != 0)
            return GameMenuMessages.INVALID_FOOD_RATE;
        else {
            DataBase.getCurrentGovernment().setFood(rateNumber);
            return GameMenuMessages.SET_FOOD_RATE_SUCCESS;
        }
    }

    public static GameMenuMessages modifyGates(String openState){
        if(openState==null)
            return GameMenuMessages.EMPTY_INPUT_FIELDS_ERROR;

        if(selectedBuilding==null)
            return GameMenuMessages.MODIFYGATES_UNMATCHING_BUILDING;

        if(!selectedBuilding.getName().equals("BigStoneGate")||
            selectedBuilding.getName().equals("SmallStoneGate"))
                return GameMenuMessages.MODIFYGATES_UNMATCHING_BUILDING;
            
        if(openState.equals("open")){
            selectedBuilding.changeCanPass(true);
            return GameMenuMessages.SUCCESS;
        }
        else if(openState.equals("close")){
            selectedBuilding.changeCanPass(false);
            return GameMenuMessages.SUCCESS;
        }
        else return GameMenuMessages.INVALID_STATE;
    }

    public static GameMenuMessages setFearRateController(int rateNumber) {
        if (rateNumber > 5 || rateNumber < -5 || (rateNumber % 1) != 0)
            return GameMenuMessages.INVALID_FEAR_RATE;
        else {
            DataBase.getCurrentGovernment().setFear(rateNumber);
            return GameMenuMessages.SET_FEAR_RATE_SUCCESS;
        }
    }

    public static Government getCurrentGovernment() {
        return currentGovernment;
    }

    public static GameMenuMessages digDitchController(String x, String y) {
        if(x == null || y == null)
            return GameMenuMessages.NO_OPTIONS;
        int xInt = Integer.parseInt(x);
        int yInt = Integer.parseInt(y);

        if(xInt > DataBase.getSelectedMap().getLength() || xInt <= 0)
            return GameMenuMessages.INVALID_X;
        if(yInt > DataBase.getSelectedMap().getWidth() || yInt <= 0)
            return GameMenuMessages.INVALID_Y;

        if(DataBase.getSelectedUnit() == null)
            return GameMenuMessages.NO_UNIT_SELECTED;
        else if(! (DataBase.getSelectedUnit().get(0) instanceof Troop)){
            return GameMenuMessages.CANNOT_DIG_DITCH;
        }
        else{
            Troop selectedTroop = (Troop) DataBase.getSelectedUnit().get(0);
            if(selectedTroop.isDigMoat() == false)
                return GameMenuMessages.CANNOT_DIG_DITCH;

            DataBase.getSelectedMap().getSquareFromMap(yInt , xInt).setLand(Land.DITCH);
            return GameMenuMessages.DIG_DITCH_SUCCESS;
        }
    }

    public static GameMenuMessages fillDitchController(String x, String y) {
        if(x == null || y == null)
            return GameMenuMessages.NO_OPTIONS;
        int xInt = Integer.parseInt(x);
        int yInt = Integer.parseInt(y);

        if(xInt > DataBase.getSelectedMap().getLength() || xInt <= 0)
            return GameMenuMessages.INVALID_X;
        if(yInt > DataBase.getSelectedMap().getWidth() || yInt <= 0)
            return GameMenuMessages.INVALID_Y;

        if(DataBase.getSelectedUnit() == null)
            return GameMenuMessages.NO_UNIT_SELECTED;
        else if(! (DataBase.getSelectedUnit().get(0) instanceof Troop)){
            return GameMenuMessages.CANNOT_DIG_DITCH;
        }
        else{
            Troop selectedTroop = (Troop) DataBase.getSelectedUnit().get(0);
            if(selectedTroop.isDigMoat() == false)
                return GameMenuMessages.CANNOT_DIG_DITCH;

            DataBase.getSelectedMap().getSquareFromMap(yInt , xInt).setLand(Land.DITCH);
            return GameMenuMessages.FILL_DITCH_SUCCESS;
        }
    }
}
