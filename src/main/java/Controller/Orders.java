package Controller;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Model.DataBase;
import Model.Units.Unit;

import java.security.SecureRandom;

public class Orders {
    public static Matcher createMatcher(String regex, String input) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher;
    }

    private static String findRawFlagOption(String flag, String input) {
        String optionRegex = "(?<option>(\"[^\"]+\")|([^\\s]+))";
        String flagRegex = flag + "\\s+" + optionRegex;
        Matcher flagMatcher = createMatcher(flagRegex, input);
        if (!flagMatcher.find()) return null;

        return flagMatcher.group("option");
    }

    public static boolean doesFlagExist(String flag, String input) {
        Matcher matcher = createMatcher(flag, input);
        if (matcher.find())
            return true;

        return false;
    }

    public static String findFlagOption(String flag, String input) {

        String option = findRawFlagOption(flag, input);
        if (option == null)
            return null;

        option = trimIfNeeded(option);
        return option;
    }

    public static String trimIfNeeded(String input) {
        if (input.charAt(0) == '\"')
            return trimEndAndStartOfString(input);
        else return input;
    }

    public static String findWordAfterFlagSequence(String flag, String input) {
        String nextWordRegex = "(?<nextWord>(\"[^\"]+\")|([^\\s]+))";

        String flagOption = findRawFlagOption(flag, input);
        if (flagOption == null) return null;


        String nextWordSearchRegex = flag + "\\s+" + flagOption + "\\s+" + nextWordRegex;
        Matcher nextWordMatcher = createMatcher(nextWordSearchRegex, input);
        if (!nextWordMatcher.find()) return null;


        String nextWord = nextWordMatcher.group("nextWord");
        nextWord = trimIfNeeded(nextWord);

        return nextWord;
    }


    public static boolean isInputInteger(String input){
        String integerRegex="\\d+";
        createMatcher(integerRegex, input);
        
        if(input.matches(integerRegex))
            return true;
        else return false;
    }

    public static Boolean isOrderNotJunky(String order) {
        //ToDo

        //note: aksare ordera, bayad hameye flag haye valid va optioneshon ke joda shodan az string, 
        //      kamel khali she va chizi azash namone; vagarna yani vasate dastor
        //      chize cherto pert vared karde va error bayad begire

        return null;
    }

    private static String trimEndAndStartOfString(String input) {
        String output = "";
        for (int i = 1; i < input.length() - 1; i++) {
            output = output.concat("" + input.charAt(i));
        }
        return output;
    }

    public static ArrayList<int[]> concatCoords(ArrayList<int[]> coords1,ArrayList<int[]> coords2 ){
        for (int[] coord : coords2) 
            coords1.add( coord);
        
        return coords1;
    } 
}