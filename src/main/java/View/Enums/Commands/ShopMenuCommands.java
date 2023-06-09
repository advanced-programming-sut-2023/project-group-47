package View.Enums.Commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ShopMenuCommands {
    SHOW_PRICE_LIST("^\\s*show\\s*price\\s*list\\s*$"),
    BUY_ITEM("^buy\\s*(?<options>.+)$"),
    SELL_ITEM("^\\s*sell\\s*(?<options>.+)\\s*$"),
    EXIT("^\\s*exit\\s*$"),
    ;
    private String regex;

    private ShopMenuCommands(String regex) {
        this.regex = regex;
    }

    public static Matcher getMatcher(String input, ShopMenuCommands command) {
        Matcher matcher = Pattern.compile(command.regex).matcher(input);
        if (matcher.matches())
            return matcher;
        return null;
    }
}
