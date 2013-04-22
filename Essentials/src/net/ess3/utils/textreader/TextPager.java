package net.ess3.utils.textreader;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.ess3.I18n;
import static net.ess3.I18n._;
import org.bukkit.command.CommandSender;


public class TextPager
{
	private final IText text;
	private final boolean onePage;

	public TextPager(final IText text)
	{
		this(text, false);
	}

	public TextPager(final IText text, final boolean onePage)
	{
		this.text = text;
		this.onePage = onePage;
	}

	public void showPage(final String pageStr, final String chapterPageStr, final String commandName, final CommandSender sender)
	{
		List<String> lines = text.getLines();
		List<String> chapters = text.getChapters();
		Map<String, Integer> bookmarks = text.getBookmarks();

		if (bookmarks.isEmpty())
		{
			int page = 1;
			try
			{
				page = Integer.parseInt(pageStr);
			}
			catch (Exception ex)
			{
				page = 1;
			}
			if (page < 1)
			{
				page = 1;
			}

			final int start = onePage ? 0 : (page - 1) * 9;
			final int pages = lines.size() / 9 + (lines.size() % 9 > 0 ? 1 : 0);
			if (!onePage)
			{
				StringBuilder content = new StringBuilder();
				final String[] title = commandName.split(" ", 2);
				if (title.length > 1)
				{
					content.append(I18n.capitalCase(title[0])).append(": ");
					content.append(title[1]);
				}
				else if (chapterPageStr != null)
				{
					content.append(I18n.capitalCase(commandName)).append(": ");
					content.append(chapterPageStr);
				}
				else
				{
					content.append(I18n.capitalCase(commandName));
				}
				sender.sendMessage(_("§e ---- §6{2} §e--§6 Page §c{0}§6/§c{1} §e----", page, pages, content));
			}
			for (int i = start; i < lines.size() && i < start + (onePage ? 20 : 9); i++)
			{
				sender.sendMessage(lines.get(i));
			}
			if (!onePage && page < pages)
			{
				sender.sendMessage(_("§6Type§c /{0} {1} §6to read the next page.", commandName, page + 1));
			}
			return;
		}

		if (pageStr == null || pageStr.isEmpty() || pageStr.matches("[0-9]+"))
		{
			if (lines.get(0).startsWith("#"))
			{
				if (onePage)
				{
					return;
				}
				sender.sendMessage(_("Select chapter:"));
				final StringBuilder sb = new StringBuilder();
				boolean first = true;
				for (String string : chapters)
				{
					if (!first)
					{
						sb.append(", ");
					}
					first = false;
					sb.append(string);
				}
				sender.sendMessage(sb.toString());
				return;
			}
			else
			{
				int page = 1;
				try
				{
					page = Integer.parseInt(pageStr);
				}
				catch (Exception ex)
				{
					page = 1;
				}
				if (page < 1)
				{
					page = 1;
				}

				int start = onePage ? 0 : (page - 1) * 9;
				int end;
				for (end = 0; end < lines.size(); end++)
				{
					String line = lines.get(end);
					if (line.startsWith("#"))
					{
						break;
					}
				}

				int pages = end / 9 + (end % 9 > 0 ? 1 : 0);
				if (!onePage)
				{

					sender.sendMessage(_("§e ---- §6{2} §e--§6 Page §c{0}§6/§c{1} §e----", page, pages, I18n.capitalCase(commandName)));
				}
				for (int i = start; i < end && i < start + (onePage ? 20 : 9); i++)
				{
					sender.sendMessage(lines.get(i));
				}
				if (!onePage && page < pages)
				{
					sender.sendMessage(_("§6Type§c /{0} {1} §6to read the next page.", commandName, page + 1));
				}
				return;
			}
		}

		int chapterpage = 0;
		if (chapterPageStr != null)
		{
			try
			{
				chapterpage = Integer.parseInt(chapterPageStr) - 1;
			}
			catch (Exception ex)
			{
				chapterpage = 0;
			}
			if (chapterpage < 0)
			{
				chapterpage = 0;
			}
		}

		if (!bookmarks.containsKey(pageStr.toLowerCase(Locale.ENGLISH)))
		{
			sender.sendMessage(_("Unknown chapter."));
			return;
		}
		final int chapterstart = bookmarks.get(pageStr.toLowerCase(Locale.ENGLISH)) + 1;
		int chapterend;
		for (chapterend = chapterstart; chapterend < lines.size(); chapterend++)
		{
			final String line = lines.get(chapterend);
			if (line.length() > 0 && line.charAt(0) == '#')
			{
				break;
			}
		}
		final int start = chapterstart + (onePage ? 0 : chapterpage * 9);

		final int page = chapterpage + 1;
		final int pages = (chapterend - chapterstart) / 9 + ((chapterend - chapterstart) % 9 > 0 ? 1 : 0);
		if (!onePage)
		{
			sender.sendMessage(_("§6Chapter {0}, page §c{1}§6 of §c{2}§6:", pageStr, page, pages));
		}
		for (int i = start; i < chapterend && i < start + (onePage ? 20 : 9); i++)
		{
			sender.sendMessage(lines.get(i));
		}
		if (!onePage && page < pages)
		{
			sender.sendMessage(_("§6Type§c /{0} {1} §6to read the next page.", commandName, pageStr + " " + (page + 1)));
		}
	}
}
