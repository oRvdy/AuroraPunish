package com.auroramc.punish.database.types;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.auroramc.punish.database.DBInterface;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

@RequiredArgsConstructor
public class MongoDB implements DBInterface {

    @NonNull
    private String accessURL;

    @Getter
    private MongoClient mongoClient;

    @Override
    public void connect() {
        this.mongoClient = MongoClients.create(accessURL);
    }

    @Override
    public void close() {
        this.mongoClient.close();
        this.mongoClient = null;
    }

    public MongoCollection<Document> getCollection(String collectionName, MongoDatabase db) {
        return db.getCollection(collectionName);
    }

    public MongoDatabase getDatabase(String databaseName) {
        return this.mongoClient.getDatabase(databaseName);
    }
}
