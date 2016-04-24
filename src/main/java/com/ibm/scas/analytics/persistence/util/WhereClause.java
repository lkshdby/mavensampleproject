package com.ibm.scas.analytics.persistence.util;

/*
 * Represents query of table using a where clause.
 * e.g. 
 * select * from accounts
 * where owner.id = 1
 */
public class WhereClause {
	
	private String column;
	private Object value;
	private boolean match = true;
	
	public WhereClause(String column, Object value) {
		this.column = column;
		this.value = value;
	}
	
	/**
	 * Set match to false if doing reverse match.  e.g.
	 * WHERE owner.id != <abcd>
	 * 
	 * if 'value' is null, the statement becomes
	 * WHERE owner.id IS NOT NULL
	 * @param column
	 * @param value
	 * @param match
	 */
	public WhereClause(String column, Object value, boolean match) {
		this.column = column;
		this.value = value;
		this.match = match;
	}
	
	public String getColumn() {
		return column;
	}

	public Object getValue() {
		return value;
	}
	
	public boolean isMatch() {
		return match;
	}

}
