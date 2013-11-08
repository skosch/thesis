/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package db;

public class DbTableView {

	public final static int INDEX_PK = 0;

	public final static int OFFSET_PK = 1;

	public final static String DEFAULT_TOKEN = "?";

	public final String name;

	protected final String[] attributes;

	protected String[] tokens;



	public DbTableView(String name, String[] attributes, String[] tokens) {
		this(name, attributes);
		this.tokens = tokens;
	}

	public DbTableView(String name, String... attributes) {
		super();
		this.name = name;
		this.attributes = attributes;
		if( name ==null || attributes == null || attributes.length == 0) {
			throw new IllegalArgumentException("Try to build an invalid table view");
		}
	}

	public final String getName() {
		return name;
	}

	public String getAttribute(int i) {
		return attributes[i];
	}

	public String getJdbcToken(int i) {
		return tokens == null ? "?" : ":"+tokens[i];
	}

	public int size() {
		return attributes.length;	
	}



	public final String createCountValueQuery( int attr) {
		final StringBuilder b = new StringBuilder();
		b.append("SELECT COUNT(0) FROM ").append(name);
		b.append(" WHERE ").append(getAttribute(attr));
		b.append("=").append(getJdbcToken(attr));
		return new String(b);
	}

	public final String createCountPKQuery() {
		return createCountValueQuery(INDEX_PK);
	}

	private void appendEquality(StringBuilder b, int i) {
		b.append(getAttribute(i)).append("=").append(getJdbcToken(i));
	}

	public final String createfindPrimaryKeyQuery() {
		final StringBuilder b = new StringBuilder();
		b.append("SELECT ").append(getAttribute(INDEX_PK));
		b.append(" FROM ").append(name);
		b.append(" WHERE ");
		int i;
		for ( i = OFFSET_PK; i < size() - 1; i++) {
			appendEquality(b, i);
			b.append(" AND ");
		}
		appendEquality(b, i);
		return new String(b);
	}	

	public final String createSelectQuery(String selection) {
		final StringBuilder b = new StringBuilder();
		b.append("SELECT ").append(selection);
		b.append(" FROM ").append(name);
		if(attributes != null && attributes.length > 0) {
			b.append(" WHERE ");
			int i;
			for ( i = 0; i < size() - 1; i++) {
				appendEquality(b, i);
				b.append(" AND ");
			}
			appendEquality(b, i);
		}
		return new String(b);
	}

	public final String createInsertQuery(boolean generatedPK) {
		final StringBuilder b = new StringBuilder();
		b.append("INSERT INTO ").append(name);
		b.append(" ( ");
		final int offset = generatedPK ? OFFSET_PK : INDEX_PK;
		int i;
		for ( i = offset; i < size() - 1; i++) {
			b.append(getAttribute(i)).append(", ");
		}
		b.append(getAttribute(i));
		b.append(" ) VALUES ( ");
		for (i = offset; i < size() - 1; i++) {
			b.append(getJdbcToken(i)).append(", ");
		}
		b.append(getJdbcToken(i)).append(" )");
		return new String(b);
	}
}
