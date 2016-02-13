package com.lothrazar.cyclicmagic;

import java.util.List;
import com.lothrazar.cyclicmagic.potion.PotionCustom;
import com.lothrazar.cyclicmagic.util.UtilParticle;
import com.lothrazar.cyclicmagic.util.Vector3;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class PotionRegistry {
	// tired;//http://www.minecraftforge.net/wiki/Potion_Tutorial
	public static Potion waterwalk;
	public static Potion slowfall;
	public static Potion frost;
	public static Potion magnet;

	public final static int I = 0;
	public final static int II = 1;
	public final static int III = 2;
	public final static int IV = 3;
	public final static int V = 4;

	public static void register() {
		// initPotionTypesReflection();

		// NEW : the array is now [256]
		// old v's of the game only had 32 so NO room to add modded potions

		registerNewPotionEffects();
	}

	private static void registerNewPotionEffects() {
		// http://www.minecraftforge.net/forum/index.php?topic=11024.0
		// ??? http://www.minecraftforge.net/forum/index.php?topic=12358.0

		PotionRegistry.waterwalk = new PotionCustom(ModMain.cfg.potionIdWaterwalk, new ResourceLocation(Const.MODID, "textures/potions/waterwalk.png"), false, 0, "potion.waterwalk");

		PotionRegistry.slowfall = new PotionCustom(ModMain.cfg.potionIdSlowfall, new ResourceLocation(Const.MODID, "textures/potions/slowfall.png"), false, 0, "potion.slowfall");

		PotionRegistry.frost = new PotionCustom(ModMain.cfg.potionIdFrost, new ResourceLocation(Const.MODID, "textures/potions/frost.png"), false, 0, "potion.frost");
		
		PotionRegistry.magnet = new PotionCustom(ModMain.cfg.potionIdMagnet, new ResourceLocation(Const.MODID, "textures/potions/magnet.png"), false, 0, "potion.magnet");

		// TODO: test out brewing api for these?
	}

	private final static int ITEM_HRADIUS = 20;
	private final static int ITEM_VRADIUS = 4;
	private final static float ITEMSPEED = 1.2F;
	public static void tickMagnet(LivingUpdateEvent event){
		if (event.entityLiving.isPotionActive(PotionRegistry.magnet)) {
			Entity entityIn = event.entityLiving;
			World world = event.entity.worldObj;
		
			BlockPos pos = entityIn.getPosition();
			int x = pos.getX(), y = pos.getY(), z = pos.getZ();
	
			List<EntityItem> found = world.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.fromBounds(x - ITEM_HRADIUS, y - ITEM_VRADIUS, z - ITEM_HRADIUS, x + ITEM_HRADIUS, y + ITEM_VRADIUS, z + ITEM_HRADIUS));
	
			//int moved = 0;
			for (EntityItem eitem : found) {
				Vector3.setEntityMotionFromVector(eitem, x, y, z, ITEMSPEED);
				//moved++;
			}
	
			List<EntityXPOrb> foundExp = world.getEntitiesWithinAABB(EntityXPOrb.class, AxisAlignedBB.fromBounds(x - ITEM_HRADIUS, y - ITEM_VRADIUS, z - ITEM_HRADIUS, x + ITEM_HRADIUS, y + ITEM_VRADIUS, z + ITEM_HRADIUS));
	
			for (EntityXPOrb eitem : foundExp) {
				Vector3.setEntityMotionFromVector(eitem, x, y, z, ITEMSPEED);
				//moved++;
			}
		}
	}

	public static void tickFrost(LivingUpdateEvent event) {
		if (event.entityLiving.isPotionActive(PotionRegistry.frost)) {
			World world = event.entityLiving.worldObj;
			BlockPos pos = event.entityLiving.getPosition();

			if (world.rand.nextDouble() < 0.5) {
				UtilParticle.spawnParticle(world, EnumParticleTypes.SNOWBALL, pos);
			}

			if (world.rand.nextDouble() < 0.3 
					&& world.getBlockState(pos.down()).getBlock() != Blocks.snow_layer 
					&& world.isAirBlock(pos.down()) == false
					&& world.isSideSolid(pos, EnumFacing.UP)) {
				world.setBlockState(pos, Blocks.snow_layer.getDefaultState());
			}
		}
	}

	public static void tickWaterwalk(LivingUpdateEvent event) {
		if (event.entityLiving.isPotionActive(PotionRegistry.waterwalk)) {
			tickLiquidWalk(event, Blocks.water);
		}
	}

	private static void tickLiquidWalk(LivingUpdateEvent event, Block liquid) {
		World world = event.entityLiving.worldObj;

		if (world.getBlockState(event.entityLiving.getPosition().down()).getBlock() == liquid && world.isAirBlock(event.entityLiving.getPosition()) && event.entityLiving.motionY < 0) {
			if (event.entityLiving instanceof EntityPlayer) {
			
				EntityPlayer p = (EntityPlayer) event.entityLiving;
				if (p.isSneaking()){
					return;// let them slip down into it, they cancelling
				}
			}

			event.entityLiving.motionY = 0;// stop falling
			event.entityLiving.onGround = true; // act as if on solid ground
			event.entityLiving.setAIMoveSpeed(0.1F);// walking and not sprinting
													// is this speed
		}
	}

	public static void tickSlowfall(LivingUpdateEvent event) {
		if (event.entityLiving.isPotionActive(PotionRegistry.slowfall)) {
			
			
			if (event.entityLiving instanceof EntityPlayer) 	{
				EntityPlayer p = (EntityPlayer) event.entityLiving;
				if (p.isSneaking()) {
					return;// so fall normally for now
				}
			}
			
			// else: so we are either a non-sneaking player, or a non player
			// entity

			// a normal fall seems to go up to 0, -1.2, -1.4, -1.6, then
			// flattens out at -0.078
			if (event.entityLiving.motionY < 0) {
				event.entityLiving.motionY *= ModMain.cfg.slowfallSpeed;

				event.entityLiving.fallDistance = 0f; // for no fall damage
			}
		}
	}

	public static void addOrMergePotionEffect(EntityLivingBase player, PotionEffect newp) {
		//this could be in a utilPotion class i guess...
		if (player.isPotionActive(newp.getPotionID())) {
			// do not use built in 'combine' function, just add up duration
			PotionEffect p = player.getActivePotionEffect(Potion.potionTypes[newp.getPotionID()]);

			int ampMax = Math.max(p.getAmplifier(), newp.getAmplifier());

			player.addPotionEffect(new PotionEffect(newp.getPotionID(), newp.getDuration() + p.getDuration(), ampMax));
		}
		else {
			player.addPotionEffect(newp);
		}
	}
}
