/* =======================================================
	Copyright 2014 - ePortfolium - Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
   ======================================================= */

package ca.mati.portfolio.fileserveur;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.mysql.jdbc.Driver;

public class FilePersistenceSql implements ApiFilePersistence {

	public static final boolean TRACE = true;

	private String application = "";
	private final PersistenceConfig persistenceConfig;
	@Override
  public PersistenceConfig getPersistenceConfig() {
		return persistenceConfig;
	}

	public FilePersistenceSql( String path, String app ) throws IOException {
		application = app;
		persistenceConfig = new PersistenceConfig(path, "sql");
	}

	@Override
	public InputStream getFileInputStream(String fileUuid) throws SQLException {

		Connection connection = null; // connection to the database

		if ( isFileDeleted(fileUuid) ) {
			throw new SQLException("resource: " + fileUuid + " was deleted");
		}

		InputStream inputStream = null;
		try {

			// connects to the database
			DriverManager.registerDriver(new Driver());
			connection = DriverManager.getConnection(getPersistenceConfig().getRepoUrl(),
					getPersistenceConfig().getUser(), getPersistenceConfig().getPassword());

			// queries the database
			String sql = "SELECT * FROM `files_table` WHERE `user_id` = ? AND `file_uuid` = ?";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(2, fileUuid);

			ResultSet result = statement.executeQuery();
			if (result.next()) {
				// gets file blob data
				Blob blob = result.getBlob("file_data");
				inputStream = blob.getBinaryStream();
			} else {
				throw new SQLException();
			}
		} catch (SQLException ex) {
			throw new SQLException();
		} finally {
			if (connection != null) {
				// closes the database connection
				try {
					connection.close();
				} catch (SQLException ex) {
					throw new SQLException();
				}
			}
		}
		return inputStream;
	}

	@Override
	public String saveFile(String fileUuid, InputStream inputStream) throws SQLException
	{
		Connection connection = null; // connection to the database
		String message = null;  // message will be sent back to client
		String sql = null;

		if ( (null == fileUuid)  || (fileUuid.length() == 0)  ) {
			// Create the file UUID
			fileUuid = UUID.randomUUID().toString();
			// constructs INSERT SQL statement
			sql = "INSERT INTO files_table " +
					"(`file_uuid`,`user_id`,`file_name`, `file_data`) " +
					"VALUES ( ?, ?, ?, ?)";
		} else {
			// constructs UPDATE SQL statement
			sql = "UPDATE files_table SET " +
					"`file_uuid` = ?, `user_id` = ?, `file_name` = ?, `file_data` = ? " +
					" WHERE `user_id`=" + "\"" + "" + "\"" + " AND `file_uuid`=" + "\"" + fileUuid + "\"";
		}

		try {
			// connects to the database
			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
			connection = DriverManager.getConnection(getPersistenceConfig().getRepoUrl(),
					getPersistenceConfig().getUser(), getPersistenceConfig().getPassword());

			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, fileUuid);

			if (inputStream != null) {
				// fetches input stream of the upload file for the blob column
				statement.setBlob(4, inputStream);
			}

			// sends the statement to the database server
			int row = statement.executeUpdate();
			if (row > 0) {
				System.out.println("File uploaded and saved into database");
				message = fileUuid;
			}
		} catch (SQLException ex) {
			message = ex.getMessage();
		} finally {
			if (connection != null) {
				// closes the database connection
				try {
					connection.close();
				} catch (SQLException ex) {
					message = ex.getMessage();
				}
			}
		}
		return message;
	}

	@Override
	public String updateFile(String fileUuid, InputStream inputStream) throws SQLException
	{
		if ( isFileDeleted(fileUuid) ) {
			throw new SQLException("resource: " + fileUuid + " was already deleted");
		}
		String result_message = saveFile(fileUuid, inputStream);
		return result_message;
	}

	@Override
	public String deleteFile(String fileUuid) throws SQLException
	{
		if ( isFileDeleted(fileUuid) ) {
			throw new SQLException("resource: " + fileUuid + " was already deleted");
		}
		Connection connection = null; // connection to the database
		String message = null;  // message will be sent back to client
		String sql = null;
		// constructs UPDATE SQL statement
		sql = "UPDATE `files_table` SET `deleted` = ? " +
					" WHERE `user_id`=" + "\"" + "" + "\"" + " AND `file_uuid`=" + "\"" + fileUuid + "\"";
		try {
			// connects to the database
			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
			connection = DriverManager.getConnection(getPersistenceConfig().getRepoUrl(),
					getPersistenceConfig().getUser(), getPersistenceConfig().getPassword());

			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1, 1);

			// sends the statement to the database server
			int row = statement.executeUpdate();
			if (row > 0) {
				message = "Resource: " +  fileUuid + " marked 'deleted' into the database";
				System.out.println(message);
			}
		} catch (SQLException ex) {
			message = ex.getMessage();
		} finally {
			if (connection != null) {
				// closes the database connection
				try {
					connection.close();
				} catch (SQLException ex) {
					message = ex.getMessage();
				}
			}
		}
		return message;
	}

	@Override
	public boolean isFileDeleted(String fileUuid) throws SQLException
	{
		boolean fileDeleted = false;
		Connection connection = null; // connection to the database
		try {

			// connects to the database
			DriverManager.registerDriver(new Driver());
			connection = DriverManager.getConnection(getPersistenceConfig().getRepoUrl(),
					getPersistenceConfig().getUser(), getPersistenceConfig().getPassword());

			// queries the database
			String sql = "SELECT * FROM `files_table` WHERE `user_id` = ? AND `file_uuid` = ?";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(2, fileUuid);

			ResultSet result = statement.executeQuery();
			if (result.next()) {
				// gets file deleted state
				if ( Integer.parseInt(result.getString("deleted")) == 1 ) {
					System.out.println("Resource deleted: " + fileUuid);
					fileDeleted = true;
				}
			} else {
				throw new SQLException();
			}
		} catch (SQLException ex) {
		} finally {
			if (connection != null) {
				// closes the database connection
				try {
					connection.close();
				} catch (SQLException ex) {
				}
			}
		}
		return fileDeleted;
	}

}
