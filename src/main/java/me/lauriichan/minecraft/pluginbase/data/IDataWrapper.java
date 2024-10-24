package me.lauriichan.minecraft.pluginbase.data;

import me.lauriichan.minecraft.pluginbase.resource.source.IDataSource;

public interface IDataWrapper<T, D extends IDataExtension<T>> {

    int SUCCESS = 0x00;
    int SKIPPED = 0x01;
    
    int FAIL_IO_LOAD = 0x11;
    int FAIL_IO_SAVE = 0x12;
    
    int FAIL_DATA_PROPERGATE = 0x21;
    int FAIL_DATA_LOAD = 0x22;
    int FAIL_DATA_SAVE = 0x23;
    int FAIL_DATA_MIGRATE = 0x24;

    static boolean isFailedState(final int state) {
        return state != SUCCESS && state != SKIPPED;
    }

    static boolean isIOError(final int state) {
        return state == FAIL_IO_LOAD || state == FAIL_IO_SAVE;
    }

    static boolean isDataError(final int state) {
        return state == FAIL_DATA_LOAD || state == FAIL_DATA_PROPERGATE || state == FAIL_DATA_MIGRATE || state == FAIL_DATA_SAVE;
    }
    
    D data();
    
    Class<D> dataType();
    
    String path();
    
    IDataSource source();
    
    IDataHandler<T> handler();
    
    int reload(boolean wipeAfterLoad);
    
    int save(boolean force);

}
