package com.hbm.render.item;

import org.lwjgl.opengl.GL11;

import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.render.model.ModelRevolver;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

public class ItemRenderRevolver implements IItemRenderer {
	
	protected ModelRevolver swordModel;
	
	public ItemRenderRevolver() {
		swordModel = new ModelRevolver();
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		switch(type) {
		case EQUIPPED:
		case EQUIPPED_FIRST_PERSON:
		case ENTITY:
			return true;
		default: return false;
		}
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return false;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		switch(type) {
		case EQUIPPED_FIRST_PERSON:
			GL11.glPushMatrix();
	            
				if(item.getItem() == ModItems.gun_revolver)
					Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(RefStrings.MODID +":textures/models/ModelRevolver.png"));
				if(item.getItem() == ModItems.gun_revolver_saturnite)
					Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(RefStrings.MODID +":textures/models/ModelRevolverSaturnite.png"));
				GL11.glRotatef(-135.0F, 0.0F, 0.0F, 1.0F);
				GL11.glTranslatef(-0.5F, 0.0F, -0.2F);
				//GL11.glScalef(2.0F, 2.0F, 2.0F);
				GL11.glScalef(0.5F, 0.5F, 0.5F);
	            
				//((EntityPlayer)data[1]).isSwingInProgress = false;
				
				swordModel.render((Entity)data[1], 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
			GL11.glPopMatrix();
			break;
		case EQUIPPED:
		case ENTITY:
			GL11.glPushMatrix();
			if(item.getItem() == ModItems.gun_revolver)
				Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(RefStrings.MODID +":textures/models/ModelRevolver.png"));
			if(item.getItem() == ModItems.gun_revolver_saturnite)
				Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(RefStrings.MODID +":textures/models/ModelRevolverSaturnite.png"));
				GL11.glRotatef(-200.0F, 0.0F, 0.0F, 1.0F);
				GL11.glRotatef(75.0F, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(-30.0F, 1.0F, 0.0F, 0.0F);
				GL11.glTranslatef(0.0F, -0.2F, -0.5F);
				//GL11.glScalef(2.0F, 2.0F, 2.0F);
				swordModel.render((Entity)data[1], 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
			GL11.glPopMatrix();
		default: break;
		}
	}

}
