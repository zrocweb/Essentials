package net.ess3.commands;

import static net.ess3.I18n._;
import net.ess3.api.IUser;
import net.ess3.converter.EnumConverter;
import net.ess3.utils.LocationUtil;
import org.bukkit.Location;
import org.bukkit.TreeType;


public class Commandbigtree extends EssentialsCommand
{
	private static enum BigTree
	{
		REDWOOD(TreeType.TALL_REDWOOD),
		TREE(TreeType.BIG_TREE),
		JUNGLE(TreeType.JUNGLE);
		private final TreeType bukkitType;

		private BigTree(final TreeType bukkitType)
		{
			this.bukkitType = bukkitType;
		}

		public TreeType getBukkitType()
		{
			return bukkitType;
		}
	}
	private final static EnumConverter<BigTree> BIGTREE_PARSER = EnumConverter.getInstance(BigTree.class);

	@Override
	public void run(final IUser user, final String commandLabel, final String[] args) throws Exception
	{
		final BigTree bigTree = BIGTREE_PARSER.parse(args).getValue();
		final TreeType bukkitTree = bigTree.getBukkitType();

		final Location loc = LocationUtil.getTarget(user.getPlayer());
		final Location safeLocation = LocationUtil.getSafeDestination(loc);
		final boolean success = user.getPlayer().getWorld().generateTree(safeLocation, bukkitTree);
		if (success)
		{
			user.sendMessage(_("§6Big tree spawned."));
		}
		else
		{
			throw new Exception(_("§4Big tree generation failure. Try again on grass or dirt."));
		}
	}
}
