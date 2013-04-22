package net.ess3.ranks;

import java.util.LinkedHashMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.ess3.storage.Comment;
import net.ess3.storage.MapValueType;
import net.ess3.storage.StorageObject;


@Data
@EqualsAndHashCode(callSuper = false)
public class Ranks implements StorageObject
{
	public Ranks()
	{
		final RankOptions defaultOptions = new RankOptions();
		ranks.put("default", defaultOptions);
	}
	@Comment(
			"The order of the ranks matters, the ranks are checked from top to bottom.\n"
			 + "All rank names have to be lower case.\n"
			 + "The ranks can be connected to users using the permission essentials.ranks.rankname")
	@MapValueType(RankOptions.class)
	private LinkedHashMap<String, RankOptions> ranks = new LinkedHashMap<String, RankOptions>();
}
