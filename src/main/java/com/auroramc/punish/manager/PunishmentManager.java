package com.auroramc.punish.manager;

import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.auroramc.punish.Main;
import com.auroramc.punish.database.Database;
import com.auroramc.punish.database.types.MongoDB;
import com.auroramc.punish.model.PunishmentReason;
import com.auroramc.punish.types.PunishmentType;
import com.auroramc.punish.utils.TimeUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import org.bson.Document;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import dev.auroramc.laas.player.Profile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PunishmentManager {

    public static PunishmentManager instance;
    public static MongoCollection<Document> getCollection() {
        MongoDB mongoDB = Database.getInstance(MongoDB.class);
        return mongoDB != null ? mongoDB.getDatabase("Aurora").getCollection("punishments") : null;
    }
    MongoCollection<Document> collection = getCollection();
    public ConcurrentHashMap<String, Long> bannedIPs = new ConcurrentHashMap<>();

    private PunishmentManager() {
        }

    public static PunishmentManager getInstance() {
        if (instance == null) {
            instance = new PunishmentManager();
        }
        return instance;
    }

    private long calculateDuration(PunishmentType[] types, int occurrences) {
        return occurrences < types.length ? types[occurrences].getDurationMillis() : types[types.length - 1].getDurationMillis();
    }

    private String calculateEndDate(long durationMillis) {
        if (durationMillis == 0) {
            return "N/A";
        }
        long endTime = System.currentTimeMillis() + durationMillis;
        return TimeUtils.formatDate(endTime);
    }

    public void punishPlayer(String playerName, PunishmentReason reason, String evidence, String punishedBy) {
        countOccurrences(playerName, reason.getName()).thenAccept(occurrences -> {
            PunishmentType[] types = reason.getTypes();
            long durationMillis = calculateDuration(types, occurrences);
            String endDate = calculateEndDate(durationMillis);
            PunishmentType type = occurrences < types.length ? types[occurrences] : types[types.length - 1];

            Document newPunishment = new Document("id", generatePunishmentId())
                    .append("playerName", playerName)
                    .append("reason", reason.getName())
                    .append("type", type.getType().name())
                    .append("duration", durationMillis)
                    .append("timestamp", System.currentTimeMillis())
                    .append("status", "pendente")
                    .append("evidence", evidence)
                    .append("startDate", TimeUtils.formatDate(System.currentTimeMillis()))
                    .append("endDate", endDate)
                    .append("category", "N/A")
                    .append("isVisible", true)
                    .append("isPermanent", durationMillis == 0)
                    .append("unpunishDate", "N/A")
                    .append("unpunishReason", "N/A")
                    .append("network", "N/A")
                    .append("punishedBy", punishedBy)
                    .append("isIgnored", false)
                    .append("occurrences", occurrences); // Adiciona o campo 'occurrences'

            savePunishment(newPunishment).thenRun(() -> {
                ProxiedPlayer player = Main.getInstance().getProxy().getPlayer(playerName);
                if (player != null) {
                    if (newPunishment.getString("type").equals(PunishmentType.Type.BAN.name())) {
                        sendBanMessage(player, newPunishment);
                        } else if (newPunishment.getString("type").equals(PunishmentType.Type.MUTE.name())) {
                        sendMuteMessage(player, newPunishment);
                    }
                    activatePunishment(playerName);
                }

                schedulePunishmentFinalization(newPunishment);
                notifyStaff(playerName, reason, type);
            });
        }).exceptionally(ex -> {
            System.err.println("Failed to count occurrences: " + ex.getMessage());
            return null;
        });
    }


    private CompletableFuture<Integer> countOccurrences(String playerName, String reason) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        collection.find(Filters.and(
                Filters.eq("playerName", playerName),
                Filters.eq("reason", reason),
                Filters.eq("isIgnored", false),
                Filters.or(Filters.eq("status", "ativo"), Filters.eq("status", "pendente"))
        )).subscribe(new Subscriber<Document>() {
            private int count = 0;

            @Override
            public void onSubscribe(Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Document document) {
                count++;
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                future.complete(count);
            }
        });
        return future;
    }


    private String generatePunishmentId() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    private CompletableFuture<Void> savePunishment(Document punishment) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        collection.insertOne(punishment).subscribe(new Subscriber<InsertOneResult>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(InsertOneResult result) {
                future.complete(null);
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                if (!future.isDone()) {
                    future.complete(null);
                }
            }
        });
        return future;
    }

    public void activatePunishment(String playerName) {
        CompletableFuture<List<Document>> futureDocuments = new CompletableFuture<>();
        List<Document> documents = new ArrayList<>();

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
                futureDocuments.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                futureDocuments.complete(documents);
            }
        });

        futureDocuments.thenAccept(docs -> {
            docs.forEach(doc -> {
                String status = doc.getString("status");
                if ("pendente".equals(status)) {
                    long durationMillis = doc.getLong("duration");
                    long punishmentStartTime = doc.getLong("timestamp");

                    Document updatedPunishment = new Document(doc);
                    long currentTime = System.currentTimeMillis();

                    if (durationMillis == 0) {
                        updatedPunishment.put("status", "ativo");
                        updatedPunishment.put("startDate", TimeUtils.formatDate(currentTime));
                        updatedPunishment.put("endDate", "N/A");
                    } else if (currentTime < (punishmentStartTime + durationMillis)) {
                        updatedPunishment.put("status", "ativo");
                        updatedPunishment.put("startDate", TimeUtils.formatDate(currentTime));
                        long endTimeMillis = punishmentStartTime + durationMillis;
                        updatedPunishment.put("endDate", TimeUtils.formatDate(endTimeMillis));
                    } else {
                        updatedPunishment.put("status", "finalizado");
                        updatedPunishment.put("endDate", TimeUtils.formatDate(currentTime));
                    }

                    collection.replaceOne(Filters.eq("id", doc.getString("id")), updatedPunishment).subscribe(new Subscriber<UpdateResult>() {
                        @Override
                        public void onSubscribe(Subscription subscription) {
                            subscription.request(1);
                        }

                        @Override
                        public void onNext(UpdateResult updateResult) {
                            System.out.println("Updated punishment: " + updatedPunishment.toJson());
                        }

                        @Override
                        public void onError(Throwable t) {
                            System.err.println("Failed to update punishment: " + t.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            // No further action needed
                        }
                    });
                }
            });
        }).exceptionally(ex -> {
            System.err.println("Failed to retrieve punishments: " + ex.getMessage());
            return null;
        });
    }

    private CompletableFuture<Document> getPunishmentByPlayer(String playerName) {
        CompletableFuture<Document> future = new CompletableFuture<>();
        collection.find(Filters.eq("playerName", playerName)).first().subscribe(new Subscriber<Document>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                subscription.request(1);
            }

            @Override
            public void onNext(Document document) {
                future.complete(document);
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onComplete() {
                if (!future.isDone()) {
                    future.complete(null);
                }
            }
        });
        return future;
    }

    private void updatePunishments(Document document, Document updateDocument) {
        collection.updateOne(Filters.eq("playerName", document.getString("playerName")), updateDocument).subscribe(new Subscriber<UpdateResult>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                subscription.request(1);
            }

            @Override
            public void onNext(UpdateResult updateResult) {
                System.out.println("Punishments updated successfully: " + updateResult.toString());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Failed to update punishments: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                // No action needed on complete
            }
        });
    }

    private void schedulePunishmentFinalization(Document punishment) {
        if (punishment.getLong("duration") > 0) {
            long durationMillis = punishment.getLong("duration");

            TaskScheduler scheduler = Main.getInstance().getProxy().getScheduler();
            scheduler.schedule(Main.getInstance(), () -> {
                CompletableFuture<Void> future = new CompletableFuture<>();

                // Verificar o status da punição antes de atualizar
                collection.find(Filters.eq("id", punishment.getString("id"))).first().subscribe(new Subscriber<Document>() {
                    @Override
                    public void onSubscribe(Subscription subscription) {
                        subscription.request(1);
                    }

                    @Override
                    public void onNext(Document currentPunishment) {
                        if (currentPunishment != null) {
                            String status = currentPunishment.getString("status");

                            // Somente se o status for "ativo", atualizamos para "finalizado"
                            if (status.equals("ativo")) {
                                collection.updateOne(
                                        Filters.eq("id", punishment.getString("id")),
                                        new Document("$set", new Document("status", "finalizado").append("endDate", TimeUtils.formatDate(System.currentTimeMillis())))
                                ).subscribe(new Subscriber<UpdateResult>() {
                                    @Override
                                    public void onSubscribe(Subscription subscription) {
                                        subscription.request(1);
                                    }

                                    @Override
                                    public void onNext(UpdateResult updateResult) {
                                        future.complete(null);
                                    }

                                    @Override
                                    public void onError(Throwable t) {
                                        future.completeExceptionally(t);
                                    }

                                    @Override
                                    public void onComplete() {
                                        if (!future.isDone()) {
                                            future.complete(null);
                                        }
                                    }
                                });
                            } else {
                                // Se o status não for "ativo", não fazer nada
                                future.complete(null);
                            }
                        } else {
                            future.completeExceptionally(new Exception("Punição não encontrada."));
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }

                    @Override
                    public void onComplete() {
                        if (!future.isDone()) {
                            future.complete(null);
                        }
                    }
                });
            }, durationMillis, TimeUnit.MILLISECONDS);
        }
    }

    public void sendBanMessage(ProxiedPlayer player, Document punishment) {
        String endDate = punishment.getString("endDate");
        String reason = punishment.getString("reason");
        String evidence = punishment.getString("evidence");
        String punishedBy = punishment.getString("punishedBy");
        String id = punishment.getString("id");

        StringBuilder message = new StringBuilder("§c§lAurora\n\n§cVocê está banido ");
        if (endDate.equals("N/A")) {
            message.append("permanentemente do servidor.");
        } else {
            message.append("do servidor até o dia ").append(endDate).append(".");
        }
        message.append("\n\n§cMotivo: ").append(reason);

        if (!evidence.isEmpty()) {
            message.append(" §c- ").append(evidence);
        }
        if (endDate.equals("N/A")) {
            message.append("\n§cAutor: ").append(punishedBy);
        }

        TextComponent banMessage = new TextComponent(message.toString());
        TextComponent reviewLink = new TextComponent("\n\n§cUse o ID §e#" + id + " §cpara criar uma revisão em ");
        TextComponent link = new TextComponent("§cAurora.com/revisao");
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://Aurora.com/revisao"));
        reviewLink.addExtra(link);

        player.disconnect(banMessage, reviewLink);
    }

    public void sendMuteMessage(ProxiedPlayer player, Document punishment) {
        String endDate = punishment.getString("endDate");
        String reason = punishment.getString("reason");
        String evidence = punishment.getString("evidence");
        String punishedBy = punishment.getString("punishedBy");
        String id = punishment.getString("id");

        StringBuilder message = new StringBuilder("\n§c • Você está silenciado ");
        if (endDate.equals("N/A")) {
            message.append("permanentemente.");
        } else {
            message.append("até o dia ").append(endDate).append(".");
        }
        message.append("\n\n§c • Motivo: ").append(reason);

        if (!evidence.isEmpty()) {
            message.append(" §c- ").append(evidence);
        }
        if (endDate.equals("N/A")) {
            message.append("\n§c • Autor: ").append(punishedBy);
        }

        TextComponent muteMessage = new TextComponent(message.toString());
        TextComponent reviewLink = new TextComponent("\n§c • Use o ID §e#" + id + " §cpara criar uma revisão em ");
        TextComponent link = new TextComponent("§cAurora.com/revisao\n");
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://Aurora.com/revisao"));
        reviewLink.addExtra(link);

        player.sendMessage(muteMessage, reviewLink);
    }

    private void notifyStaff(String playerName, PunishmentReason reason, PunishmentType type) {
        Collection<ProxiedPlayer> staffList = Main.getInstance().getProxy().getPlayers();

        for (ProxiedPlayer staff : staffList) {
            Profile staffProfile = Profile.getProfile(staff.getName());
            final ProxiedPlayer player = null;
            if (!player.hasPermission("role.moderador")) {
            TextComponent staffMessage = new TextComponent("\n§c • " + playerName + " foi " + (type.getType() == PunishmentType.Type.BAN ? "banido" : "mutado") + ".\n");
                staffMessage.addExtra("§c • Motivo: " + reason.getName() + " (" + type.getDuration() + ")\n");
                staffMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Clique para mais informações.")));
                staffMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/checkpunir " + playerName));

                staff.sendMessage(staffMessage);
            }
        }
    }
}
