package crazypants.enderio.machine.painter;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.ModObject;
import crazypants.enderio.crafting.IEnderIoRecipe;
import crazypants.enderio.crafting.IRecipeInput;
import crazypants.enderio.crafting.IRecipeOutput;
import crazypants.enderio.crafting.impl.EnderIoRecipe;
import crazypants.enderio.crafting.impl.RecipeInputClass;
import crazypants.enderio.crafting.impl.RecipeOutput;
import crazypants.enderio.machine.MachineRecipeInput;
import crazypants.enderio.machine.MachineRecipeRegistry;
import crazypants.util.Util;

public class BlockCustomStair extends BlockStairs implements ITileEntityProvider {

  public static BlockCustomStair create() {
    BlockCustomStair result = new BlockCustomStair();
    result.init();
    return result;
  }

  private Icon lastRemovedComponetIcon = null;

  private Random rand = new Random();

  protected BlockCustomStair() {
    super(ModObject.blockCustomStair.actualId, Block.brick, 0);
    setCreativeTab(null);
    setUnlocalizedName(ModObject.blockCustomStair.unlocalisedName);
    setLightOpacity(0);
  }

  private void init() {
    LanguageRegistry.addName(this, ModObject.blockCustomStair.name);
    GameRegistry.registerBlock(this, BlockItemCustomStair.class, ModObject.blockCustomStair.unlocalisedName);
    GameRegistry.registerTileEntity(TileEntityCustomBlock.class, ModObject.blockCustomStair.unlocalisedName + "TileEntity");
    MachineRecipeRegistry.instance.registerRecipe(ModObject.blockPainter.unlocalisedName, new PainterTemplate());
  }

  public static ItemStack createItemStackForSourceBlock(int id, int damage) {
    ItemStack result = new ItemStack(ModObject.blockCustomStair.id, 1, damage);
    PainterUtil.setSourceBlock(result, id, damage);
    return result;
  }

  @SideOnly(Side.CLIENT)
  @Override
  public boolean addBlockHitEffects(World world, MovingObjectPosition target,
      EffectRenderer effectRenderer) {
    Icon tex = null;

    TileEntityCustomBlock cb = (TileEntityCustomBlock)
        world.getBlockTileEntity(target.blockX, target.blockY, target.blockZ);
    Block b = cb.getSourceBlock();
    if(b != null) {
      tex = b.getIcon(ForgeDirection.NORTH.ordinal(), cb.getSourceBlockMetadata());
    }
    if(tex == null) {
      tex = blockIcon;
    }
    lastRemovedComponetIcon = tex;
    addBlockHitEffects(world, effectRenderer, target.blockX, target.blockY,
        target.blockZ, target.sideHit, tex);
    return true;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public boolean addBlockDestroyEffects(World world, int x, int y, int z, int
      meta, EffectRenderer effectRenderer) {
    Icon tex = lastRemovedComponetIcon;
    byte b0 = 4;
    for (int j1 = 0; j1 < b0; ++j1) {
      for (int k1 = 0; k1 < b0; ++k1) {
        for (int l1 = 0; l1 < b0; ++l1) {
          double d0 = x + (j1 + 0.5D) / b0;
          double d1 = y + (k1 + 0.5D) / b0;
          double d2 = z + (l1 + 0.5D) / b0;
          int i2 = this.rand.nextInt(6);
          EntityDiggingFX fx = new EntityDiggingFX(world, d0, d1, d2, d0 - x - 0.5D,
              d1 - y - 0.5D, d2 - z - 0.5D, this, i2, 0).applyColourMultiplier(x, y, z);
          fx.setParticleIcon(tex);
          effectRenderer.addEffect(fx);
        }
      }
    }
    return true;

  }

  @SideOnly(Side.CLIENT)
  private void addBlockHitEffects(World world, EffectRenderer effectRenderer,
      int x, int y, int z, int side, Icon tex) {
    float f = 0.1F;
    double d0 = x + rand.nextDouble() * (getBlockBoundsMaxX() -
        getBlockBoundsMinX() - f * 2.0F) + f + getBlockBoundsMinX();
    double d1 = y + rand.nextDouble() * (getBlockBoundsMaxY() -
        getBlockBoundsMinY() - f * 2.0F) + f + getBlockBoundsMinY();
    double d2 = z + rand.nextDouble() * (getBlockBoundsMaxZ() -
        getBlockBoundsMinZ() - f * 2.0F) + f + getBlockBoundsMinZ();
    if(side == 0) {
      d1 = y + getBlockBoundsMinY() - f;
    } else if(side == 1) {
      d1 = y + getBlockBoundsMaxY() + f;
    } else if(side == 2) {
      d2 = z + getBlockBoundsMinZ() - f;
    } else if(side == 3) {
      d2 = z + getBlockBoundsMaxZ() + f;
    } else if(side == 4) {
      d0 = x + getBlockBoundsMinX() - f;
    } else if(side == 5) {
      d0 = x + getBlockBoundsMaxX() + f;
    }
    EntityDiggingFX digFX = new EntityDiggingFX(world, d0, d1, d2, 0.0D, 0.0D,
        0.0D, this, side, 0);
    digFX.applyColourMultiplier(x, y,
        z).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F);
    digFX.setParticleIcon(tex);
    effectRenderer.addEffect(digFX);
  }

  @Override
  public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side) {
    int meta = world.getBlockMetadata(x, y, z);
    boolean flipped = ((meta & 4) != 0);
    return ((meta & 3) + side.ordinal() == 5) || (side == ForgeDirection.UP && flipped);
  }

  @Override
  public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int blockSide) {
    TileEntity te = world.getBlockTileEntity(x, y, z);
    if(te instanceof TileEntityCustomBlock) {
      TileEntityCustomBlock tef = (TileEntityCustomBlock) te;
      if(tef.getSourceBlockId() > 0 && tef.getSourceBlockId() < Block.blocksList.length) {
        return blocksList[tef.getSourceBlockId()].getIcon(blockSide, tef.getSourceBlockMetadata());
      }
    }
    return blocksList[Block.anvil.blockID].getBlockTexture(world, x, y, z, blockSide);
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void registerIcons(IconRegister iconRegister) {
    blockIcon = iconRegister.registerIcon("enderio:conduitConnector");
  }

  @Override
  public TileEntity createNewTileEntity(World world) {
    return null;
  }

  @Override
  public TileEntity createTileEntity(World world, int metadata) {
    return new TileEntityCustomBlock();
  }

  @Override
  public int getLightOpacity(World world, int x, int y, int z) {
    TileEntity te = world.getBlockTileEntity(x, y, z);
    if(te instanceof TileEntityCustomBlock) {
      TileEntityCustomBlock tef = (TileEntityCustomBlock) te;
      if(tef.getSourceBlockId() > 0) {
        return Math.min(super.getLightOpacity(world, x, y, z), Block.lightOpacity[tef.getSourceBlockId()]);
      }

    }
    return super.getLightOpacity(world, x, y, z);
  }

  @Override
  public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
    int id = -1;
    Block b = PainterUtil.getSourceBlock(stack);
    if(b != null) {
      id = b.blockID;
    }

    TileEntity te = world.getBlockTileEntity(x, y, z);
    if(te instanceof TileEntityCustomBlock) {
      TileEntityCustomBlock tef = (TileEntityCustomBlock) te;
      tef.setSourceBlockId(id);
      tef.setSourceBlockMetadata(PainterUtil.getSourceBlockMetadata(stack));
    }
    world.markBlockForUpdate(x, y, z);
    super.onBlockPlacedBy(world, x, y, z, player, stack);
  }

  /**
   * Remove the tile entity too.
   */
  @Override
  public void breakBlock(World world, int x, int y, int z, int par5, int par6) {

    if(!world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops")) {
      TileEntity te = world.getBlockTileEntity(x, y, z);

      if(te instanceof TileEntityCustomBlock) {
        TileEntityCustomBlock tef = (TileEntityCustomBlock) te;

        ItemStack itemStack = createItemStackForSourceBlock(tef.getSourceBlockId(), tef.getSourceBlockMetadata());

        float f = 0.7F;
        double d0 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
        double d1 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
        double d2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
        EntityItem entityitem = new EntityItem(world, x + d0, y + d1, z + d2, itemStack);
        entityitem.delayBeforeCanPickup = 10;
        world.spawnEntityInWorld(entityitem);

      } else {
        System.out.println("dropBlockAsItem_do: No tile entity.");
      }

    }

    world.removeBlockTileEntity(x, y, z);
  }

  @Override
  public int quantityDropped(Random par1Random) {
    return 0; // need to do custom dropping to maintain source metadata
  }

  public static final class PainterTemplate extends BasicPainterTemplate {

    public PainterTemplate() {
    }

    @Override
    public ItemStack[] getCompletedResult(float chance, MachineRecipeInput... inputs) {
      ItemStack paintSource = MachineRecipeInput.getInputForSlot(1, inputs);
      if(paintSource == null) {
        return new ItemStack[0];
      }
      return new ItemStack[] { createItemStackForSourceBlock(paintSource.itemID, paintSource.getItemDamage()) };
    }

    @Override
    public boolean isValidTarget(ItemStack target) {
      if(target == null) {
        return false;
      }
      Block blk = Util.getBlockFromItemId(target.itemID);
      return blk instanceof BlockStairs;
    }

    @Override
    public List<IEnderIoRecipe> getAllRecipes() {
      IRecipeInput input = new RecipeInputClass<BlockStairs>(new ItemStack(Block.stairsWoodOak), BlockStairs.class, new ItemStack(Block.stairsCobblestone),
          new ItemStack(Block.stairsStoneBrick));
      IRecipeOutput output = new RecipeOutput(new ItemStack(ModObject.blockCustomStair.actualId, 1, 0));

      IEnderIoRecipe recipe = new EnderIoRecipe(getMachineName(), DEFAULT_ENERGY_PER_TASK, input, output);
      return Collections.singletonList(recipe);
    }
  }

}
