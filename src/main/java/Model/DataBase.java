package Model;

import Main.Client;
import Main.Request;
import Main.Result;
import Main.UserDataBase;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;

public class DataBase {
    // for each game :
    private final ArrayList<Client> clients;
    private final ArrayList<Government> governments;
    private final Map selectedMap;
    private final Timeline produceTimeline;


    public DataBase(Map selectedMap) {
        clients = new ArrayList<>();
        governments = new ArrayList<>();
        this.selectedMap = selectedMap;
        produceTimeline = new Timeline(new KeyFrame(Duration.seconds(3), actionEvent -> {
            produce();
        }));
        produceTimeline.setCycleCount(-1);
    }


    public Map getSelectedMap() {
        return selectedMap;
    }

    public ArrayList<Client> getClients() {
        return clients;
    }


    public void addClient(Client client) {
        Government government = new Government(this ,0);
        government.setOwner(client.getUser());

        UserDataBase userDataBase = new UserDataBase(client, government, this);
        client.setUserDataBase(userDataBase);

        clients.add(client);
        governments.add(government);
    }


    public void startGame() {
        produceTimeline.play();
    }

    public void endGame() {
        produceTimeline.stop();
    }


    private void produce() {
        for(Government government : governments) {
            government.addAndRemoveFromGovernment();
            government.generateResource();
        }
    }



    public void updateClient(Result result) {
        //TODO : Send Client!
    }
}