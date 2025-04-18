package com.auroramc.punish.commands.collections;

import com.auroramc.punish.commands.Command;
import com.auroramc.punish.commands.CommandInterface;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@Command(cmd = "clearchat", alias = {"cc"}, onlyPlayer = true)
public class ClearChatCommand implements CommandInterface {

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;

        for (ProxiedPlayer onlinePlayer : player.getServer().getInfo().getPlayers()) {
            for (int i = 0; i < 100; i++) {
                onlinePlayer.sendMessage(new TextComponent(" "));
            }
        }

        player.sendMessage(TextComponent.fromLegacyText("§aO chat foi limpo com sucesso."));
    }
}
