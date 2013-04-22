package net.ess3.permissions;


public class BasePermission extends AbstractSuperpermsPermission
{
	protected String permission;

	public BasePermission(String base, String permission)
	{
		super();
		this.permission = base + permission;
	}

	@Override
	public String getPermissionName()
	{
		return permission;
	}
}
