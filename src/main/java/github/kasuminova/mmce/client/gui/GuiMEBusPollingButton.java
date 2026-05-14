package github.kasuminova.mmce.client.gui;

import appeng.api.config.FullnessMode;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
import github.kasuminova.mmce.common.util.PollingRateUtils;
import hellfirepvp.modularmachinery.common.data.Config;
import net.minecraft.client.resources.I18n;

import java.util.function.IntSupplier;


public class GuiMEBusPollingButton extends GuiImgButton {

    private final IntSupplier pollingRateSupplier;

    public GuiMEBusPollingButton(final int x, final int y, final IntSupplier pollingRateSupplier) {
        super(x, y, Settings.FULLNESS_MODE, FullnessMode.FULL);
        this.pollingRateSupplier = pollingRateSupplier;
    }

    @Override
    public String getMessage() {
        int pollingRate = Math.max(0, this.pollingRateSupplier.getAsInt());
        int maxAdaptive = Config.meHatchAdaptivePollingMin;
        int minAdaptive = Config.meHatchAdaptivePollingMax;

        String currentValue = pollingRate <= 0
            ? I18n.format("gui.mehatch.polling_rate.current_adaptive", maxAdaptive, minAdaptive)
            : I18n.format("gui.mehatch.polling_rate.current", PollingRateUtils.format(pollingRate));

        return I18n.format("gui.mehatch.polling_rate.name") + "\n\n"
            + currentValue + "\n"
            + I18n.format("gui.mehatch.polling_rate.tooltip");
    }
}