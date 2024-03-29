package no.runsafe.runsafebank.events;

import no.runsafe.framework.api.event.inventory.IInventoryClick;
import no.runsafe.framework.api.event.inventory.IInventoryClosed;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Sound;
import no.runsafe.framework.minecraft.event.inventory.RunsafeInventoryClickEvent;
import no.runsafe.framework.minecraft.event.inventory.RunsafeInventoryCloseEvent;
import no.runsafe.runsafebank.BankHandler;
import no.runsafe.runsafebank.Config;

public class Inventory implements IInventoryClick, IInventoryClosed
{
	public Inventory(BankHandler bankHandler)
	{
		this.bankHandler = bankHandler;
	}

	@Override
	public void OnInventoryClickEvent(RunsafeInventoryClickEvent event)
	{
		if (!event.getInventory().getName().contains("'s Bank Vault"))
			return;

		IPlayer viewer = event.getWhoClicked();

		if (viewer.hasPermission("runsafe.bank.blacklistOverride"))
			return;

		if (!bankHandler.isViewingBank(viewer)) // Double check someone didn't name a random chest "'s Bank Vault"
			return;

		if (!Config.isBlacklistedItem(event.getCurrentItem()))
			return;

		event.cancel();
		viewer.sendColouredMessage(Config.Messages.getItemNotAllowed());
		viewer.closeInventory();
		viewer.playSound(Sound.Creature.Villager.No);
	}

	@Override
	public void OnInventoryClosed(RunsafeInventoryCloseEvent event)
	{
		if (!event.getInventory().getName().contains("'s Bank Vault"))
			return;

		event.getPlayer().playSound(Sound.Chest.EnderChestClose);
	}

	private final BankHandler bankHandler;
}
