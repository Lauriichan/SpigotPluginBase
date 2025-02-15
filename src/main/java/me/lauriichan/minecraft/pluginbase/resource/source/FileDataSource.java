package me.lauriichan.minecraft.pluginbase.resource.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class FileDataSource implements IDataSource {

    private final File file;

    public FileDataSource(final File file) {
        this.file = file;
    }

    @Override
    public boolean isResource() {
        return file.isFile();
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public File getSource() {
        return file;
    }
    
    @Override
    public String getPath() {
        return file.getAbsolutePath();
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }

    @Override
    public boolean isWritable() {
        return file.isFile();
    }

    @Override
    public FileOutputStream openWritableStream() throws IOException {
        ensureCreated();
        return new FileOutputStream(file);
    }

    @Override
    public boolean isReadable() {
        return file.isFile();
    }

    @Override
    public FileInputStream openReadableStream() throws IOException {
        return new FileInputStream(file);
    }
    
    private void ensureCreated() {
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
        }
    }

}