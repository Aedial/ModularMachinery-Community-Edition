package github.kasuminova.mmce.common.network;

import github.kasuminova.mmce.common.container.ContainerMEBusPollingRate;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


public class PktSetMEBusPollingRate implements IMessage, IMessageHandler<PktSetMEBusPollingRate, IMessage> {

    private int pollingRate;

    public PktSetMEBusPollingRate() {
    }

    public PktSetMEBusPollingRate(int pollingRate) {
        this.pollingRate = pollingRate;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pollingRate = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.pollingRate);
    }

    @Override
    public IMessage onMessage(final PktSetMEBusPollingRate message, final MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;

        player.getServerWorld().addScheduledTask(() -> {
            if (player.openContainer instanceof ContainerMEBusPollingRate container) {
                container.setPollingRate(Math.max(0, message.pollingRate));
            }
        });

        return null;
    }
}