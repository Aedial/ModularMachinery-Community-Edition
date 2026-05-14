package github.kasuminova.mmce.common.tile;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.IAEFluidTank;
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

public class MEFluidInputBus extends MEFluidBus implements SettingsTransfer {

    private static final String CONFIG_TAG_KEY = "config";

    private final AEFluidInventory config = new AEFluidInventory(this, MEFluidBus.TANK_SLOT_AMOUNT);

    @Override
    public ItemStack getVisualItemStack() {
        return new ItemStack(ItemsMM.meFluidInputBus);
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);
        config.readFromNBT(compound, CONFIG_TAG_KEY);
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        config.writeToNBT(compound, CONFIG_TAG_KEY);
    }

    public IAEFluidTank getConfig() {
        return config;
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(@Nonnull final IGridNode node) {
        return this.getPollingTickingRequest();
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull final IGridNode node, final int ticksSinceLastCall) {
        if (!proxy.isActive()) return this.getInactiveTickRateModulation();

        int[] needUpdateSlots = getNeedUpdateSlots();
        if (needUpdateSlots.length == 0) {
            return this.getNoWorkTickRateModulation(ticksSinceLastCall);
        }

        ReadWriteLock rwLock = tanks.getRWLock();

        try {
            rwLock.writeLock().lock();

            boolean successAtLeastOnce = false;
            inTick = true;
            IMEMonitor<IAEFluidStack> inv = proxy.getStorage().getInventory(channel);
            int capacity = tanks.getCapacity();
            for (final int slot : needUpdateSlots) {
                changedSlots[slot] = false;
                IAEFluidStack cfgStack = config.getFluidInSlot(slot);
                IAEFluidStack invStack = tanks.getFluidInSlot(slot);

                if (cfgStack == null) {
                    if (invStack == null) {
                        continue;
                    }
                    tanks.setFluidInSlot(slot, insertStackToAE(inv, invStack));
                    continue;
                }

                if (!cfgStack.equals(invStack)) {
                    if (invStack != null) {
                        IAEFluidStack stack = insertStackToAE(inv, invStack);
                        if (stack != null) {
                            tanks.setFluidInSlot(slot, stack);
                            continue;
                        }
                    }

                    IAEFluidStack stack = extractStackFromAE(inv, cfgStack.copy().setStackSize(capacity));
                    tanks.setFluidInSlot(slot, stack);
                    if (stack != null) {
                        successAtLeastOnce = true;
                    }
                    continue;
                }

                if (capacity == invStack.getStackSize()) {
                    continue;
                }

                if (capacity > invStack.getStackSize()) {
                    int countToReceive = (int) (capacity - invStack.getStackSize());

                    IAEFluidStack stack = extractStackFromAE(inv, invStack.copy().setStackSize(countToReceive));
                    if (stack != null) {
                        tanks.setFluidInSlot(slot, invStack.copy()
                                                           .setStackSize(invStack.getStackSize() + stack.getStackSize()));
                        successAtLeastOnce = true;
                    }
                    continue;
                }

                int countToExtract = (int) (invStack.getStackSize() - capacity);
                IAEFluidStack stack = insertStackToAE(inv, invStack.copy().setStackSize(countToExtract));
                if (stack == null) {
                    tanks.setFluidInSlot(slot, invStack.copy()
                                                       .setStackSize(invStack.getStackSize() - countToExtract));
                } else {
                    tanks.setFluidInSlot(slot, invStack.copy()
                                                       .setStackSize(invStack.getStackSize() - countToExtract + stack.getStackSize())
                    );
                }
                successAtLeastOnce = true;
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

    @Override
    protected boolean hasWorkToDo() {
        int capacity = tanks.getCapacity();

        for (int slot = 0; slot < config.getSlots(); slot++) {
            IAEFluidStack cfgStack = config.getFluidInSlot(slot);
            IAEFluidStack invStack = tanks.getFluidInSlot(slot);

            if (cfgStack == null) {
                if (invStack != null) return true;
                continue;
            }

            if (invStack == null) return true;

            if (!cfgStack.equals(invStack) || invStack.getStackSize() != capacity) {
                return true;
            }
        }

        return false;
    }

    private IAEFluidStack extractStackFromAE(final IMEMonitor<IAEFluidStack> inv, final IAEFluidStack stack) throws GridAccessException {
        return Platform.poweredExtraction(proxy.getEnergy(), inv, stack.copy(), source);
    }

    private IAEFluidStack insertStackToAE(final IMEMonitor<IAEFluidStack> inv, final IAEFluidStack stack) throws GridAccessException {
        return Platform.poweredInsert(proxy.getEnergy(), inv, stack.copy(), source);
    }

    @Nullable
    @Override
    public MachineComponent<IFluidHandler> provideComponent() {
        return new MachineComponent.FluidHatch(IOType.INPUT) {
            @Override
            public IFluidHandler getContainerProvider() {
                return tanks;
            }

            @Override
            public long getGroupID() {
                return getGroupId();
            }
        };
    }

    @Override
    public boolean canGroupInput() {
        return true;
    }

    @Nonnull
    @Override
    public GuiType getMainGuiType() {
        return GuiType.ME_FLUID_INPUT_BUS;
    }

    @Override
    public NBTTagCompound downloadSettings() {
        NBTTagCompound tag = new NBTTagCompound();
        config.writeToNBT(tag, CONFIG_TAG_KEY);
        this.writePollingSettings(tag);
        return tag;
    }

    @Override
    public void uploadSettings(NBTTagCompound settings) {
        config.readFromNBT(settings, CONFIG_TAG_KEY);
        this.readPollingSettings(settings);
        this.markForUpdate();
    }
}
