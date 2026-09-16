/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util.databasechange;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import liquibase.datatype.DataTypeFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.h2.H2Connection;
import org.dbunit.operation.DatabaseOperation;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.ext.datatype.core.MySQLBooleanType;

/**
 * Allows to test database upgrade. It accepts initialDatabasePath which should point to the h2
 * liqubaseConnection that will be used for upgrade.
 */
public class DatabaseUpgradeTestUtil {
	
	private final Connection connection;
	
	private final Database liqubaseConnection;
	
	private final DatabaseConnection dbUnitConnection;
	
	private final File tempDir;
	
	private final File tempDBFile;
	
	private final String connectionUrl;
	
	public DatabaseUpgradeTestUtil(String initialDatabasePath) throws IOException, SQLException {
		InputStream databaseInputStream = getClass().getResourceAsStream(initialDatabasePath);
		
		tempDir = File.createTempFile("openmrs-tests-temp-", "");
		tempDir.delete();
		tempDir.mkdir();
		tempDir.deleteOnExit();
		
		tempDBFile = new File(tempDir, "openmrs.mv.db");
		tempDBFile.delete();
		try {
			tempDBFile.createNewFile();
		}
		catch (IOException e) {
			tempDir.delete();
			throw e;
		}
		tempDBFile.deleteOnExit();
		
		FileOutputStream tempDBOutputStream = new FileOutputStream(tempDBFile);
		
		try {
			IOUtils.copy(databaseInputStream, tempDBOutputStream);
			
			databaseInputStream.close();
			tempDBOutputStream.close();
		}
		catch (IOException e) {
			tempDBFile.delete();
			tempDir.delete();
			
			throw e;
		}
		finally {
			IOUtils.closeQuietly(databaseInputStream);
			IOUtils.closeQuietly(tempDBOutputStream);
		}
		
		String databaseUrl = tempDir.getAbsolutePath().replace("\\", "/") + "/openmrs";
		
		connectionUrl = "jdbc:h2:" + databaseUrl + ";MODE=LEGACY;AUTO_RECONNECT=TRUE;DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=TRUE;NON_KEYWORDS=VALUE,KEY,USER,TYPE,FIELD";
		
		connection = DriverManager.getConnection(connectionUrl, "sa", "sa");
		connection.setAutoCommit(true);
		
		try {
			liqubaseConnection = DatabaseFactory.getInstance()
			        .findCorrectDatabaseImplementation(new JdbcConnection(connection));
			liqubaseConnection.setDatabaseChangeLogTableName("LIQUIBASECHANGELOG");
			liqubaseConnection.setDatabaseChangeLogLockTableName("LIQUIBASECHANGELOGLOCK");
		}
		catch (LiquibaseException e) {
			tempDir.delete();
			tempDBFile.delete();
			
			throw new SQLException(e);
		}
		
		try {
			dbUnitConnection = new H2Connection(connection, "PUBLIC");
			dbUnitConnection.getConfig().setProperty(DatabaseConfig.PROPERTY_TABLE_TYPE,
			    new String[] { "TABLE", "BASE TABLE" });
			dbUnitConnection.getConfig().setProperty(DatabaseConfig.FEATURE_CASE_SENSITIVE_TABLE_NAMES, false);
		}
		catch (DatabaseUnitException e) {
			tempDir.delete();
			tempDBFile.delete();
			
			throw new SQLException(e);
		}
		
		restartSequences();
	}
	
	/**
	 * H2 2 does not advance {@code NEXT VALUE FOR} sequences when rows are inserted with explicit
	 * ids, so later generated inserts reuse existing primary keys. Restart each sequence past the
	 * current maximum.
	 */
	private void restartSequences() throws SQLException {
		List<String[]> columns = new ArrayList<>();
		try (PreparedStatement query = connection.prepareStatement(
		        "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_DEFAULT FROM INFORMATION_SCHEMA.COLUMNS "
		                + "WHERE TABLE_SCHEMA = 'PUBLIC' AND COLUMN_DEFAULT LIKE 'NEXT VALUE FOR%'")) {
			try (ResultSet resultSet = query.executeQuery()) {
				while (resultSet.next()) {
					columns.add(new String[] { resultSet.getString(1), resultSet.getString(2), resultSet.getString(3) });
				}
			}
		}
		
		try (Statement statement = connection.createStatement()) {
			for (String[] column : columns) {
				long nextValue = 1L;
				try (ResultSet max = statement
				        .executeQuery("SELECT COALESCE(MAX(\"" + column[1] + "\"), 0) FROM \"" + column[0] + "\"")) {
					if (max.next()) {
						nextValue = max.getLong(1) + 1L;
					}
				}
				String columnDefault = column[2];
				int sequenceStart = columnDefault.lastIndexOf('.');
				if (sequenceStart < 0) {
					continue;
				}
				String sequenceName = columnDefault.substring(sequenceStart + 1).replace("\"", "").trim();
				statement.execute("ALTER SEQUENCE IF EXISTS \"" + sequenceName + "\" RESTART WITH " + nextValue);
			}
		}
	}
	
	public void close() throws SQLException {
		try {
			connection.close();
		}
		finally {
			tempDBFile.delete();
			tempDir.delete();
		}
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public void executeDataset(String path) throws IOException, SQLException {
		InputStream inputStream = getClass().getResourceAsStream(path);
		ReplacementDataSet replacementDataSet;
		try {
			replacementDataSet = new ReplacementDataSet(
			        new FlatXmlDataSet(new InputStreamReader(inputStream), false, true, false));
			
			inputStream.close();
		}
		catch (DataSetException e) {
			throw new IOException(e);
		}
		finally {
			IOUtils.closeQuietly(inputStream);
		}
		replacementDataSet.addReplacementObject("[NULL]", null);
		
		try {
			DatabaseOperation.REFRESH.execute(dbUnitConnection, replacementDataSet);
			
			connection.commit();
			restartSequences();
		}
		catch (DatabaseUnitException e) {
			throw new IOException(e);
		}
	}
	
	public List<Map<String, String>> select(String tableName, String where, String columnName, String... columnNames)
	        throws SQLException {
		String[] allColumnNames = ArrayUtils.addAll(new String[] { columnName }, columnNames);
		
		String sql = "select " + StringUtils.join(allColumnNames, ", ") + " from " + tableName;
		if (!StringUtils.isBlank(where)) {
			sql += " where " + where;
		}
		PreparedStatement query = connection.prepareStatement(sql);
		ResultSet resultSet = query.executeQuery();
		
		List<Map<String, String>> results = new ArrayList<>();
		while (resultSet.next()) {
			Map<String, String> columns = new HashMap<>();
			results.add(columns);
			
			for (int i = 0; i < allColumnNames.length; i++) {
				Object object = resultSet.getObject(i + 1);
				columns.put(allColumnNames[i], object != null ? object.toString() : null);
			}
		}
		
		query.close();
		
		return results;
	}
	
	public void insertGlobalProperty(String globalProperty, String value) throws SQLException {
		PreparedStatement insert = connection
		        .prepareStatement("insert into global_property (property, property_value, uuid) values (?, ?, ?)");
		insert.setString(1, globalProperty);
		insert.setString(2, value);
		insert.setString(3, UUID.randomUUID().toString());
		
		insert.executeUpdate();
		
		insert.close();
		
		connection.commit();
	}
	
	public void upgrade() throws IOException, SQLException {
		upgrade("liquibase-update-to-latest-from-1.9.x.xml");
	}
	
	public void upgrade(String filename) throws IOException, SQLException {
		try {
			Liquibase liquibase = new Liquibase(filename, new ClassLoaderResourceAccessor(getClass().getClassLoader()),
			        liqubaseConnection);
			
			DataTypeFactory dataTypeFactory = DataTypeFactory.getInstance();
			dataTypeFactory.register(new MySQLBooleanType());
			
			liquibase.update("");
			
			connection.commit();
		}
		catch (LiquibaseException e) {
			throw new IOException(e);
		}
	}
}
