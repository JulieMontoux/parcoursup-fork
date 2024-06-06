package fr.parcoursup.algos.propositions.donnees;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.apache.logging.log4j.Logger;

public class ConnectionMonitoring implements Connection {

	private Connection cn;
	
	private Logger logger;
	
	
	public Connection getReelConnection()
	{
		return cn;
	}
	public ConnectionMonitoring(Connection connection, Logger logger) {
		super();
		this.cn = connection;
		this.logger = logger;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return cn.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return cn.isWrapperFor(iface);
	}

	@Override
	public Statement createStatement() throws SQLException {
		return  new StatementMonitoring(cn.createStatement(), logger);
	}

	@Override
	public PreparedStatementMonitoring prepareStatement(String sql) throws SQLException {
		//PreparedStatement ststmt=cn.prepareStatement(sql);
		PreparedStatementMonitoring ststmtM = new PreparedStatementMonitoring(cn.prepareStatement(sql), logger);
		ststmtM.setSql(sql);
		return ststmtM;
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		CallableStatement ststmt= cn.prepareCall(sql);
		//ststmt.setFetchSize(TAILLE_BLOC_APPELS);
		return ststmt;
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		return cn.nativeSQL(sql);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		cn.setAutoCommit(autoCommit);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return cn.getAutoCommit();
	}

	@Override
	public void commit() throws SQLException {
		cn.commit();
	}

	@Override
	public void rollback() throws SQLException {
		cn.rollback();
	}

	@Override
	public void close() throws SQLException {
		//logger.info("Fermeture de la session, derniere rqt "+sql);
		cn.close();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return cn.isClosed();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return cn.getMetaData();
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return cn.isReadOnly();
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		cn.setCatalog(catalog);
	}

	@Override
	public String getCatalog() throws SQLException {
		return cn.getCatalog();
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		cn.setTransactionIsolation(level);
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return cn.getTransactionIsolation();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return cn.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		cn.clearWarnings();
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		//logger.info("Creation du stmt(type6) ");
		Statement ststmt=cn.createStatement(resultSetType, resultSetConcurrency);
		//ststmt.setFetchSize(TAILLE_BLOC_APPELS);
		
		
		return ststmt;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		//logger.info("Creation du stmt(type7) "+sql);
		PreparedStatement ststmt=cn.prepareStatement(sql, resultSetType, resultSetConcurrency);
		//ststmt.setFetchSize(TAILLE_BLOC_APPELS);
		return ststmt;
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		//logger.info("Creation du stmt(type8) "+sql);
		CallableStatement ststmt=cn.prepareCall(sql, resultSetType, resultSetConcurrency);
		//ststmt.setFetchSize(TAILLE_BLOC_APPELS);
		return ststmt;
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return cn.getTypeMap();
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		cn.setTypeMap(map);		
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		cn.setHoldability(holdability);
	}

	@Override
	public int getHoldability() throws SQLException {
		return cn.getHoldability();
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return cn.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return cn.setSavepoint(name);
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		cn.rollback(savepoint);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		cn.releaseSavepoint(savepoint);
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		//logger.info("Creation du stmt(type4) resultSetType "+resultSetType);
		Statement ststmt=cn.createStatement( resultSetType,  resultSetConcurrency,  resultSetHoldability);
		//ststmt.setFetchSize(TAILLE_BLOC_APPELS);
		return ststmt;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		//logger.info("Creation du stmt(type1) "+sql);
		PreparedStatement ststmt=cn.prepareStatement( sql,  resultSetType,  resultSetConcurrency,
				resultSetHoldability);
		//ststmt.setFetchSize(TAILLE_BLOC_APPELS);
		return ststmt;
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		CallableStatement ststmt= cn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		//ststmt.setFetchSize(TAILLE_BLOC_APPELS);
		return ststmt;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		//logger.info("Creation du stmt(type2) "+sql);
		PreparedStatement ststmt=cn.prepareStatement(sql, autoGeneratedKeys);
		//ststmt.setFetchSize(TAILLE_BLOC_APPELS);
		return ststmt;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		//logger.info("Creation du stmt(type3) "+sql);
		PreparedStatement ststmt=cn.prepareStatement(sql, columnIndexes);
		//ststmt.setFetchSize(TAILLE_BLOC_APPELS);
		return ststmt;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		//logger.info("Creation du stmt(type5) "+sql);
		PreparedStatement ststmt=cn.prepareStatement(sql, columnNames);
		//ststmt.setFetchSize(TAILLE_BLOC_APPELS);
		return ststmt;
	}

	@Override
	public Clob createClob() throws SQLException {
		return cn.createClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		return cn.createBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return cn.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return cn.createSQLXML();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return cn.isValid(timeout);
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		cn.setClientInfo(name, value);
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		cn.setClientInfo(properties);
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return cn.getClientInfo(name);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return cn.getClientInfo();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return cn.createArrayOf(typeName, elements);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return cn.createStruct(typeName, attributes);
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		cn.setSchema(schema);
	}

	@Override
	public String getSchema() throws SQLException {
		return cn.getSchema();
	}

	@Override
	public void abort(Executor executor) throws SQLException {
		cn.abort(executor);
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		cn.setNetworkTimeout(executor, milliseconds);
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return cn.getNetworkTimeout();
	}
}
