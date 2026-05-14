package github.kasuminova.mmce.client.gui;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import github.kasuminova.mmce.common.container.ContainerMEBusPollingRate;
import github.kasuminova.mmce.common.network.PktOpenMEBusGui;
import github.kasuminova.mmce.common.network.PktSetMEBusPollingRate;
import github.kasuminova.mmce.common.tile.base.MEPollingMachineComponent;
import github.kasuminova.mmce.common.util.PollingRateUtils;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;


public class GuiMEBusPollingRate extends AEBaseGui implements ContainerMEBusPollingRate.IPollingRateListener {

    private GuiTabButton originalGuiBtn;

    private GuiButton plusTick;
    private GuiButton plusSecond;
    private GuiButton plusMinute;
    private GuiButton plusHour;
    private GuiButton plusDay;
    private GuiButton minusTick;
    private GuiButton minusSecond;
    private GuiButton minusMinute;
    private GuiButton minusHour;
    private GuiButton minusDay;

    private final MEPollingMachineComponent host;
    private int currentPollingRate = 0;

    public GuiMEBusPollingRate(final InventoryPlayer inventoryPlayer, final MEPollingMachineComponent host) {
        super(new ContainerMEBusPollingRate(inventoryPlayer, host));
        this.host = host;
    }

    @Override
    public void initGui() {
        super.initGui();

        ((ContainerMEBusPollingRate) this.inventorySlots).setListener(this);

        this.buttonList.add(this.plusTick = new GuiButton(0, this.guiLeft + 23, this.guiTop + 32, 24, 20, "+1t"));
        this.buttonList.add(this.plusSecond = new GuiButton(1, this.guiLeft + 49, this.guiTop + 32, 24, 20, "+1s"));
        this.buttonList.add(this.plusMinute = new GuiButton(2, this.guiLeft + 75, this.guiTop + 32, 24, 20, "+1m"));
        this.buttonList.add(this.plusHour = new GuiButton(3, this.guiLeft + 101, this.guiTop + 32, 24, 20, "+1h"));
        this.buttonList.add(this.plusDay = new GuiButton(4, this.guiLeft + 127, this.guiTop + 32, 24, 20, "+1d"));

        this.buttonList.add(this.minusTick = new GuiButton(5, this.guiLeft + 23, this.guiTop + 69, 24, 20, "-1t"));
        this.buttonList.add(this.minusSecond = new GuiButton(6, this.guiLeft + 49, this.guiTop + 69, 24, 20, "-1s"));
        this.buttonList.add(this.minusMinute = new GuiButton(7, this.guiLeft + 75, this.guiTop + 69, 24, 20, "-1m"));
        this.buttonList.add(this.minusHour = new GuiButton(8, this.guiLeft + 101, this.guiTop + 69, 24, 20, "-1h"));
        this.buttonList.add(this.minusDay = new GuiButton(9, this.guiLeft + 127, this.guiTop + 69, 24, 20, "-1d"));

        this.buttonList.add(this.originalGuiBtn = new GuiTabButton(
            this.guiLeft + 154,
            this.guiTop,
            this.host.getVisualItemStack(),
            this.host.getVisualItemStack().getDisplayName(),
            this.itemRender
        ));
    }

    @Override
    public void onPollingRateChanged(int pollingRate) {
        this.currentPollingRate = pollingRate;
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(I18n.format("gui.mehatch.polling_rate.name"), 8, 6, 0x404040);

        String display = PollingRateUtils.format(this.currentPollingRate);
        int textWidth = this.fontRenderer.getStringWidth(display);
        this.fontRenderer.drawString(display, (this.xSize - textWidth) / 2, 57, 0xFFFFFF);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.bindTexture("guis/priority.png");
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void actionPerformed(@NotNull final GuiButton btn) throws IOException {
        super.actionPerformed(btn);

        if (btn == this.originalGuiBtn) {
            ModularMachinery.NET_CHANNEL.sendToServer(
                new PktOpenMEBusGui(this.host.getPos(), this.host.getMainGuiType())
            );
            return;
        }

        int delta = this.getButtonDelta(btn);
        if (delta != 0) this.addPollingRate(delta);
    }

    private int getButtonDelta(final GuiButton btn) {
        if (btn == this.plusTick)    return  PollingRateUtils.TICKS_PER_TICK;
        if (btn == this.plusSecond)  return  PollingRateUtils.TICKS_PER_SECOND;
        if (btn == this.plusMinute)  return  PollingRateUtils.TICKS_PER_MINUTE;
        if (btn == this.plusHour)    return  PollingRateUtils.TICKS_PER_HOUR;
        if (btn == this.plusDay)     return  PollingRateUtils.TICKS_PER_DAY;
        if (btn == this.minusTick)   return -PollingRateUtils.TICKS_PER_TICK;
        if (btn == this.minusSecond) return -PollingRateUtils.TICKS_PER_SECOND;
        if (btn == this.minusMinute) return -PollingRateUtils.TICKS_PER_MINUTE;
        if (btn == this.minusHour)   return -PollingRateUtils.TICKS_PER_HOUR;
        if (btn == this.minusDay)    return -PollingRateUtils.TICKS_PER_DAY;

        return 0;
    }

    private void addPollingRate(final int delta) {
        long result = (long) this.currentPollingRate + delta;
        result = Math.max(0L, Math.min(Integer.MAX_VALUE, result));
        this.currentPollingRate = (int) result;

        ModularMachinery.NET_CHANNEL.sendToServer(
            new PktSetMEBusPollingRate(this.currentPollingRate)
        );
    }
}