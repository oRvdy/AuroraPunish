package com.auroramc.punish.commands.collections;

import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.auroramc.punish.commands.Command;
import com.auroramc.punish.commands.CommandInterface;
import com.auroramc.punish.database.Database;
import com.auroramc.punish.database.types.MongoDB;
import com.auroramc.punish.utils.TimeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bson.Document;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Command(cmd = "checkpunir", alias = {"checkpunish"}, onlyPlayer = true)
public class CheckPunishCommand implements CommandInterface {

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(TextComponent.fromLegacyText("§cEste comando só pode ser usado por jogadores."));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        // Verifique a permissão correta para permitir o uso do comando
        if (!player.hasPermission("role.admin")) {
            player.sendMessage(TextComponent.fromLegacyText("§cVocê não tem permissão para usar este comando."));
            return;
        }

        if (args.length == 0) {
            player.sendMessage(TextComponent.fromLegacyText("§cUtilize /checkpunir <jogador> para ver as punições de um jogador."));
            return;
        }

        String targetName = args[0];
        MongoDB mongoDB = Database.getInstance(MongoDB.class);
        if (mongoDB != null) {
            MongoDatabase database = mongoDB.getDatabase("Aurora");
            MongoCollection<Document> collection = database.getCollection("punishments");

            CompletableFuture<List<Document>> futurePunishments = new CompletableFuture<>();
            List<Document> documentsList = new ArrayList<>();

            collection.find().subscribe(new Subscriber<Document>() {
                @Override
                public void onSubscribe(Subscription subscription) {
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(Document document) {
                    documentsList.add(document);
                }

                @Override
                public void onError(Throwable t) {
                    futurePunishments.completeExceptionally(t);
                }

                @Override
                public void onComplete() {
                    futurePunishments.complete(documentsList);
                }
            });

            futurePunishments.thenAccept(punishments -> {
                if (punishments == null || punishments.isEmpty()) {
                    player.sendMessage(TextComponent.fromLegacyText("§eO jogador não possui punições registradas."));
                    return;
                }

                boolean hasPunishment = false;
                for (Document punishment : punishments) {
                    if (punishment.getString("playerName").equalsIgnoreCase(targetName)) {
                        hasPunishment = true;
                        break;
                    }
                }

                if (!hasPunishment) {
                    player.sendMessage(TextComponent.fromLegacyText("§eO jogador não possui punições registradas."));
                    return;
                }

                for (Document punishment : punishments) {
                    if (punishment.getString("playerName").equalsIgnoreCase(targetName)) {
                        TextComponent punishmentText = buildPunishmentText(punishment, player);
                        player.sendMessage(punishmentText);
                    }
                }
                player.sendMessage(TextComponent.fromLegacyText(""));
            }).exceptionally(ex -> {
                player.sendMessage(TextComponent.fromLegacyText("§cOcorreu um erro ao buscar as punições: " + ex.getMessage()));
                return null;
            });
        } else {
            player.sendMessage(TextComponent.fromLegacyText("§cO MongoDB não está disponível."));
        }
    }

    private TextComponent buildPunishmentText(Document punishment, ProxiedPlayer player) {
        long timestamp = punishment.getLong("timestamp");
        String status = punishment.getString("status");
        String formattedDate = TimeUtils.formatDate(timestamp);
        String reason = punishment.getString("reason");

        String color = getColorForStatus(status);

        TextComponent dateComponent = new TextComponent(color + "[" + formattedDate + "]");
        TextComponent reasonComponent = new TextComponent(color + "[" + reason + "]");
        TextComponent evidenceComponent = new TextComponent("§f[Prova]");
        String evidence = punishment.getString("evidence");
        evidenceComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(evidence.isEmpty() ? "Nenhuma prova fornecida" : "§fClique para copiar: §7" + evidence)));
        evidenceComponent.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, evidence));

        TextComponent punishmentText = new TextComponent();
        punishmentText.addExtra(dateComponent);
        punishmentText.addExtra(" ");
        punishmentText.addExtra(reasonComponent);
        punishmentText.addExtra(" ");
        punishmentText.addExtra(evidenceComponent);

        if (!status.equalsIgnoreCase("despunido")) {
            TextComponent revokeComponent = new TextComponent("[Revogar]");
            if (punishment.getString("punishedBy").equals(player.getName()) || player.hasPermission("role.ceo")) {
                revokeComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para revogar esta punição")));
                revokeComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/despunir " + punishment.getString("id")));
            } else {
                revokeComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§cApenas o autor da punição ou um administrador podem remover esta punição.")));
            }
            punishmentText.addExtra(" ");
            punishmentText.addExtra(revokeComponent);
        }

        return punishmentText;
    }

    private String getColorForStatus(String status) {
        switch (status.toLowerCase()) {
            case "pendente":
                return "§e";
            case "ativo":
                return "§a";
            case "finalizado":
                return "§c";
            case "despunido":
                return "§7";
            default:
                return "§f";
        }
    }
}
