/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles.base;

import hellfirepvp.modularmachinery.common.block.prop.ItemBusSize;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileItemBus
 * Created by HellFirePvP
 * Date: 09.07.2017 / 17:37
 */
public abstract class TileItemBus extends TileInventory implements SelectiveUpdateTileEntity {
    private static final String NBT_EXTERNAL_IO_DISABLED = "disableExternalIO";

    protected int         successCounter   = 0;
    protected boolean     inventoryChanged = false;
    private   ItemBusSize size;
    private   boolean     externalIODisabled = false;

    public TileItemBus() {
    }

    public TileItemBus(ItemBusSize size) {
        super(size.getSlotCount());
        this.size = size;
    }

    @Override
    public void doRestrictedTick() {
    }

    protected boolean canWork(int minWorkDelay, int maxWorkDelay) {
        if (inventoryChanged) {
            inventoryChanged = false;
            return true;
        }

        if (successCounter <= 0) {
            return ticksExisted % maxWorkDelay == 0;
        }
        int workDelay = Math.max(minWorkDelay, maxWorkDelay - (successCounter * 5));
        return ticksExisted % workDelay == 0;
    }

    protected void incrementSuccessCounter(int maxWorkDelay, int minWorkDelay) {
        int max = (maxWorkDelay - minWorkDelay) / 5;
        if (successCounter < max) {
            successCounter++;
        }
    }

    protected void decrementSuccessCounter() {
        if (successCounter > 0) {
            successCounter--;
        }
    }

    public ItemBusSize getSize() {
        return size;
    }

    public boolean isExternalIODisabled() {
        return externalIODisabled;
    }

    public boolean setExternalIODisabled(boolean externalIODisabled) {
        if (this.externalIODisabled == externalIODisabled) return false;

        this.externalIODisabled = externalIODisabled;
        this.successCounter = 0;
        this.inventoryChanged = true;

        if (getWorld() != null && !getWorld().isRemote) markForUpdate();

        return true;
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);

        this.size = ItemBusSize.values()[MathHelper.clamp(compound.getInteger("busSize"), 0, ItemBusSize.values().length - 1)];
        this.externalIODisabled = compound.getBoolean(NBT_EXTERNAL_IO_DISABLED);
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);

        compound.setInteger("busSize", this.size.ordinal());
        if (this.externalIODisabled) {
            compound.setBoolean(NBT_EXTERNAL_IO_DISABLED, true);
        } else {
            compound.removeTag(NBT_EXTERNAL_IO_DISABLED);
        }
    }
}
