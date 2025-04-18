package com.auroramc.punish.commands.collections;

import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.auroramc.punish.commands.Command;
import com.auroramc.punish.commands.CommandInterface;
import com.auroramc.punish.database.Database;
import com.auroramc.punish.database.types.MongoDB;
import com.auroramc.punish.manager.PunishmentManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bson.Document;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Command(cmd = "despunir", alias = {"unpunish", "unpunir"})
public class UnPunishCommand implements CommandInterface {

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        boolean isConsole = !(sender instanceof ProxiedPlayer);
        ProxiedPlayer player = isConsole ? null : (ProxiedPlayer) sender;

        if (player != null && !player.hasPermission("role.admin")) {
            sender.sendMessage(TextComponent.fromLegacyText("§cVocê não tem permissão para usar este comando."));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(TextComponent.fromLegacyText("§cUtilize /despunir <id> para revogar uma punição pelo ID."));
            return;
        }

        String punishmentId = args[0];

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
                    sender.sendMessage(TextComponent.fromLegacyText("§cNenhuma punição encontrada para o ID fornecido."));
                    return;
                }

                Document punishmentToRemove = punishments.stream()
                        .filter(punishment -> punishmentId.equals(punishment.getString("id")))
                        .findFirst()
                        .orElse(null);

                if (punishmentToRemove == null) {
                    sender.sendMessage(TextComponent.fromLegacyText("§cNenhuma punição encontrada com o ID " + punishmentId + "."));
                    return;
                }

                if (punishmentToRemove.getString("status").equals("despunido")) {
                    sender.sendMessage(TextComponent.fromLegacyText("§cEsta punição já foi revogada."));
                    return;
                }

                // Verifica se o console pode revogar a punição ou se o jogador tem permissão
                if (player != null || sender.hasPermission("role.admin")) {
                    try {
                        collection.updateOne(
                                new Document("id", punishmentId),
                                new Document("$set", new Document("status", "despunido").append("revokedBy", isConsole ? "Console" : player.getName()))
                        ).subscribe(new Subscriber<com.mongodb.client.result.UpdateResult>() {
                            @Override
                            public void onSubscribe(Subscription subscription) {
                                subscription.request(Long.MAX_VALUE);
                            }

                            @Override
                            public void onNext(com.mongodb.client.result.UpdateResult updateResult) {
                                // Successfully updated the punishment
                                if (player != null) {
                                    ProxiedPlayer punishedPlayer = player.getServer().getInfo().getPlayers().stream()
                                            .filter(p -> p.getName().equals(punishmentToRemove.getString("playerName")))
                                            .findFirst()
                                            .orElse(null);

                                    if (punishedPlayer != null) {
                                        String playerIP = punishedPlayer.getAddress().getHostString();
                                        PunishmentManager.getInstance().bannedIPs.remove(playerIP);
                                    }
                                }
                                sender.sendMessage(TextComponent.fromLegacyText("§ePunição com ID " + punishmentId + " revogada com sucesso."));
                            }

                            @Override
                            public void onError(Throwable t) {
                                sender.sendMessage(TextComponent.fromLegacyText("§cErro ao revogar a punição: " + t.getMessage()));
                            }

                            @Override
                            public void onComplete() {
                                // Nothing to do
                            }
                        });
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(TextComponent.fromLegacyText("§c" + e.getMessage()));
                    }
                } else {
                    sender.sendMessage(TextComponent.fromLegacyText("§cVocê não pode revogar esta punição."));
                }
            }).exceptionally(ex -> {
                sender.sendMessage(TextComponent.fromLegacyText("§cOcorreu um erro ao revogar a punição: " + ex.getMessage()));
                return null;
            });
        } else {
            sender.sendMessage(TextComponent.fromLegacyText("§cO MongoDB não está disponível."));
        }
    }
}
