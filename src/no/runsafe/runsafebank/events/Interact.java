package no.runsafe.runsafebank.events;

import no.runsafe.framework.event.player.IPlayerInteractEvent;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.event.player.RunsafePlayerInteractEvent;

import no.runsafe.framework.server.player.RunsafePlayer;
import org.bukkit.Material;

public class Interact implements IPlayerInteractEvent
{
	public Interact(IOutput output)
	{
		this.output = output;
	}

	@Override
	public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
	{
		if (event.getBlock().getTypeId() == Material.ENDER_CHEST.getId())
		{
			RunsafePlayer player = event.getPlayer();
			String test = player.getInventory().serialize();

			output.write(test);
		}
	}

	private IOutput output;
}
