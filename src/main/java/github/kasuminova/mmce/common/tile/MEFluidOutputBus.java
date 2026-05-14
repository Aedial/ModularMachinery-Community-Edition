package github.kasuminova.mmce.common.tile;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.me.GridAccessException;
import appeng.util.Platform;
import github.kasuminova.mmce.common.tile.base.MEFluidBus;
import hellfirepvp.modularmachinery.common.CommonProxy.GuiType;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.locks.ReadWriteLock;

public class MEFluidOutputBus extends MEFluidBus implements SettingsTransfer {

    public MEFluidOutputBus() {
        this.tanks.setOneFluidOneSlot(true);
    }

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(ItemsMM.meFluidOutputBus);
    }

    @Nullable
    @Override
    public MachineComponent<IFluidHandler> provideComponent() {
        return new MachineComponent.FluidHatch(IOType.OUTPUT) {
            @Override
            public IFluidHandler getContainerProvider() {
                return tanks;
            }
        };
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(@Nonnull final IGridNode node) {
        return this.getPollingTickingRequest();
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull final IGridNode node, final int ticksSinceLastCall) {
        if (!proxy.isActive()) {
            return this.getInactiveTickRateModulation();
        }
        int[] needUpdateSlots = getNeedUpdateSlots();
        if (needUpdateSlots.length == 0) {
            return this.getNoWorkTickRateModulation(ticksSinceLastCall);
        }

        ReadWriteLock rwLock = tanks.getRWLock();

        try {
            rwLock.writeLock().lock();

            inTick = true;
            boolean successAtLeastOnce = false;
            IMEMonitor<IAEFluidStack> inv = proxy.getStorage().getInventory(channel);
            for (final int slot : needUpdateSlots) {
                changedSlots[slot] = false;
                IAEFluidStack fluid = tanks.getFluidInSlot(slot);

                if (fluid == null) {
                    continue;
                }

                IAEFluidStack left = Platform.poweredInsert(proxy.getEnergy(), inv, fluid.copy(), source);

                if (left != null) {
                    if (fluid.getStackSize() != left.getStackSize()) {
                        successAtLeastOnce = true;
                    }
                } else {
                    successAtLeastOnce = true;
                }

                tanks.setFluidInSlot(slot, left);
            }

            inTick = false;
            rwLock.writeLock().unlock();
            return this.getWorkTickRateModulation(successAtLeastOnce, ticksSinceLastCall);
        } catch (GridAccessException e) {
            inTick = false;
            changedSlots = new boolean[TANK_SLOT_AMOUNT];
            rwLock.writeLock().unlock();
            return TickRateModulation.IDLE;
        }
    }

    @Nonnull
    @Override
    public GuiType getMainGuiType() {
        return GuiType.ME_FLUID_OUTPUT_BUS;
    }

    @Override
    protected boolean hasWorkToDo() {
        return this.hasFluid();
    }

    public boolean hasFluid() {
        for (int i = 0; i < tanks.getSlots(); i++) {
            IAEFluidStack stack = tanks.getFluidInSlot(i);
            if (stack != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NBTTagCompound downloadSettings() {
        NBTTagCompound tag = new NBTTagCompound();
        this.writePollingSettings(tag);
        return tag;
    }

    @Override
    public void uploadSettings(NBTTagCompound settings) {
        this.readPollingSettings(settings);
    }


}
