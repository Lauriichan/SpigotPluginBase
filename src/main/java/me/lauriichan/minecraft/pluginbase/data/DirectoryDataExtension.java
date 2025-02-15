package me.lauriichan.minecraft.pluginbase.data;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;

public abstract class DirectoryDataExtension<T> implements IDirectoryDataExtension<T> {
    
    private final ObjectSet<FileKey> newData = ObjectSets.synchronize(new ObjectArraySet<>());
    
    public final ObjectSet<FileKey> newData() {
        return newData;
    }
    
    @Override
    public void clearNewData() {
        newData.clear();
    }

}
