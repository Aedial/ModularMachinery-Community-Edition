package hellfirepvp.modularmachinery.common.item;

import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.tiles.base.TileItemBus;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class ItemBusIOToggle extends Item {

    private final boolean disableExternalIO;

    protected ItemBusIOToggle(boolean disableExternalIO) {
        this.disableExternalIO = disableExternalIO;
        setMaxStackSize(1);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUseFirst(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos,
                                           @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ,
                                           @Nonnull EnumHand hand) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof TileItemBus)) return EnumActionResult.PASS;

        // Intercept the click before the bus GUI consumes it.
        if (world.isRemote) return EnumActionResult.SUCCESS;

        TileItemBus itemBus = (TileItemBus) tileEntity;
        boolean changed = itemBus.setExternalIODisabled(disableExternalIO);
        player.sendStatusMessage(new TextComponentTranslation(getMessageKey(changed)), true);
        return EnumActionResult.SUCCESS;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format(getTooltipKey()));
    }

    private String getMessageKey(boolean changed) {
        String prefix = "message.itembus.external_io.";
        if (disableExternalIO) {
            return changed ? prefix + "disabled" : prefix + "already_disabled";
        }

        return changed ? prefix + "enabled" : prefix + "already_enabled";
    }

    private String getTooltipKey() {
        String prefix = "tooltip.itembus.external_io.";
        return disableExternalIO ? prefix + "disable" : prefix + "enable";
    }
}