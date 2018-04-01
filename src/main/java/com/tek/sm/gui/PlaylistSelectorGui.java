package com.tek.sm.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import com.tek.sm.util.InventoryUtils;
import com.tek.sm.util.Reference;

public class PlaylistSelectorGui{

	private Inventory inventory;
	
	public PlaylistSelectorGui() {
		this.inventory = Bukkit.createInventory(null, 27, Reference.CHOOSE_TITLE);
		
		init();
	}

	public void init() {
		this.inventory.setItem(InventoryUtils.slot(4, 2), Reference.BACKMAIN);
		this.inventory.setItem(InventoryUtils.slot(2, 1), Reference.playlist(1));
		this.inventory.setItem(InventoryUtils.slot(4, 1), Reference.playlist(2));
		this.inventory.setItem(InventoryUtils.slot(6, 1), Reference.playlist(3));
	}
	
	public Inventory getInventory() {
		return this.inventory;
	}
	
	public static boolean isChooseGui(Inventory inventory) {
		if(inventory.getTitle() == null) return false;
		return inventory.getTitle().startsWith(Reference.CHOOSE_TITLE);
	}

}
