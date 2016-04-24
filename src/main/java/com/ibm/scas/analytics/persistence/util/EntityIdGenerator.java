package com.ibm.scas.analytics.persistence.util;

import java.util.UUID;
import java.util.Vector;

import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.sequencing.Sequence;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.config.SessionCustomizer;

public class EntityIdGenerator extends Sequence implements SessionCustomizer {
	private static final long serialVersionUID = 787493240285074740L;

	public EntityIdGenerator() {
		super();
	}
 
	public EntityIdGenerator(String name) {
		super(name);
	}
 
	@Override
	public Object getGeneratedValue(Accessor accessor,
			AbstractSession writeSession, String seqName) {
		return UUID.randomUUID().toString().toLowerCase();
	}
 
	@Override
	public Vector<?> getGeneratedVector(Accessor accessor,
			AbstractSession writeSession, String seqName, int size) {
		return null;
	}
 
 
	@Override
	public boolean shouldAcquireValueAfterInsert() {
		return false;
	}

	@Override
	public boolean shouldUseTransaction() {
		return false;
	}
 
	@Override
	public boolean shouldUsePreallocation() {
		return false;
	}
 
	public void customize(Session session) throws Exception {
		EntityIdGenerator sequence = new EntityIdGenerator("system-uuid");
 
		session.getLogin().addSequence(sequence);
	}

	@Override
	public void onConnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnect() {
		// TODO Auto-generated method stub
		
	}
 
}