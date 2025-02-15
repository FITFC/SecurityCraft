package net.geforcemods.securitycraft.misc;

import net.minecraft.world.item.crafting.Ingredient;

public enum PageGroup {
	NONE(false, "", ""), //ignored anyways
	REINFORCED(false, "gui.securitycraft:scManual.reinforced", "securitycraft.reinforced.info"),
	BLOCK_MINES(true, "gui.securitycraft:scManual.block_mines", "securitycraft.block_mines.info"),
	BUTTONS(true, "gui.securitycraft:scManual.reinforced_buttons", "securitycraft.reinforced_buttons.info"),
	PRESSURE_PLATES(true, "gui.securitycraft:scManual.reinforced_pressure_plates", "securitycraft.reinforced_pressure_plates.info"),
	FURNACE_MINES(true, "block.securitycraft.furnace_mine", "securitycraft.furnace_mines.info"),
	KEYCARDS(true, "gui.securitycraft:scManual.keycards", "securitycraft.keycards.info"),
	SECRET_SIGNS(true, "gui.securitycraft:scManual.secret_signs", "securitycraft.secret_signs.info"),
	BLOCK_REINFORCERS(true, "gui.securitycraft:scManual.block_reinforcers", "securitycraft.block_reinforcers.info");

	private final boolean hasRecipeGrid;
	private final String title;
	private final String specialInfoKey;
	private Ingredient items = Ingredient.EMPTY;

	PageGroup(boolean hasRecipeGrid, String title, String specialInfoKey) {
		this.hasRecipeGrid = hasRecipeGrid;
		this.title = title;
		this.specialInfoKey = specialInfoKey;
	}

	public boolean hasRecipeGrid() {
		return hasRecipeGrid;
	}

	public String getTitle() {
		return title;
	}

	public String getSpecialInfoKey() {
		return specialInfoKey;
	}

	public Ingredient getItems() {
		return items;
	}

	public void setItems(Ingredient items) {
		this.items = items;
	}
}
