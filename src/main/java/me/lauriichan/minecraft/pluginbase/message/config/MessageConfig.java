package me.lauriichan.minecraft.pluginbase.message.config;

import me.lauriichan.laylib.localization.IMessage;
import me.lauriichan.laylib.localization.MessageProvider;
import me.lauriichan.minecraft.pluginbase.config.Configuration;
import me.lauriichan.minecraft.pluginbase.config.ISingleConfigExtension;
import me.lauriichan.minecraft.pluginbase.message.provider.ISimpleMessage;
import me.lauriichan.minecraft.pluginbase.message.provider.ISimpleMessageProvider;

public abstract class MessageConfig implements ISingleConfigExtension {

    protected void loadMessages(final Configuration configuration, final String language, final MessageProvider[] providers) {
        for (final MessageProvider provider : providers) {
            IMessage message = provider.getMessage(language);
            if (!(message instanceof ISimpleMessage)) {
                continue;
            }
            ((ISimpleMessage) message).translation(configuration.get(provider.getId(), String.class));
        }
    }

    protected void saveMessages(final Configuration configuration, final String language, final MessageProvider[] providers) {
        IMessage message;
        for (final MessageProvider provider : providers) {
            message = provider.getMessage(language);
            if (message == null) {
                if (provider instanceof ISimpleMessageProvider) {
                    configuration.set(provider.getId(), ((ISimpleMessageProvider) provider).getFallback());
                }
                continue;
            }
            configuration.set(message.id(), message.value());
        }
    }

}
