package com.ibm.scas.analytics.persistence.util;

import java.util.Collection;

/*
 * Represents query of table using a where in clause.
 * e.g. 
 * select * from accounts
 * where owner.id in (1,2,3)
 */
public class WhereInClause extends WhereClause {
	
	private Collection<? extends Object> values;
	
	public WhereInClause(String column, Collection<? extends Object> values) {
		super(column, values);
		this.values = values;
	}
	
	public Collection<? extends Object> getValues() {
		return values;
	}

}
