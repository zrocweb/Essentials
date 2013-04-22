package net.ess3.settings.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.ess3.storage.Comment;
import net.ess3.storage.StorageObject;


@Data
@EqualsAndHashCode(callSuper = false)
public class Home implements StorageObject
{
	@Comment("When players die, should they respawn at their homes, instead of the spawnpoint?")
	private boolean respawnAtHome = false;
	@Comment(
			"When a player interacts with a bed, should their home be set to that location?\n"
			 + "If you enable this and remove default player access to the /sethome command, \n"
			 + "you can make beds the only way for players to set their home location.")
	private boolean bedSetsHome = false;
	@Comment("If no home is set, should the player be send to spawn, when /home is used.")
	private boolean spawnIfNoHome = false;
	@Comment("Allows people to set their bed at daytime")
	private boolean updateBedAtDaytime = true;
}
