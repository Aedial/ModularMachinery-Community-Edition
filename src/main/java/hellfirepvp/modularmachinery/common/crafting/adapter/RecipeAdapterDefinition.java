package hellfirepvp.modularmachinery.common.crafting.adapter;

import hellfirepvp.modularmachinery.common.crafting.PreparedRecipe;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import net.minecraft.util.ResourceLocation;

import java.util.List;

/**
 * The description of a recipe adapter.
 *
 * <p>
 * CraftTweaker and other script engines may provide implementations, while
 * the recipe loader only relies on this contract.
 */
public interface RecipeAdapterDefinition extends PreparedRecipe {

    List<RecipeModifier> getModifiers();

    ResourceLocation getAdapterParentMachineName();
}
