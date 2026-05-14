package github.kasuminova.mmce.common.tile.base;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.me.GridAccessException;
import github.kasuminova.mmce.common.util.TickManagerHelper;
import hellfirepvp.modularmachinery.common.CommonProxy.GuiType;
import hellfirepvp.modularmachinery.common.data.Config;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;


public abstract class MEPollingMachineComponent extends MEMachineComponent {

    private static final String POLLING_RATE_NBT_KEY = "pollingRate";

    private int pollingRate = 0;
    private boolean isSleeping = false;

    public int getPollingRate() {
        return this.pollingRate;
    }

    public void setPollingRate(int ticks) {
        int sanitized = Math.max(0, ticks);
        if (this.pollingRate == sanitized) return;

        this.pollingRate = sanitized;
        this.isSleeping = false;
        this.markForUpdate();
        this.reRegisterTickable();
    }

    public boolean isAdaptivePolling() {
        return this.pollingRate <= 0;
    }

    @Nonnull
    public TickingRequest getPollingTickingRequest() {
        if (!this.isAdaptivePolling()) {
            this.isSleeping = false;
            return new TickingRequest(this.pollingRate, this.pollingRate, false, true);
        }

        int min = Math.max(1, Config.meHatchAdaptivePollingMin);
        int max = Math.max(min, Config.meHatchAdaptivePollingMax);
        boolean sleepImmediately = !Config.meHatchAdaptivePollingWaitForSleep && !this.hasWorkToDo();

        this.isSleeping = sleepImmediately;
        return new TickingRequest(min, max, sleepImmediately, true);
    }

    @Nonnull
    public TickRateModulation getInactiveTickRateModulation() {
        this.isSleeping = false;
        return this.isAdaptivePolling() ? TickRateModulation.SLOWER : TickRateModulation.IDLE;
    }

    @Nonnull
    public TickRateModulation getNoWorkTickRateModulation(final int ticksSinceLastCall) {
        if (!this.isAdaptivePolling()) {
            this.isSleeping = false;
            return TickRateModulation.SAME;
        }

        if (this.hasWorkToDo()) {
            this.isSleeping = false;
            return TickRateModulation.SLOWER;
        }

        if (!Config.meHatchAdaptivePollingWaitForSleep) {
            this.isSleeping = true;
            return TickRateModulation.SLEEP;
        }

        // Only sleep if we're at max delay and still have no work to do. This prevents the flow of :
        // 1. Work to do, wake up
        // 2. No work to do, sleep (after 5 ticks)
        // 3. Work to do, wake up immediately
        // Which would cause constant sleeping and waking if the speed is at the right interval,
        // negating the optimization that adaptive polling is supposed to provide.
        // We only wake up on recipe completion if we're sleeping.
        int adaptiveMax = Math.max(1, Math.max(Config.meHatchAdaptivePollingMin, Config.meHatchAdaptivePollingMax));
        if (ticksSinceLastCall >= adaptiveMax) {
            this.isSleeping = true;
            return TickRateModulation.SLEEP;
        }

        this.isSleeping = false;
        return TickRateModulation.SLOWER;
    }

    @Nonnull
    public TickRateModulation getWorkTickRateModulation(final boolean didWork, final int ticksSinceLastCall) {
        if (!this.isAdaptivePolling()) {
            this.isSleeping = false;
            return TickRateModulation.SAME;
        }

        if (didWork) {
            this.isSleeping = false;
            return TickRateModulation.FASTER;
        }

        return this.getNoWorkTickRateModulation(ticksSinceLastCall);
    }

    @Override
    public void markNoUpdate() {
        super.markNoUpdate();
        this.wakeUpIfAdaptive();
    }

    public void wakeUpIfAdaptive() {
        if (!this.isAdaptivePolling() || !this.isSleeping || !this.proxy.isReady() || !this.hasWorkToDo()) {
            return;
        }

        try {
            IGridNode node = this.proxy.getNode();
            ITickManager tickManager = this.proxy.getTick();
            if (!tickManager.alertDevice(node)) tickManager.wakeDevice(node);
            this.isSleeping = false;
        } catch (GridAccessException e) {
            // Not connected to the grid yet.
        }
    }

    public void writePollingSettings(NBTTagCompound tag) {
        tag.setInteger(POLLING_RATE_NBT_KEY, this.pollingRate);
    }

    public void readPollingSettings(NBTTagCompound tag) {
        if (tag.hasKey(POLLING_RATE_NBT_KEY)) {
            this.setPollingRate(tag.getInteger(POLLING_RATE_NBT_KEY));
        }
    }

    @Override
    public void readCustomNBT(final NBTTagCompound compound) {
        super.readCustomNBT(compound);

        if (compound.hasKey(POLLING_RATE_NBT_KEY)) {
            this.pollingRate = Math.max(0, compound.getInteger(POLLING_RATE_NBT_KEY));
        }
    }

    @Override
    public void writeCustomNBT(final NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        compound.setInteger(POLLING_RATE_NBT_KEY, this.pollingRate);
    }

    private void reRegisterTickable() {
        if (!(this instanceof IGridTickable tickable) || !this.proxy.isReady()) {
            return;
        }

        TickManagerHelper.reRegisterTickable(this.proxy.getNode(), tickable);
    }

    protected abstract boolean hasWorkToDo();

    @Nonnull
    public abstract GuiType getMainGuiType();
}