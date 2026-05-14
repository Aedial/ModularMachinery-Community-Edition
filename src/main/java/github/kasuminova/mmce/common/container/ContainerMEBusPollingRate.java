package github.kasuminova.mmce.common.container;

import appeng.api.config.SecurityPermissions;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.util.Platform;
import github.kasuminova.mmce.common.tile.base.MEPollingMachineComponent;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class ContainerMEBusPollingRate extends AEBaseContainer {

    private final MEPollingMachineComponent host;

    @SideOnly(Side.CLIENT)
    private IPollingRateListener listener;

    @GuiSync(0)
    public int pollingRate;

    public ContainerMEBusPollingRate(final InventoryPlayer inventoryPlayer, final MEPollingMachineComponent host) {
        super(inventoryPlayer, host);
        this.host = host;
        this.pollingRate = host.getPollingRate();
    }

    @SideOnly(Side.CLIENT)
    public void setListener(final IPollingRateListener listener) {
        this.listener = listener;
        this.listener.onPollingRateChanged(this.pollingRate);
    }

    public void setPollingRate(final int pollingRate) {
        this.host.setPollingRate(pollingRate);
        this.pollingRate = this.host.getPollingRate();
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        super.detectAndSendChanges();

        if (Platform.isServer()) this.pollingRate = this.host.getPollingRate();
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        if (field.equals("pollingRate") && this.listener != null) {
            this.listener.onPollingRateChanged(this.pollingRate);
        }

        super.onUpdate(field, oldValue, newValue);
    }

    @SideOnly(Side.CLIENT)
    public interface IPollingRateListener {
        void onPollingRateChanged(int pollingRate);
    }
}