package org.pentaho.di.repository.delegates;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.PermissionMeta;
import org.pentaho.di.repository.Repository;

public class RepositoryPermissionDelegate extends BaseRepositoryDelegate {

	private static Class<?> PKG = PermissionMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public RepositoryPermissionDelegate(Repository repository) {
		super(repository);
	}
	
	public RowMetaAndData getPermission(long id_permission) throws KettleException
	{
		return repository.connectionDelegate.getOneRow(quoteTable(Repository.TABLE_R_PERMISSION), quote(Repository.FIELD_PERMISSION_ID_PERMISSION), id_permission);
	}
	
	public synchronized long getPermissionID(String code) throws KettleException
	{
		return repository.connectionDelegate.getIDWithValue(quoteTable(Repository.TABLE_R_PERMISSION), quote(Repository.FIELD_PERMISSION_ID_PERMISSION), quote(Repository.FIELD_PERMISSION_CODE), code);
	}

	/**
	 * Load a permission from the repository
	 * 
	 * @param id_permission The id of the permission to load
	 * @throws KettleException
	 */
	public PermissionMeta loadPermissionMeta(long id_permission) throws KettleException
	{
		PermissionMeta permissionMeta = new PermissionMeta();
		
		try
		{
			RowMetaAndData r = getPermission(id_permission);
			permissionMeta.setID(id_permission);
			String code = r.getString("CODE", null);
			permissionMeta.setType( PermissionMeta.getType(code) );
			
			return permissionMeta;
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "PermissionMeta.Error.LoadPermisson", Long.toString(id_permission)), dbe);
		}
	}


	public synchronized int getNrPermissions(long id_profile) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+quoteTable(Repository.TABLE_R_PROFILE_PERMISSION)+" WHERE "+quote(Repository.FIELD_PROFILE_PERMISSION_ID_PROFILE)+" = " + id_profile;
		RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

}
