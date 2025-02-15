package me.lauriichan.minecraft.pluginbase.data;

import java.util.function.BiFunction;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;

public abstract class DirectoryDataExtension<T> implements IDirectoryDataExtension<T> {

    private final ObjectSet<FileKey> newData = ObjectSets.synchronize(new ObjectArraySet<>());
    private BiFunction<String, String, FileKey> keyWrapper;

    @Override
    public final void keyWrapper(BiFunction<String, String, FileKey> keyWrapper) {
        if (this.keyWrapper != null) {
            return;
        }
        this.keyWrapper = keyWrapper;
    }

    @Override
    public final BiFunction<String, String, FileKey> keyWrapper() {
        return keyWrapper;
    }

    @Override
    public FileKey keyOf(String path, String extension) {
        return keyWrapper.apply(path, extension);
    }

    public final ObjectSet<FileKey> newData() {
        return newData;
    }

    @Override
    public void clearNewData() {
        newData.clear();
    }

}
