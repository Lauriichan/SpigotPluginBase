package me.lauriichan.minecraft.pluginbase.inventory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaLookup;

public final class GuiInventoryReflection {

    private static final Class<?> INVENTORY_VIEW = ClassUtil.findClass("org.bukkit.inventory.InventoryView");

    private static final MethodHandle GET_OPEN_INVENTORY = JavaLookup.PLATFORM.findMethod(HumanEntity.class, "getOpenInventory",
        MethodType.methodType(INVENTORY_VIEW));
    private static final MethodHandle GET_TOP_INVENTORY = JavaLookup.PLATFORM.findMethod(INVENTORY_VIEW, "getTopInventory",
        MethodType.methodType(Inventory.class));
    private static final MethodHandle SET_TITLE = JavaLookup.PLATFORM.findMethod(INVENTORY_VIEW, "setTitle",
        MethodType.methodType(void.class, String.class));

    private static final boolean IS_COMPATIBLE = Modifier.isAbstract(INVENTORY_VIEW.getModifiers());
    
    private GuiInventoryReflection() {
        throw new UnsupportedOperationException();
    }

    public static void updateTitle(HumanEntity entity, Inventory inventory, String title) {
        if (!IS_COMPATIBLE) {
            setReflectionTitle(entity, inventory, title);
            return;
        }
        var view = entity.getOpenInventory();
        if (view.getTopInventory() == inventory) {
            view.setTitle(title);
        }
    }
    
    private static void setReflectionTitle(HumanEntity entity, Inventory inventory, String title) {
        try {
            Object object = GET_OPEN_INVENTORY.invoke(entity);
            if (GET_TOP_INVENTORY.invoke(object) == inventory) {
                SET_TITLE.invoke(object, title);
            }
        } catch (Throwable e) {
            // Ignore any exceptions
        }
    }

}
