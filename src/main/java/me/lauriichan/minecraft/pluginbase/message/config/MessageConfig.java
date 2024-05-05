package me.lauriichan.minecraft.pluginbase.message.config;

import me.lauriichan.laylib.localization.IMessage;
import me.lauriichan.laylib.localization.MessageProvider;
import me.lauriichan.minecraft.pluginbase.config.Configuration;
import me.lauriichan.minecraft.pluginbase.config.ISingleConfigExtension;
import me.lauriichan.minecraft.pluginbase.message.provider.SimpleMessage;
import me.lauriichan.minecraft.pluginbase.message.provider.SimpleMessageProvider;

public abstract class MessageConfig implements ISingleConfigExtension {

    protected void loadMessages(final Configuration configuration, final String language, final MessageProvider[] providers) {
        for (final MessageProvider provider : providers) {
            if (!(provider.getMessage(language) instanceof final SimpleMessage message)) {
                continue;
            }
            message.translation(configuration.get(provider.getId(), String.class));
        }
    }

    protected void saveMessages(final Configuration configuration, final String language, final MessageProvider[] providers) {
        IMessage message;
        for (final MessageProvider provider : providers) {
            message = provider.getMessage(language);
            if (message == null) {
                if (provider instanceof final SimpleMessageProvider simpleProvider) {
                    configuration.set(provider.getId(), simpleProvider.getFallback());
                }
                continue;
            }
            configuration.set(message.id(), message.value());
        }
    }

}
