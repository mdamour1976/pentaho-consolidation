package org.pentaho.di.repository.kdr.delegates;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;

public class KettleDatabaseRepositoryUserDelegate extends KettleDatabaseRepositoryBaseDelegate {

	private static Class<?> PKG = UserInfo.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	public KettleDatabaseRepositoryUserDelegate(KettleDatabaseRepository repository) {
		super(repository);
	}
	
	public RowMetaAndData getUser(ObjectId id_user) throws KettleException
	{
		return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_USER), quote(KettleDatabaseRepository.FIELD_USER_ID_USER), id_user);
	}

	public synchronized ObjectId getUserID(String login) throws KettleException
	{
		return repository.connectionDelegate.getIDWithValue(quoteTable(KettleDatabaseRepository.TABLE_R_USER), quote(KettleDatabaseRepository.FIELD_USER_ID_USER), quote(KettleDatabaseRepository.FIELD_USER_LOGIN), login);
	}

	// Load user with login from repository, don't verify password...
	public UserInfo loadUserInfo(UserInfo userInfo, String login) throws KettleException
	{
		try
		{
			userInfo.setObjectId( getUserID(login) );
			if (userInfo.getObjectId()!=null)
			{
				RowMetaAndData r = getUser(userInfo.getObjectId());
				if (r!=null)
				{
					userInfo.setLogin( r.getString("LOGIN", null) );
					userInfo.setPassword( Encr.decryptPassword( r.getString("PASSWORD", null) ) );
					userInfo.setUsername( r.getString("NAME", null) );
					userInfo.setDescription( r.getString("DESCRIPTION", null) );
					userInfo.setEnabled( r.getBoolean("ENABLED", false) );
					return userInfo;
				}
				else
				{
					throw new KettleDatabaseException(BaseMessages.getString(PKG, "UserInfo.Error.UserNotFound", login));
				}
			}
            else
            {
                throw new KettleDatabaseException(BaseMessages.getString(PKG, "UserInfo.Error.UserNotFound", login));
            }
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "UserInfo.Error.UserNotLoaded", login, ""), dbe);
		}
	}
	

	/** Load user with login from repository and verify the password... 
	 * 
	 * @param rep
	 * @param login
	 * @param passwd
	 * @throws KettleException
	 */
	public UserInfo loadUserInfo(UserInfo userInfo, String login, String passwd) throws KettleException
	{
		loadUserInfo(userInfo, login);
		
		// Verify the password:
		// decrypt password if needed and compare with the one
		String userPass=Encr.decryptPasswordOptionallyEncrypted(passwd);
		if ( userInfo.getObjectId()==null || !userPass.equals(userInfo.getPassword()) )
		{
            throw new KettleDatabaseException(BaseMessages.getString(PKG, "UserInfo.Error.IncorrectPasswortLogin"));
		}
		return userInfo;
	}
	
	public void saveUserInfo(UserInfo userInfo) throws KettleException
	{
		try
		{
			// Do we have a user id already?
			if (userInfo.getObjectId()==null)
			{
				userInfo.setObjectId(getUserID(userInfo.getLogin())); // Get userid in the repository
			}
			
			if (userInfo.getObjectId()==null)
			{
				// This means the login doesn't exist in the database 
				// and we have no id, so we don't know the old one...
				// Just grab the next user ID and do an insert:
				userInfo.setObjectId(repository.connectionDelegate.getNextUserID());
				repository.connectionDelegate.insertTableRow("R_USER", fillTableRow(userInfo)); 
			}
			else
			{
				repository.connectionDelegate.updateTableRow("R_USER", "ID_USER", fillTableRow(userInfo));
			}
			
			// Put a commit behind it!
			repository.commit();
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "UserInfo.Error.SavingUser", userInfo.getLogin()), dbe);
		}
		
	}

	public RowMetaAndData fillTableRow(UserInfo userInfo)
	{
        RowMetaAndData r = new RowMetaAndData();
		r.addValue( new ValueMeta("ID_USER", ValueMetaInterface.TYPE_INTEGER), userInfo.getObjectId() );
		r.addValue( new ValueMeta("LOGIN", ValueMetaInterface.TYPE_STRING), userInfo.getLogin() );
		r.addValue( new ValueMeta("PASSWORD", ValueMetaInterface.TYPE_STRING), Encr.encryptPassword(userInfo.getPassword()) );
		r.addValue( new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), userInfo.getUsername() );
		r.addValue( new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING), userInfo.getDescription() );
		r.addValue( new ValueMeta("ENABLED", ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(userInfo.isEnabled()) );
		return r;
	}

	public synchronized int getNrUsers() throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_USER);
		RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public boolean existsUserInfo(RepositoryElementInterface user) throws KettleException {
		return (user.getObjectId()!=null || getUserID(user.getName())!=null);
	}

	public synchronized void renameUser(ObjectId id_user, String newname) throws KettleException
	{
		String sql = "UPDATE "+quoteTable(KettleDatabaseRepository.TABLE_R_USER)+" SET "+quote(KettleDatabaseRepository.FIELD_USER_NAME)+" = ? WHERE "+quote(KettleDatabaseRepository.FIELD_USER_ID_USER)+" = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_USER_NAME, ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_USER_ID_USER, ValueMetaInterface.TYPE_INTEGER), id_user);

		repository.connectionDelegate.getDatabase().execStatement(sql, table.getRowMeta(), table.getData());
	}


}
