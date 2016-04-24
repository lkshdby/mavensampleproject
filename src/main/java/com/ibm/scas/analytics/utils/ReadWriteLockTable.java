package com.ibm.scas.analytics.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.inject.Singleton;

/**
 * A generic lock table that can be used to provide
 * locks for a given key <T>.
 * Calls into this lock table are thread safe. 
 * @param <T> The type to use for the lock table key
 */
@Singleton
public class ReadWriteLockTable<T> {
	private final Map<T, ReadWriteLock> lockTable = new HashMap<T, ReadWriteLock>();
	
	/**
	 * Get the lock for id. Does not add a lock if one doesn't
	 * already exist.
	 * @param id Key to get the lock for.
	 * @return The current lock for id, or null if no lock exists
	 */
	public ReadWriteLock getLock(T id) {
		synchronized(this.lockTable) {
			return this.lockTable.get(id);
		}
	}
	
	/**
	 * Check if a lock exists for id.
	 * @param id Key to check if a lock exists for.
	 * @return True if a lock exists for id, false if not.
	 */
	public boolean lockExists(T id) {
		synchronized(this.lockTable) {
			return this.lockTable.containsKey(id);
		}
	}
	
	/**
	 * Get the lock for id. If it doesn't already exist,
	 * it will be added prior to returning.
	 * @param id Key to add and get the lock for.
	 * @return Get the lock for id. If no lock exists prior
	 * to this call, a new lock is added and returned.
	 */
	public ReadWriteLock getLockWithAdd(T id) {
		synchronized(this.lockTable) {
			if (!this.lockTable.containsKey(id)) {
		        this.lockTable.put(id, new ReentrantReadWriteLock());
			}
	        return this.lockTable.get(id);
		}
	}
	
	/**
	 * Remove the lock for id.
	 * @param id Key to remove the lock for.
	 */
	public void removeLock(T id) {
		synchronized(this.lockTable) {
			this.lockTable.remove(id);
		}
	}
}
