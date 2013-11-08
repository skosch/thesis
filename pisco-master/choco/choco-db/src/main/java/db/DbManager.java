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

import static java.lang.System.getProperty;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

//import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.Solution;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.measure.IMeasures;


public class DbManager {

	public final static int DEFAULT_COLUMN_WIDTH = 12;

	public final static Logger LOGGER = ChocoLogging.getMainLogger();

	public final DriverManagerDataSource dataSource = new DriverManagerDataSource();

	public final SingleConnectionDataSource sdataSource;

	public final JdbcTemplate jdbcTemplate;

	public final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private Integer environmentID;

	public DbManager(File databaseDir, String databaseName) {
		this(OdbHsqldbBridge.makeEmbeddedURL(databaseDir, databaseName));
	}


	public DbManager(String url) {
		super();
		LOGGER.info("fetching data source ...");
		dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
		dataSource.setUrl(url);
		dataSource.setUsername("sa");
		dataSource.setPassword("");
		sdataSource = new SingleConnectionDataSource(DataSourceUtils.getConnection(dataSource), false);
		namedParameterJdbcTemplate = new  NamedParameterJdbcTemplate(sdataSource);
		jdbcTemplate = new JdbcTemplate(sdataSource);
	}


	public final void shutdown() {
		jdbcTemplate.execute("SHUTDOWN");
	}

	public final void commit() {
		jdbcTemplate.execute("COMMIT");
	}


	//*****************************************************************//
	//*******************  QUERIES  ********************************//
	//***************************************************************//

	protected final boolean containsPrimaryKey(final DbTableView dbTable, Object pkValue) {
		final int count = jdbcTemplate.queryForInt( dbTable.createCountPKQuery(), new Object[]{pkValue});
		if( count > 1) {
			LOGGER.warning("query_db...[DUPLICATE PRIMARY KEY].");
		}
		return count > 0;
	}

	protected final boolean containsPrimaryKey(final DbTableView dbTable, SqlParameterSource parameters) {
		final int count = namedParameterJdbcTemplate.queryForInt( dbTable.createCountPKQuery(), parameters);
		if( count > 1) {
			LOGGER.warning("query_db...[DUPLICATE PRIMARY KEY].");
		}
		return count > 0;
	}

	public void insertEntryIfAbsentPK(final DbTableView dbTable,final SqlParameterSource parameters) {
		if( ! containsPrimaryKey(dbTable, parameters)) {
			namedParameterJdbcTemplate.update(dbTable.createInsertQuery(false), parameters);
		}
	}

	public void insertEntryIfAbsentPK(final DbTableView dbTable,final Object... values) {
		if( ! containsPrimaryKey(dbTable, values[DbTableView.INDEX_PK])) {
			jdbcTemplate.update(dbTable.createInsertQuery(false), values);
		}
	}

	public final Integer insertEntryAndRetrieveGPK(final DbTableView dbTable,final SqlParameterSource parameters) {
		namedParameterJdbcTemplate.update(dbTable.createInsertQuery(true), parameters);
		return (Integer) jdbcTemplate.queryForObject(DbTables.CALL_IDENTITY, Integer.class);
	}

	public final Integer insertEntryAndRetrieveGPK(final DbTableView dbTable,final Object... values) {
		jdbcTemplate.update(dbTable.createInsertQuery(true), values);
		return (Integer) jdbcTemplate.queryForObject(DbTables.CALL_IDENTITY, Integer.class);
	}


	public final Integer retrieveGPKOrInsertEntry(final DbTableView dbTable,final SqlParameterSource parameters) {
		try {
			return (Integer) namedParameterJdbcTemplate.queryForObject(dbTable.createfindPrimaryKeyQuery(), parameters, Integer.class);
		} catch (EmptyResultDataAccessException e) {
			return insertEntryAndRetrieveGPK(dbTable, parameters);
		}

	}

	public final Integer retrieveGPKOrInsertEntry(final DbTableView dbTable,final Object... values) {
		try {
			return (Integer) jdbcTemplate.queryForObject(dbTable.createfindPrimaryKeyQuery(), values, Integer.class);
		} catch (EmptyResultDataAccessException e) {
			return insertEntryAndRetrieveGPK(dbTable, values);
		}

	}





	//*****************************************************************//
	//*******************  T_SOLVERS  *********************************//
	//***************************************************************//


	public final Integer getModelID(Solver solver) {
		return solver == null ?
				retrieveGPKOrInsertEntry(DbTables.T_MODELS, DbTables.EMPTY_MODEL) :		
					retrieveGPKOrInsertEntry(DbTables.T_MODELS, new BeanPropertySqlParameterSource(solver));
	}

	public final void insertConfiguration(Integer solverID,  String description) {
		jdbcTemplate.update(DbTables.T_CONFIGURATIONS.createInsertQuery(true), new Object[] { solverID, description});
	}


	public final void insertMeasures(Integer solverID, IMeasures m) {
		Integer measuresID = insertEntryAndRetrieveGPK(DbTables.T_MEASURES, new BeanPropertySqlParameterSource(m));
		jdbcTemplate.update(DbTables.T_LIMITS.createInsertQuery(false), new Object[] { measuresID, solverID});
	}

	public final Integer insertSolver(Solver solver, String instanceName) {
		return insertSolver(solver, instanceName, false);
	}
	
	public final Integer insertSolver(Solver solver, String instanceName, boolean enableSolution) {
		//insert solver
		final Integer solverID = insertEntryAndRetrieveGPK(DbTables.T_SOLVERS, new Object[]{ 
				instanceName, solver.isFeasible() == null ? "UNKNOWN" : solver.isFeasible(), 
				solver.getTimeCount(),	enableSolution ? solver.solutionToString() : "",
				getModelID(solver), getEnvironmentID(), null, new Timestamp(System.currentTimeMillis())}
		);
		//measure insertion order is important in database.
		//insert measures
		for (Solution sol : solver.getSearchStrategy().getStoredSolutions()) {
			insertMeasures(solverID, sol.getMeasures());
		}
		insertMeasures(solverID, solver);
		return solverID;

	}

//
//	public final Integer insertSolver(Solver solver, String instanceName, String status, double runtime, String values, Long seed) {
//		//insert solver
//		final Integer solverID = insertEntryAndRetrieveGPK(DbTables.T_SOLVERS, 
//				new Object[]{ instanceName, status, runtime, values,
//				getModelID(solver), getEnvironmentID(), seed, new Timestamp(System.currentTimeMillis())}
//		);
//		//order is important in database.
//		if( solver != null) {
//			//insert measures
//			for (Solution sol : solver.getSearchStrategy().getStoredSolutions()) {
//				insertMeasures(solverID, sol.getMeasures());
//			}
//			insertMeasures(solverID, solver);
//		}
//		return solverID;
//	}


	//*****************************************************************//
	//*******************  EXECUTION  ********************************//
	//***************************************************************//

	/**
	 * return the maximum memory in Mo.
	 */
	protected final static Integer getMaxMemory() {
		return Integer.valueOf( (int) (Runtime.getRuntime().maxMemory()/DbTables.Mo));
	}



	protected final Integer getRuntimeID() {
		String host;	
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			host = "unknwon-host";
		}
		return retrieveGPKOrInsertEntry(
				DbTables.T_RUNTIMES, 
				host, getProperty("user.name"), getMaxMemory()
		);
	}




	public final Integer getOperatingSystemID() {
		return retrieveGPKOrInsertEntry(
				DbTables.T_OS, 
				getProperty("os.name"), getProperty("os.version"), getProperty("os.arch")
		);
	}

	protected final Integer getJvmID() {
		return retrieveGPKOrInsertEntry(
				DbTables.T_JVM, 
				getProperty("java.runtime.name"), getProperty("java.version"), getProperty("java.vendor")
		);
	}

	public final Integer getEnvironmentID() {
		if(environmentID == null) {
			environmentID = retrieveGPKOrInsertEntry(
					DbTables.T_ENVIRONMENTS, 
					getRuntimeID(), getOperatingSystemID(), getJvmID()
			);
		}
		return environmentID;
	}




	//*****************************************************************//
	//*******************  DISPLAY  ********************************//
	//***************************************************************//

	private final static String format(Object o, int width) {
		String str = o == null ? "null" : o instanceof String ? (String) o : o.toString();
		return str.length() <= width ? String.format("%1$"+ width +"s", str) : str.substring(0, width);
	}

	public final void printTable(final DbTableView dbTable) {
		if(LOGGER.isLoggable(Level.INFO) ) {
			LOGGER.info(displayTable(dbTable));
		}
	}

	public final void printTable(final String dbTable) {
		if(LOGGER.isLoggable(Level.INFO) ) {
			LOGGER.info(displayTable(dbTable));
		}
	}

	public final String displayTable(final String dbTable) {
		return displayTable(dbTable, DEFAULT_COLUMN_WIDTH);
	}

	public final String displayTable(final DbTableView dbTable) {
		return displayTable(dbTable.getName());
	}


	public final String displayTable(final String dbTable, final int columnWidth) {
		final StringBuilder b =new StringBuilder();
		b.append("TABLE: ").append(dbTable).append('\n');
		RowCallbackHandler rch = new RowCallbackHandler() {
			public void processRow(ResultSet res) throws SQLException {
				final ResultSetMetaData rmd = res.getMetaData();
				if(res.getRow() == 1) {
					//insert header
					b.append('|');
					for (int i = 1; i < rmd.getColumnCount() + 1; i++) {
						b.append(format(rmd.getColumnName(i), columnWidth)).append('|');
					}
					b.append('\n');
				}
				//display row
				b.append('|');
				for (int i = 1; i < rmd.getColumnCount() + 1; i++) {
					b.append(format(res.getObject(i),columnWidth)).append('|');
				}
				b.append('\n');
			}
		};
		jdbcTemplate.query( "SELECT * FROM "+dbTable,rch);
		return new String(b);

	}

//	public final void test() {
//
//		getEnvironmentID();		
//		LOGGER.info(displayTable("T_JVM"));
//		LOGGER.info(displayTable("T_OS"));
//		LOGGER.info(displayTable("T_RUNTIMES"));
//		LOGGER.info(displayTable("T_ENVIRONMENTS"));
//
//
//		Solver s = new CPSolver();
//		LOGGER.log(Level.INFO,"MODEL ID: {0}",getModelID(s));
//		s.createBoundIntVar("v1", 0, 5);
//		getModelID(s);
//		LOGGER.info(displayTable("T_MODELS"));
//		LOGGER.log(Level.INFO,"MODEL ID: {0}",getModelID(s));
//		s.createBoundIntVar("v2", 0, 5);
//		LOGGER.info(displayTable("T_MODELS"));
//		//getModelID(s);
//		//jdbcTemplate.execute("COMMIT");
//		LOGGER.log(Level.INFO,"MODEL ID: {0}",getModelID(s));
//		LOGGER.info(displayTable("T_MODELS"));
//
//
//
//		s.solveAll();
//		insertSolver(s, "UNKNOWN");
//		printTable(DbTables.T_SOLVERS);
//		printTable(DbTables.T_MEASURES);
//		//		LOGGER.info(""+insertMeasures(s));
//		//		for (Solution sol : s.getSearchStrategy().getStoredSolutions()) {
//		//			LOGGER.info(""+insertMeasures(sol));
//		//		}
//		//		
//		//
//		//		String sql = "select count(0) from T_MEASURES where OBJECTIVE = :objectiveValue";
//		//
//		//		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(s);
//		//
//		//		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new  NamedParameterJdbcTemplate(dataSource);
//		//		LOGGER.info(displayTable("T_MEASURES"));
//		//
//		//		//LOGGER.info("contains PK :"+containsPrimaryKey("T_MEASURES", "ID", 1));
//		//		//LOGGER.info("contains PK :"+containsPrimaryKey("T_MEASURES", "ID", 10));
//		//		//LOGGER.info("contains PK :"+jdbcTemplate.queryForInt("SELECT COUNT(ID) FROM T_MEASURES WHERE ID=7"));
//		//		ChocoLogging.flushLogs();
//		//		//		LOGGER.info(getLastGeneratedKey().toString());
//		//		//		
//		//		//		Integer execID = getExecutionID("osp1", "SAT");
//		//		//		//insertSolver(s, execID, "");
//		//		//		
//		//		//		LOGGER.log(Level.INFO, "meas ID = {0}", insertMeasures(s));
//		//		//		LOGGER.info(displayTable("T_MEASURES"));
//		//		//		LOGGER.log(Level.INFO, "meas ID = {0}", insertMeasures(s));
//		//		//		LOGGER.info(displayTable("T_MEASURES"));
//		//		//		LOGGER.log(Level.INFO, "meas ID = {0}", insertMeasures(s));
//		//		//		LOGGER.info(displayTable("T_MEASURES"));
//		//		//getSolverID(execID, s);
//		//		//		getExecutionID("jsp1", "MIN", 2);
//		//		//		LOGGER.info(displayTable("T_RESTARTS"));
//		//		//		LOGGER.info(displayTable("T_STRATEGIES"));
//		//		//		LOGGER.info(displayTable("T_MODELS"));
//		//		//		LOGGER.info(displayTable("T_SOLVERS"));
//		//		//		LOGGER.info(displayTable("T_EXECUTIONS"));
//		//		//		LOGGER.info(displayTable("T_LIMITS"));
//		//
//
//		//jdbcTemplate.queryForObject("SELECT LABEL FROM T_PROBLEMS WHERE CATEGORY=NULL" , new Object[]{null}, new int[]{VARCHAR}, String.class);
//		shutdown();
//	}

}
