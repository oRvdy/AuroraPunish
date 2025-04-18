package com.auroramc.punish.listener.collections;

import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.auroramc.punish.database.Database;
import com.auroramc.punish.database.types.MongoDB;
import com.auroramc.punish.manager.PunishmentManager;
import com.auroramc.punish.listener.Listener;
import com.auroramc.punish.listener.ListenersManager;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.CompletableFuture;

@Listener
public class PlayerChatListener extends ListenersManager {

    @EventHandler
    public void onChat(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return; // Apenas jogadores devem ser verificados
        }

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        String playerName = player.getName();

        MongoDB mongoDB = Database.getInstance(MongoDB.class);
        if (mongoDB == null) {
            player.sendMessage(TextComponent.fromLegacyText("§cO MongoDB não está disponível."));
            return;
        }

        MongoDatabase database = mongoDB.getDatabase("Aurora");
        MongoCollection<Document> collection = database.getCollection("punishments");

        CompletableFuture<Boolean> futureMute = new CompletableFuture<>();

        collection.find(Filters.or(
                Filters.and(Filters.eq("playerName", playerName), Filters.eq("type", "MUTE"))
        )).subscribe(new Subscriber<Document>() {
            private Subscription subscription;
            private boolean messageSent = false;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Document doc) {
                if (messageSent) {
                    return;
                }

                String status = doc.getString("status");
                String type = doc.getString("type");

                if ("pendente".equals(status)) {
                    PunishmentManager.getInstance().activatePunishment(playerName);
                    status = "ativo";
                }

                if ("ativo".equals(status) && "MUTE".equals(type)) {
                    PunishmentManager.getInstance().sendMuteMessage(player, doc);
                    futureMute.complete(true);
                    subscription.cancel();
                    messageSent = true;
                }
            }

            @Override
            public void onError(Throwable t) {
                futureMute.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                if (!futureMute.isDone()) {
                    futureMute.complete(false);
                }
            }
        });

        try {
            boolean isMuted = futureMute.get();
            if (isMuted) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            player.sendMessage(TextComponent.fromLegacyText("§cErro ao verificar punições: " + e.getMessage()));
        }
    }
}