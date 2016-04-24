package com.ibm.scas.analytics.persistence;

import java.util.List;
import java.util.Map;

import com.ibm.scas.analytics.persistence.beans.FormField;
import com.ibm.scas.analytics.persistence.beans.StepDetail;
import com.ibm.scas.analytics.persistence.util.WhereClause;

/**
 * abstract definition of a persistence service
 * 
 * @author Han Chen
 *
 */
public interface PersistenceService {

	Map<String,String> getConnectionProperties();
	
	<T> List<T> getObjectsBy(Class<T> type, WhereClause... where) throws PersistenceException;
	<T> List<T> getAllObjects(Class<T> type) throws PersistenceException;
	<T> T getObjectById(Class<T> type, Object id) throws PersistenceException;
	<T> void saveObject(Class<T> type, T object) throws PersistenceException;
	<T> T updateObject(Class<T> type, T object) throws PersistenceException;
	<T> void deleteObject(Class<T> type, T object) throws PersistenceException;
	<T> void deleteObjectById(Class<T> type, Object id) throws PersistenceException;
	
	void beginTransaction() throws PersistenceException;
	void commitTransaction() throws PersistenceException;
	void rollbackTransaction();
	void flush();
	void clear();
	void cleanup();
	
	<T> boolean isPersisted(Class<T> type, T object) throws PersistenceException;
	<T> void detachEntity(Class<T> type, T object) throws PersistenceException;
	
	//Wizard Related
	List<FormField> getFormFieldsForStep(String stepId);
	List<FormField> getAllFormFields(String plugin);
	List<StepDetail> getAllStepDetails(String plugin);
}