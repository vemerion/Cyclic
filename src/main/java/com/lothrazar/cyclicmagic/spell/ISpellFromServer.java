package com.lothrazar.cyclicmagic.spell;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;


public interface ISpellFromServer{

	public  void castFromServer(BlockPos posMouseover, BlockPos posOffset, EntityPlayer p);
}
