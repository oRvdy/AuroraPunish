package com.auroramc.punish.commands;

import net.md_5.bungee.api.CommandSender;

public interface CommandInterface {

    void execute(CommandSender sender, String label, String[] args);

}
