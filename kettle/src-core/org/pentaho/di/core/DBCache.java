/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;

/**
 * This class caches database queries so that the same query doesn't get called twice.
 * Queries are often launched to the databases to get information on tables etc.
 * 
 * @author Matt
 * @since 15-01-04
 *
 */
public class DBCache
{
	private static DBCache dbCache;
	
	private Hashtable<DBCacheEntry,RowMetaInterface> cache;
	private boolean   usecache;
	
	private LogChannelInterface log;
	
	public void setActive()
	{
		setActive(true);
	}
	
	public void setInactive()
	{
		setActive(false);
	}
	
	public void setActive(boolean act)
	{
		usecache=act;
	}
	
	public boolean isActive()
	{
		return usecache;
	}
	
	public void put(DBCacheEntry entry, RowMetaInterface fields)
	{
		if (!usecache) return;
		
        RowMetaInterface copy = fields.clone();
		cache.put(entry, copy);
		
		//System.out.println("Cache store: "+copy.toStringMeta());
		//System.out.println("Cache store entry="+entry.sql );
	}
	
	/**
	 * Get the fields as a row generated by a database cache entry 
	 * @param entry the entry to look for
	 * @return the fields as a row generated by a database cache entry 
	 */
	public RowMetaInterface get(DBCacheEntry entry)
	{
		if (!usecache) return null;
		
        RowMetaInterface fields = cache.get(entry);
		if (fields!=null)
		{
			fields = fields.clone(); // Copy it again!
					
			//System.out.println("Cache hit!!, fields="+fields.toStringMeta() );
			//System.out.println("Cache hit entry="+entry.sql );
		}

		return fields;
	}
	
	public int size()
	{
		return cache.size();
	}
	
	/**
	 * Clear out all entries of database with a certain name
	 * @param dbname The name of the database for which we want to clear the cache or null if we want to clear it all.
	 */
	public void clear(String dbname)
	{
		if (dbname==null)
		{
			cache = new Hashtable<DBCacheEntry,RowMetaInterface>();
			setActive();
		}
		else
		{
			Enumeration<DBCacheEntry> keys = cache.keys();
			while (keys.hasMoreElements())
			{
				DBCacheEntry entry = (DBCacheEntry)keys.nextElement();
				if (entry.sameDB(dbname))
				{
					// Same name: remove it!
					cache.remove(entry);
				}
			}
		}
	}
	
	public String getFilename()
	{
		return Const.getKettleDirectory()+Const.FILE_SEPARATOR+"db.cache";
	}
	
	private DBCache() throws KettleFileException
	{
		try
		{
			clear(null);
			
            // Serialization support for the DB cache
            //
            log = new LogChannel("DBCache");
            
            String filename = getFilename();
			File file = new File(filename);
			if (file.canRead())
			{
				log.logDetailed("Loading database cache from file: ["+filename+"]");
				
				FileInputStream fis = null;
				DataInputStream dis = null;
				
				try {
					fis = new FileInputStream(file);
					dis = new DataInputStream(fis);
					int counter = 0;
	                try
					{
						while (true)
						{
							DBCacheEntry entry = new DBCacheEntry(dis);
	                        RowMetaInterface row = new RowMeta(dis);
							cache.put(entry, row);
							counter++;
						}
					}
					catch(KettleEOFException eof)
					{
						log.logDetailed("We read "+counter+" cached rows from the database cache!");
					}
				}
				catch(Exception e) {
					throw new Exception(e);
				}
				finally {
					if (dis!=null) dis.close();
				}
			}
			else
			{
				log.logDetailed("The database cache doesn't exist yet.");
			}
		}
		catch(Exception e)
		{
			throw new KettleFileException("Couldn't read the database cache",e);
		}
	}
	
	public void saveCache() throws KettleFileException {
		try {
			// Serialization support for the DB cache
			//
			String filename = getFilename();
			File file = new File(filename);
			if (!file.exists() || file.canWrite()) {
				FileOutputStream fos = null;
				DataOutputStream dos = null;

				try {
					fos = new FileOutputStream(file);
					dos = new DataOutputStream(new BufferedOutputStream(fos, 10000));

					int counter = 0;
					boolean ok = true;

					Enumeration<DBCacheEntry> keys = cache.keys();
					while (ok && keys.hasMoreElements()) {
						// Save the database cache entry
						DBCacheEntry entry = keys.nextElement();
						entry.write(dos);

						// Save the corresponding row as well.
						RowMetaInterface rowMeta = get(entry);
						if (rowMeta != null) {
							rowMeta.writeMeta(dos);
							counter++;
						} else {
							throw new KettleFileException("The database cache contains an empty row. We can't save this!");
						}
					}

					log.logDetailed("We wrote " + counter + " cached rows to the database cache!");
				} catch (Exception e) {
					throw new Exception(e);
				} finally {
					if (dos != null) {
						dos.close();
					}
				}
			} else {
				throw new KettleFileException("We can't write to the cache file: " + filename);
			}
		} catch (Exception e) {
			throw new KettleFileException("Couldn't write to the database cache", e);
		}
	}
	
	/**
	 * Create the database cache instance by loading it from disk
	 * 
	 * @return the database cache instance.
	 * @throws KettleFileException
	 */
	public static final DBCache getInstance()
	{
		if (dbCache!=null) return dbCache;
		try
		{
			dbCache = new DBCache();
		}
		catch(KettleFileException kfe)
		{
			throw new RuntimeException("Unable to create the database cache: "+kfe.getMessage());
		}
		return dbCache;
	}

}
