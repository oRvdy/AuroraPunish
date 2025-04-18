package com.auroramc.punish;

import com.auroramc.punish.commands.CommandManager;
import com.auroramc.punish.database.Database;
import com.auroramc.punish.database.types.DataType;
import com.auroramc.punish.listener.ListenersManager;
import com.auroramc.punish.plugin.AuroraPlugin;

public class Main extends AuroraPlugin {
    public static Main instance;

    @Override
    public void load() {
        Database.init(DataType.MONGODB);
        instance = this;
    }

    @Override
    public void enable() {
        CommandManager.setupCommands(this);
        ListenersManager.setupListeners(this);
        sendMessage("Plugin ativo com sucesso.");
    }

    @Override
    public void disable() {
        sendMessage("Plugin desabilitado com sucesso", 'c');
    }

    public static Main getInstance() {
        return instance;
    }
}
