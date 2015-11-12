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

import java.io.InputStream;

public interface ApiFilePersistence {

	// persistenceType = sql for sql database, fs for File System or nosql for MongoDB
	public static final String SQL_PERSISTENCE = "sql";
	public static final String FILE_SYSTEM_PERSISTENCE = "fs";
	public static final String NOSQL_PERSISTENCE = "nosql";

	public abstract PersistenceConfig getPersistenceConfig();

	public abstract InputStream getFileInputStream(String fileUuid, boolean thumbnail) throws Exception;

	public abstract String saveFile(String fileUuid, InputStream inputStream) throws Exception;

	public abstract String updateFile(String fileUuid, InputStream inputStream) throws Exception;

	public abstract String deleteFile(String fileUuid) throws Exception;

	public abstract boolean isFileDeleted(String fileUuid) throws Exception;

}