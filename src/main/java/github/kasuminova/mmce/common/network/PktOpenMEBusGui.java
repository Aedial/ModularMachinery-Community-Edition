package github.kasuminova.mmce.common.network;

import github.kasuminova.mmce.common.integration.ModIntegrationAE2;
import github.kasuminova.mmce.common.tile.base.MEPollingMachineComponent;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy.GuiType;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


public class PktOpenMEBusGui implements IMessage, IMessageHandler<PktOpenMEBusGui, IMessage> {

    private BlockPos pos = BlockPos.ORIGIN;
    private int guiType = 0;

    public PktOpenMEBusGui() {
    }

    public PktOpenMEBusGui(final BlockPos pos, final GuiType guiType) {
        this.pos = pos;
        this.guiType = guiType.ordinal();
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.guiType = buf.readInt();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeInt(this.guiType);
    }

    @Override
    public IMessage onMessage(final PktOpenMEBusGui message, final MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;

        player.getServerWorld().addScheduledTask(() -> {
            GuiType[] guiTypes = GuiType.values();
            if (message.guiType < 0 || message.guiType >= guiTypes.length) {
                return;
            }

            GuiType guiType = guiTypes[message.guiType];
            if (!isSupportedGui(guiType)) return;

            TileEntity tileEntity = player.world.getTileEntity(message.pos);
            if (!(tileEntity instanceof MEPollingMachineComponent host)) {
                return;
            }

            if (ModIntegrationAE2.securityCheck(player, host.getProxy())) {
                return;
            }

            player.openGui(
                ModularMachinery.MODID,
                guiType.ordinal(),
                player.world,
                message.pos.getX(),
                message.pos.getY(),
                message.pos.getZ()
            );
        });

        return null;
    }

    private static boolean isSupportedGui(GuiType guiType) {
        return guiType == GuiType.ME_ITEM_OUTPUT_BUS
            || guiType == GuiType.ME_ITEM_INPUT_BUS
            || guiType == GuiType.ME_FLUID_OUTPUT_BUS
            || guiType == GuiType.ME_FLUID_INPUT_BUS
            || guiType == GuiType.ME_GAS_OUTPUT_BUS
            || guiType == GuiType.ME_GAS_INPUT_BUS
            || guiType == GuiType.ME_BUS_POLLING;
    }
}