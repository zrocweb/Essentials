package net.ess3.settings.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.ess3.storage.Comment;
import net.ess3.storage.StorageObject;


@Data
@EqualsAndHashCode(callSuper = false)
public class Back implements StorageObject
{
	@Comment(
			"Do you want essentials to keep track of previous location for /back in the teleport listener? \n"
			 + "If you set this to true any plugin that uses teleport will have the previous location registered.")
	private boolean registerBackInListener = false;
}
