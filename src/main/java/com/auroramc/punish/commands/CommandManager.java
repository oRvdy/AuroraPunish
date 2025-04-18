package com.auroramc.punish.commands;

import com.auroramc.punish.plugin.AuroraPlugin;
import lombok.NonNull;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.Set;

public class CommandManager extends Command {

    public static void setupCommands(AuroraPlugin plugin) {
        Reflections reflections = new Reflections(plugin.getClass().getPackage().getName());
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(com.auroramc.punish.commands.Command.class);
        for (Class<?> clazz : annotatedClasses) {
            if (CommandInterface.class.isAssignableFrom(clazz)) {
                com.auroramc.punish.commands.Command annotation = clazz.getAnnotation(com.auroramc.punish.commands.Command.class);
                String name = annotation.cmd();
                String[] aliases = annotation.alias();
                boolean onlyPlayer = annotation.onlyPlayer();

                try {
                    Constructor<?> constructor = clazz.getConstructor();
                    CommandInterface commandInterface = (CommandInterface) constructor.newInstance();
                    plugin.getProxy().getPluginManager().registerCommand(plugin, new CommandManager(name, commandInterface, onlyPlayer, aliases));
                } catch (ReflectiveOperationException e) {
                    plugin.sendMessage("Failed to register command: " + name, 'c');
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @NonNull
    private final CommandInterface commandInterface;
    private final boolean onlyPlayer;

    public CommandManager(@NonNull String name, @NonNull CommandInterface commandInterface, boolean onlyPlayer, String... aliases) {
        super(name, null, aliases);
        this.commandInterface = commandInterface;
        this.onlyPlayer = onlyPlayer;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof net.md_5.bungee.api.connection.ProxiedPlayer) && onlyPlayer) {
            sender.sendMessage(TextComponent.fromLegacyText("§cComando disponível apenas para jogadores!"));
            return;
        }

        this.commandInterface.execute(sender, this.getName(), args);
    }
}
