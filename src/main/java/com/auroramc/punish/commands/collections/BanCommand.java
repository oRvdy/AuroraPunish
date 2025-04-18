package com.auroramc.punish.commands.collections;

import com.auroramc.punish.commands.Command;
import com.auroramc.punish.commands.CommandInterface;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@Command(cmd = "ban")
public class BanCommand implements CommandInterface {
    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cVocê precisa do grupo Admin ou superior para executar este comando."));
            return;
        }

    }
}
