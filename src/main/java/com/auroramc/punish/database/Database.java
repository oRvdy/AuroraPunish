package com.auroramc.punish.database;


import com.auroramc.punish.database.types.DataType;
import com.auroramc.punish.database.types.MongoDB;

public class Database {

    private static DBInterface instance;

    public static void init(DataType type) {
        switch (type) {
            case MONGODB: {
                instance = new MongoDB("mongodb+srv://jccledson13:ewTfWjVR2i1xzJZq@cluster0.g95htqm.mongodb.net/");
                break;
            }
        }

        System.out.println("Sistema de database escolhido: " + type.getName());

        try {
            instance.connect();
            System.out.println("Conectado com sucesso!");
        } catch (Exception e) {
            System.out.println("Ocorreu um erro enquanto conectavamos ao banco de dados!");
            throw new RuntimeException(e);
        }
    }

    public static void close() {
        instance.close();
        System.out.println("Salvando todos os dados pendentes...");
    }

    public static <T> T getInstance(Class<T> dbClass) {
        if (dbClass == null || instance == null) {
            return null;
        }

        return instance.getClass().isAssignableFrom(dbClass) ? (T) instance : null;
    }
}
