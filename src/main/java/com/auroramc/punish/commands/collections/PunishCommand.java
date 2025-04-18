package com.auroramc.punish.commands.collections;

import com.auroramc.punish.commands.Command;
import com.auroramc.punish.commands.CommandInterface;
import com.auroramc.punish.manager.PunishmentManager;
import com.auroramc.punish.model.PunishmentReason;
import com.auroramc.punish.types.PunishmentType;
import com.auroramc.punish.utils.TimeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.StringJoiner;
import java.util.regex.Pattern;

@Command(cmd = "punir", alias = {"punish"}, onlyPlayer = true)
public class PunishCommand implements CommandInterface {

    private static final Pattern URL_PATTERN = Pattern.compile("^(https?://).+");

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (!player.hasPermission("role.admin")) {
            player.sendMessage(TextComponent.fromLegacyText("§cSomente Ajudante ou superior podem executar este comando."));
            return;
        }

        if (args.length == 0) {
            player.sendMessage(TextComponent.fromLegacyText("§cUtilize /punir <jogador> para selecionar um tipo de infração."));
            return;
        }

        ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(args[0]);
        if (targetPlayer == null) {
            player.sendMessage(TextComponent.fromLegacyText("§cEste jogador não está online."));
            return;
        }

        if (args.length == 1) {
            sendAvailablePunishmentReasons(player, targetPlayer);
            return;
        }

        String reasonName = args[1].toUpperCase();
        PunishmentReason reason = getPunishmentReason(reasonName);

        if (reason == null) {
            player.sendMessage(TextComponent.fromLegacyText("§cMotivo de punição inválido!"));
            sendAvailablePunishmentReasons(player, targetPlayer);
            return;
        }

        if (!hasPermissionToPunish(player, reason)) {
            player.sendMessage(TextComponent.fromLegacyText("§cVocê não tem permissão para aplicar esta punição."));
            return;
        }

        String evidence = args.length >= 3 ? args[2] : "";
        String evidenceCheckResult = checkEvidence(player, evidence);

        if (evidenceCheckResult != null) {
            player.sendMessage(TextComponent.fromLegacyText(evidenceCheckResult));
            return;
        }

        PunishmentManager.getInstance().punishPlayer(targetPlayer.getName(), reason, evidence, player.getName());
        player.sendMessage(TextComponent.fromLegacyText("§ePunição aplicada com sucesso."));
    }

    private void sendAvailablePunishmentReasons(ProxiedPlayer sender, ProxiedPlayer targetPlayer) {
        sender.sendMessage(TextComponent.fromLegacyText(""));
        sender.sendMessage(TextComponent.fromLegacyText("§eTipos de infração disponíveis:"));

        for (PunishmentReason reason : PunishmentReason.values()) {
            TextComponent reasonText = new TextComponent("§f" + reason.getName());

            String rank = getRankForPunishment(reason);
            StringJoiner hoverText = createHoverText(reason, rank);

            reasonText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverText.toString())));
            reasonText.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/punir " + targetPlayer.getName() + " " + reason.name().toUpperCase() + " <prova>"));

            sender.sendMessage(reasonText);
        }
        sender.sendMessage(TextComponent.fromLegacyText(""));
    }

    private String getRankForPunishment(PunishmentReason reason) {
        switch (reason.getTypes()[0].getType()) {
            case BAN:
                return "§2Moderador";
            case MUTE:
                return "§eAjudante";
            default:
                return "§eAjudante";
        }
    }

    private StringJoiner createHoverText(PunishmentReason reason, String rank) {
        StringJoiner hoverText = new StringJoiner("\n");
        hoverText.add("§e" + reason.getName());
        hoverText.add("");
        hoverText.add(reason.getDescription());
        hoverText.add("");
        hoverText.add("§fGrupo mínimo: " + rank);
        hoverText.add("§fRedes: §7MINIGAMES");
        hoverText.add("");

        for (int i = 0; i < reason.getTypes().length; i++) {
            PunishmentType type = reason.getTypes()[i];
            hoverText.add("§e" + TimeUtils.getOrdinalNumber(i + 1) + ": §f[" + type.getType().name().replace("TEMP", "") + "] " + (type.getDuration().equals("0") ? "Permanente" : type.getDuration()));
        }

        return hoverText;
    }

    private PunishmentReason getPunishmentReason(String reasonName) {
        for (PunishmentReason reason : PunishmentReason.values()) {
            if (reason.name().equalsIgnoreCase(reasonName)) {
                return reason;
            }
        }
        return null;
    }

    private boolean hasPermissionToPunish(ProxiedPlayer player, PunishmentReason reason) {
        return reason.getTypes()[0].getType() != PunishmentType.Type.BAN || player.hasPermission("role.ceo");
    }

    private String checkEvidence(ProxiedPlayer sender, String evidence) {
        if (evidence.isEmpty() && !sender.hasPermission("role.admin")) {
            return "§cApenas staffers do grupo Admin ou superior podem punir sem provas.";
        }
        if (!URL_PATTERN.matcher(evidence).matches()) {
            return "§cA URL da prova deve iniciar com http:// ou https://.";
        }
        return null;
    }
}
