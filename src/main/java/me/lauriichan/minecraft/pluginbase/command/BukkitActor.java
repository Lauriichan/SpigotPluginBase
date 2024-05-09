package me.lauriichan.minecraft.pluginbase.command;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import me.lauriichan.laylib.command.ActionMessage;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.localization.IMessage;
import me.lauriichan.laylib.localization.Key;
import me.lauriichan.laylib.localization.MessageManager;
import me.lauriichan.laylib.localization.MessageProvider;
import me.lauriichan.minecraft.pluginbase.message.component.ComponentBuilder;
import net.md_5.bungee.api.ChatMessageType;

public class BukkitActor<P extends CommandSender> extends Actor<P> {

    public BukkitActor(final P handle, final MessageManager messageManager) {
        super(handle, messageManager);
    }

    @Override
    public UUID getId() {
        final Actor<Entity> actor = as(Entity.class);
        if (actor.isValid()) {
            return actor.getHandle().getUniqueId();
        }
        return IMPL_ID;
    }

    @Override
    public String getName() {
        final Actor<Entity> actor = as(Entity.class);
        if (actor.isValid()) {
            final Entity entity = actor.getHandle();
            if (entity.getCustomName() == null) {
                return entity.getName();
            }
            return entity.getCustomName();
        }
        return handle.getName();
    }

    @Override
    public void sendMessage(final String message) {
        ComponentBuilder.create().appendContent(message).send(handle);
    }

    public void sendBarMessage(final String message) {
        ComponentBuilder.create().appendContent(message).send(this, ChatMessageType.ACTION_BAR);
    }

    public void sendBarMessage(IMessage message, Key... placeholders) {
        sendBarMessage(messageManager.format(message, placeholders));
    }

    public void sendTranslatedBarMessage(MessageProvider provider, Key... placeholders) {
        sendBarMessage(messageManager.translate(provider, getLanguage(), placeholders));
    }

    public void sendTranslatedBarMessage(String messageId, Key... placeholders) {
        sendBarMessage(messageManager.translate(messageId, getLanguage(), placeholders));
    }

    @Override
    public void sendActionMessage(final ActionMessage message) {
        ComponentBuilder.create().appendContent(message).send(this);
    }

    @Override
    public boolean hasPermission(final String permission) {
        return handle.hasPermission(permission);
    }

}