package com.auroramc.punish.listener.collections;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.auroramc.punish.database.Database;
import com.auroramc.punish.database.types.MongoDB;
import com.auroramc.punish.listener.Listener;
import com.auroramc.punish.listener.ListenersManager;
import com.auroramc.punish.manager.PunishmentManager;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Listener
public class LoginListener extends ListenersManager {

    @EventHandler
    public void onPlayerLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        String playerName = player.getName();
        String playerIP = player.getAddress().getHostString();

        System.out.println("Jogador logado: " + playerName + " com IP: " + playerIP);
        PunishmentManager.getInstance().activatePunishment(playerName);

        MongoDB mongoDB = Database.getInstance(MongoDB.class);
        if (mongoDB != null) {
            MongoDatabase database = mongoDB.getDatabase("Aurora");
            MongoCollection<Document> collection = database.getCollection("punishments");

            List<Document> documents = new ArrayList<>();
            CompletableFuture<Void> future = new CompletableFuture<>();

            collection.find(Filters.eq("playerName", playerName)).subscribe(new Subscriber<Document>() {
                @Override
                public void onSubscribe(Subscription subscription) {
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(Document document) {
                    documents.add(document);
                }

                @Override
                public void onError(Throwable t) {
                    future.completeExceptionally(t);
                }

                @Override
                public void onComplete() {
                    future.complete(null);
                }
            });

            future.thenRun(() -> {
                if (!documents.isEmpty()) {
                    if (!player.hasPermission("role.ceo"));

                    documents.forEach(doc -> {
                        String status = doc.getString("status");
                        String type = doc.getString("type");
                        System.out.println("Punição recuperada para " + playerName + ": " + doc.toJson());
                        System.out.println("Status da punição: " + status);

                        if (status.equals("ativo") && type.equals("BAN")) {
                            System.out.println("Punição ativa, enviando mensagem de banimento.");
                            PunishmentManager.getInstance().sendBanMessage(player, doc);
                        } else if (status.equals("pendente") && type.equals("BAN")) {
                            System.out.println("Punição pendente, adicionando IP à lista de banidos.");
                            PunishmentManager.getInstance().sendBanMessage(player, doc);
                        }
                    });
                } else {
                    player.sendMessage(TextComponent.fromLegacyText("§cNenhuma punição encontrada para " + playerName));
                }
            }).exceptionally(ex -> {
                player.sendMessage(TextComponent.fromLegacyText("§cErro ao listar punições: " + ex.getMessage()));
                return null;
            });
        } else {
            player.sendMessage(TextComponent.fromLegacyText("§cO MongoDB não está disponível."));
        }
    }
}
