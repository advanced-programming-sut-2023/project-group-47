package View;

import Controller.ShowMapMenuController;
import Controller.Orders;
import View.Enums.Commands.ShowMapMenuCommands;

import java.util.regex.Matcher;

public class ShowMapMenu {
    public static void run(){
        String input;
        Matcher matcher;
        Input_Output.outPut("Show Map Menu:");

        while (true){
            input = Input_Output.getInput();
            if((matcher = ShowMapMenuCommands.getMatcher(input , ShowMapMenuCommands.SHOW_MAP)) != null)
                showMap(matcher);
            else if((matcher = ShowMapMenuCommands.getMatcher(input , ShowMapMenuCommands.MOVE_MAP)) != null)
                moveMap(matcher);
            else if((matcher = ShowMapMenuCommands.getMatcher(input , ShowMapMenuCommands.SHOW_DETAILS)) != null)
                showMap(matcher);
            else if(ShowMapMenuCommands.getMatcher(input , ShowMapMenuCommands.EXIT) != null) {
                Input_Output.outPut("returned back to main menu");
                break;
            } else Input_Output.outPut("Invalid command!");

        }
    }
    private static void showMap(Matcher matcher){
        String options = matcher.group("options");

        String x = Orders.findFlagOption("-x" , options);
        String y = Orders.findFlagOption("-y" , options);

        String[][] toPrint = ShowMapMenuController.showMapController(x , y);

        for (int i = 0 ; i < 20 ; i++){
            int enterFlag = 0;
            for (int j = 0 ; j < 20 ; j++){
                if(toPrint[i][j] == null)
                    continue;
                enterFlag = 1;
                System.out.print(toPrint[i][j]);
            }
            if(enterFlag == 1)
                System.out.println("");
        }
    }
    private static void moveMap(Matcher matcher){
        String direction = matcher.group("direction");
        String direction2 = matcher.group("direction2");
        String amount = matcher.group("amount");

        String[][] toPrint = ShowMapMenuController.moveMapController(direction , direction2 , amount);

        for (int i = 0 ; i < 20 ; i++){
            for (int j = 0 ; j < 20 ; j++){
                if(toPrint == null)
                    break;
                System.out.println(toPrint[i][j]);
            }
        }
    }
    private static void showDetails(Matcher matcher){
        String x = Orders.findFlagOption("-x" , matcher.group("options"));
        String y = Orders.findFlagOption("-y" , matcher.group("options"));

        String toPrint = ShowMapMenuController.showDetailsController(x , y);

        System.out.print(toPrint);
    }
}