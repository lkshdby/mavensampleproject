package com.ibm.scas.analytics.persistence.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ibm.scas.analytics.persistence.PersistenceException;
import com.ibm.scas.analytics.persistence.PersistenceService;
import com.ibm.scas.analytics.persistence.beans.ControlType;
import com.ibm.scas.analytics.persistence.beans.FormField;
import com.ibm.scas.analytics.persistence.beans.StepDetail;
import com.ibm.scas.analytics.persistence.util.WhereClause;
import com.ibm.scas.analytics.persistence.util.WhereInClause;

/**
 * A SQL implementation of the PersistenceService
 * 
 * @author Han Chen
 *
 */
abstract public class SqlPersistence implements PersistenceService {
	private static final Logger logger = Logger.getLogger(SqlPersistence.class);
	
	abstract protected Connection getConnection() throws SQLException;
	
	@Inject private Provider<EntityManager> entityMgr;
	
	
	public SqlPersistence() {
	}
	
	protected EntityManager getEntityMgr() {
		return entityMgr.get();
	}
	
	public void cleanup() {
	}
	
	@Override
	public <T> T getObjectById(Class<T> type, Object id) throws PersistenceException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("%s.getObjectById() type=%s , id=%s", this.getClass().getSimpleName(), type.getSimpleName(), id));
		}
		try {
			return getEntityMgr().find(type, id);
		} catch (EntityNotFoundException e) {
			return null;
		} catch (javax.persistence.PersistenceException e) {
			throw new PersistenceException(e.getLocalizedMessage(), e);
		}
	}
	
	@Override
	public void beginTransaction() {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("%s.beginTransaction()", this.getClass().getSimpleName()));
		}
		if (getEntityMgr().getTransaction().isActive()) {
			return;
		}
		getEntityMgr().getTransaction().begin();
	}
	
	@Override
	public void commitTransaction() {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("%s.commitTransaction()", this.getClass().getSimpleName()));
		}
		if (!getEntityMgr().getTransaction().isActive()) {
			return;
		}
		getEntityMgr().getTransaction().commit();
	}
	
	@Override
	public void rollbackTransaction() {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("%s.rollbackTransaction()", this.getClass().getSimpleName()));
		}
		if (!getEntityMgr().getTransaction().isActive()) {
			return;
		}
		getEntityMgr().getTransaction().rollback();
	}

	@Override
	public <T> void saveObject(Class<T> type, T object) throws PersistenceException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("%s.saveObject(): type=%s", this.getClass().getSimpleName(), type.getSimpleName()));
		}
		try {
			getEntityMgr().persist(object);

		} catch (javax.persistence.PersistenceException e) {
			throw new PersistenceException(e.getLocalizedMessage(), e);
		}
	}
	
	@Override
	public <T> T updateObject(Class<T> type, T object) throws PersistenceException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("%s.updateObject(): type=%s", this.getClass().getSimpleName(), type.getSimpleName()));
		}
		try {
			T returnObj = getEntityMgr().merge(object);
			
			return returnObj;
		} catch (javax.persistence.PersistenceException e) {
			throw new PersistenceException(e.getLocalizedMessage(), e);
		}	
	}
	
	@Override
	public <T> void deleteObject(Class<T> type, T object) throws PersistenceException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("%s.deleteObject(): type=%s", this.getClass().getSimpleName(), type.getSimpleName()));
		}
		try {
			getEntityMgr().remove(object);
			
		} catch (javax.persistence.PersistenceException e) {
			throw new PersistenceException(e.getLocalizedMessage(), e);
		}	
	}

	@Override
	public <T> void deleteObjectById(Class<T> type, Object id) throws PersistenceException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("%s.deleteObjectById(): type=%s, id=%s", this.getClass().getSimpleName(), type.getSimpleName(), id));
		}
		try {
			final T theObj = getEntityMgr().find(type, id);
			getEntityMgr().remove(theObj);

		} catch (EntityNotFoundException e) {
			// do nothing if entity not found
			return;
		} catch (javax.persistence.PersistenceException e) {
			throw new PersistenceException(e.getLocalizedMessage(), e);
		}	
	}
	
	@Override
	public <T> List<T> getAllObjects(Class<T> type) throws PersistenceException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("%s.getAllObjects()  type=%s", this.getClass().getSimpleName(), type.getSimpleName()));
		}
		try {
			final CriteriaBuilder cb = getEntityMgr().getCriteriaBuilder();
			final CriteriaQuery<T> q = cb.createQuery(type);
		
			final Root<T> fromType = q.from(type);
			q.select(fromType);
		
			final TypedQuery<T> theQuery = getEntityMgr().createQuery(q);
		
			return theQuery.getResultList();
		} catch (javax.persistence.PersistenceException e) {
			throw new PersistenceException(e.getLocalizedMessage(), e);
		}	
	}

	@Override
	public <T> List<T> getObjectsBy(Class<T> type, WhereClause... wheres) throws PersistenceException {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("%s.getObjectsBy(): type=%s, where=%s", this.getClass().getSimpleName(), type.getSimpleName(), wheres));
		}
		final StringBuilder queryString = new StringBuilder();
		
		queryString.append("SELECT e FROM ").append(type.getSimpleName()).append(" e");
		
		int argNum = 0;
		for (final WhereClause where : wheres) {
			if (argNum == 0) {
				queryString.append(" WHERE ");
			} else {
				queryString.append(" AND ");
			}
			
			queryString.append("e.").append(where.getColumn());
			
			if (where instanceof WhereInClause) {
				if (!where.isMatch()) {
					queryString.append(" NOT"); 
				}
				queryString.append(" IN ").append(":arg").append(argNum);
			} else {
				if (where.getValue() == null) {
					if (where.isMatch()) {
						queryString.append(" IS NULL");
					} else {
						queryString.append(" IS NOT NULL");
					}
				} else {
					if (where.isMatch()) {
						queryString.append(" = ");
					} else {
						queryString.append(" != ");
					}
					queryString.append(":arg").append(argNum);
				}
			}
			argNum++;
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("%s.getObjectsBy(): JPQL: %s", this.getClass().getSimpleName(), queryString.toString())); 
		}
		
		try {
			final TypedQuery<T> q = getEntityMgr().createQuery(queryString.toString(), type);
			
			argNum = 0;
			for (final WhereClause where : wheres) {
				if (where instanceof WhereInClause) {
					final WhereInClause whereInClause = (WhereInClause)where;
					q.setParameter(String.format("arg%d", argNum), whereInClause.getValues());
				} else {
					if (where.getValue() != null) {
						q.setParameter(String.format("arg%d", argNum), where.getValue());
					}
				}
				argNum++;
			}
			
			return q.getResultList();
		} catch (javax.persistence.PersistenceException e) {
			throw new PersistenceException(e.getLocalizedMessage(), e);
		}	
	}
	@Override
	public List<FormField> getFormFieldsForStep(String stepId) {
		if (logger.isTraceEnabled()) {
			logger.trace("getFormFieldsForStep called, stepId: " + stepId);
		}
		List<FormField> formFields = new ArrayList<FormField>();
		
		final CriteriaBuilder cb = getEntityMgr().getCriteriaBuilder();
		final CriteriaQuery<FormField> q = cb.createQuery(FormField.class);
		
		final Root<FormField> fromFormFields = q.from(FormField.class);
		final Join<FormField, ControlType> controlTypeJoin = fromFormFields.join("controlType", JoinType.INNER);
		
		final Predicate[] conditions = new Predicate[3];
		conditions[0] = cb.equal(fromFormFields.get("stepId"), stepId);
		conditions[1] = cb.equal(fromFormFields.get("isEnabled"), 1);
		conditions[2] = cb.equal(fromFormFields.get("isOnDemand"), 1);
		
		
		
		final TypedQuery<FormField> theQuery = getEntityMgr().createQuery(
				q.select(fromFormFields)
				 .where(conditions));
		formFields = (List<FormField>)theQuery.getResultList();
		
		return formFields;
	}
	
	@Override
	public List<FormField> getAllFormFields(String plugin) {
		if (logger.isTraceEnabled()) {
			logger.trace("getAllFormFields called, plugin: " + plugin);
		}
		List<FormField> formFields = new ArrayList<FormField>();
		
		final CriteriaBuilder cb = getEntityMgr().getCriteriaBuilder();
		final CriteriaQuery<FormField> q = cb.createQuery(FormField.class);
		
		final Root<FormField> fromFormFields = q.from(FormField.class);
		final Join<FormField, ControlType> controlTypeJoin = fromFormFields.join("controlType", JoinType.INNER);
		final Join<FormField, StepDetail> stepDetailsJoin = fromFormFields.join("stepDetail", JoinType.INNER);
		
		final Predicate[] conditions = new Predicate[3];
		conditions[0] = cb.equal(stepDetailsJoin.get("pluginId"), plugin);
		conditions[1] = cb.equal(fromFormFields.get("isEnabled"), 1);
		conditions[2] = cb.equal(fromFormFields.get("isOnDemand"), 0);
		
		
		final TypedQuery<FormField> theQuery = getEntityMgr().createQuery(
				q.select(fromFormFields)
				 .where(conditions));
		formFields = (List<FormField>)theQuery.getResultList();
		
		return formFields;
	}
	
	@Override
	public List<StepDetail> getAllStepDetails(String plugin) {
		if (logger.isTraceEnabled()) {
			logger.trace("getAllStepDetails called, plugin: " + plugin);
		}
		List<StepDetail> stepDetails = new ArrayList<StepDetail>();
		
		final CriteriaBuilder cb = getEntityMgr().getCriteriaBuilder();
		final CriteriaQuery<StepDetail> q = cb.createQuery(StepDetail.class);
		
		final Root<StepDetail> fromStepDetails = q.from(StepDetail.class);
		
		final Predicate[] conditions = new Predicate[2];
		conditions[0] = cb.equal(fromStepDetails.get("pluginId"), plugin);
		conditions[1] = cb.equal(fromStepDetails.get("isEnabled"), 1);
		
		Expression<String> name = fromStepDetails.get("name");
		final TypedQuery<StepDetail> theQuery = getEntityMgr().createQuery(
				q.select(fromStepDetails)
				 .where(conditions).orderBy(cb.asc(name)));
		stepDetails = (List<StepDetail>)theQuery.getResultList();
		
		return stepDetails;
	}
	
	public <T> boolean isPersisted(Class<T> type, T object) throws PersistenceException {
		final EntityManager mgr = this.getEntityMgr();
		return mgr.contains(object);
	}
	
	public <T> void detachEntity(Class<T> type, T object) throws PersistenceException {
		final EntityManager mgr = this.getEntityMgr();
		
		mgr.detach(object);
	}
	
	public void flush() {
		final EntityManager mgr = this.getEntityMgr();
		mgr.flush();
	}
	
	public void clear() {
		final EntityManager mgr = this.getEntityMgr();
		mgr.clear();
	}

}
