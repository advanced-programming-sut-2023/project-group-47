package View.Enums.Commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum GameMenuCommands {
    SELECT_UNIT("^\\s*select\\s+unit\\s+(?<option>.+)$"),
    MOVE_UNIT("^\\s*move\\s+unit\\s+to\\s+(?<coordinate>.+)$"),
    PATRON_UNIT("^\\s*patrol\\s+unit\\s+(?<coordinate>.+)$"),
    SET_STATE("^\\s*set\\s+state\\s*(?<option>.+)$"),
    ATTACK_GROUND("^\\s*attack\\s+-e\\s+(?<x>\\d+)\\s+(?<y>\\d+)\\s*$"),
    ATTACK_AIR("^\\s*ranged\\s+attack\\s+(?<coordinates>.+)"),
    DIG_TUNNEL("^\\s*dig\\s+tunnel\\s+(?<coordinate>.+)$"),
    POUR_OIL("^\\s*pour\\s+oil\\s+-d\\s+(?<direction>.+)$"),
    BUILD_SIEGE("^\\s*build\\s+-q\\s+(?<siegeName>.+)$"),
    DISBAND("^\\s*disband\\s+unit\\s*$"),
    PUT_BUILDING("\\s*put\\s+building\\s+(?<buildingComponents>.+)"),
    SELECT_BUILDING("\\s*select\\s+building\\s+(?<buildingComponents>.+)"),
    CREATE_UNIT("\\s*create\\s+unit\\s+(?<unitComponents>.+)"),
    REPAIR_BUILDING("\\s*repair\\s*"),
    SHOW_TURNS_PASSED("\\s*show\\s+turns\\s+passed\\s*"),
    SHOW_CURRENT_GOVERNMENT("\\s*show\\s+current\\s+player\\s*"),
    ENTER_SHOW_MAP_MENU("^enter\\s*show\\s*map\\s*menu$"),
    SET_TAX_RATE("^\\s*set\\s*tax\\s*rate\\s*-r\\s*(?<rateNumber>\\d+)\\s*$"),
    SHOW_TAX_RATE("^\\s*show\\s*tax\\s*rate\\s*$"),
    SHOW_POPULARITY_FACTORS("^\\s*show\\s*popularity\\s*factors\\s*$"),
    SHOW_POPULARITY("^\\s*show\\s*popularity\\s*$"),
    SHOW_FOOD_LIST("^\\s*show\\s*food\\s*list\\s*$"),
    SET_FOOD_RATE("^\\s*food\\s*rate\\s*-r\\s*(?<rateNumber>\\d+)\\s*$"),
    SET_FEAR_RATE("^\\s*fear\\s*rate\\s*-r\\s*(?<rateNumber>\\d+)\\s*$"),
    DROP_BUILDING_FOR_CUSTOMIZE("^\\s*drop\\s*building\\s*(?<options>.+)$"),
    MODIFY_GATES("^\\s*modify\\s+gate\\s+(?<state>.+)\\s*"),
    DROP_UNIT("^\\s*drop\\s*unit\\s*(?<options>.+)$"),
    NEXT_TURN("^\\s*next\\s*turn\\s*$"),
    SHOW_MONEY("^\\s*show\\s*money\\s*$"),
    DIG_DITCH("^\\s*dig\\s*ditch\\s*(?<options>.+)$"),
    FILL_DITCH("^\\s*fill\\s*ditch\\s*(?<options>.+)$"),
    ENTER_TRADE_MENU("^\\s*enter\\s*trade\\s*menu\\s*$"),
    ENTER_SHOP_MENU("^\\s*enter\\s*shop\\s*menu\\s*$"),
    ;
    ;
    private String regex;

    private GameMenuCommands(String regex) {
        this.regex = regex;
    }

    public static Matcher getMatcher(String input, GameMenuCommands command) {
        Matcher matcher = Pattern.compile(command.regex).matcher(input);
        if (matcher.matches())
            return matcher;
        return null;
    }
}
