package com.turt2live.dumbauction.command.validator;

import com.turt2live.dumbauction.DumbAuction;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Validates arguments to be integers
 *
 * @author turt2live
 */
public class IntValidator implements ArgumentValidator {

    private String error;

    @Override
    public boolean isValid(CommandSender sender, String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
        }
        return false;
    }

    @Override
    public Integer get(CommandSender sender, String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
        }
        return 0;
    }

    @Override
    public String getErrorMessage(CommandSender sender, String input) {
        return ChatColor.RED + error;
    }

    @Override
    public void setArguments(String[] arguments) {
        if (arguments.length < 1)
            throw new IllegalArgumentException("requires 1 string argument");
        if (arguments.length > 1)
            DumbAuction.getInstance().getLogger().warning("Extra arguments supplied in IntValidator");
        error = arguments[0];
    }
}
