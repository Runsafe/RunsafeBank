package no.runsafe.runsafebank;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.event.plugin.IPluginDisabled;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.log.IDebug;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;

public class BankHandler implements IPluginDisabled
{
	public BankHandler(BankRepository bankRepository, IDebug output, IScheduler scheduler, IConsole console)
	{
		this.bankRepository = bankRepository;
		this.debugger = output;
		this.console = console;

		scheduler.startAsyncRepeatingTask(new Runnable()
		{
			@Override
			public void run()
			{
				saveLoadedBanks();
			}
		}, 60, 60);
	}

	public void openBank(IPlayer viewer, IPlayer owner)
	{
		if (!this.loadedBanks.containsKey(owner))
			this.loadBank(owner);

		viewer.openInventory(this.loadedBanks.get(owner));
		debugger.debugFine(String.format("Opening %s's bank for %s", owner.getName(), viewer.getName()));
	}

	private void loadBank(IPlayer owner)
	{
		loadedBanks.put(owner, bankRepository.get(owner));
		debugger.debugFine("Loaded bank from database for " + owner.getName());
	}

	private void saveLoadedBanks()
	{
		List<IPlayer> oldBanks = new ArrayList<>();
		for (Map.Entry<IPlayer, RunsafeInventory> bank : this.loadedBanks.entrySet())
		{
			RunsafeInventory bankInventory = bank.getValue();
			IPlayer bankOwner = bank.getKey();
			this.bankRepository.update(bankOwner, bankInventory);

			this.debugger.debugFine("Saved bank to database: " + bankOwner.getName());

			if (bankInventory.getViewers().isEmpty())
				oldBanks.add(bankOwner);
		}

		for (IPlayer owner: oldBanks)
		{
			this.loadedBanks.remove(owner);
			this.debugger.debugFine("Removing silent bank reference for GC: " + owner.getName());
		}
	}

	private void forceBanksShut()
	{
		for (Map.Entry<IPlayer, RunsafeInventory> bank : this.loadedBanks.entrySet())
		{
			for (IPlayer viewer : bank.getValue().getViewers())
			{
				viewer.sendColouredMessage("&cServer restarting, you have been forced out of your bank.");
				viewer.closeInventory();
			}
		}
	}

	@Override
	public void OnPluginDisabled()
	{
		this.console.logInformation("Shutdown detected, forcing save of all loaded banks.");
		this.forceBanksShut();
		this.saveLoadedBanks();
	}

	private ConcurrentHashMap<IPlayer, RunsafeInventory> loadedBanks = new ConcurrentHashMap<>();
	private BankRepository bankRepository;
	private IDebug debugger;
	private IConsole console;
}
