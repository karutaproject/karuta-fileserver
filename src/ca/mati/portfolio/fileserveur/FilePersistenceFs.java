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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.UUID;

public class FilePersistenceFs implements ApiFilePersistence {

	public static final boolean TRACE = true;

	static final String AUXILIARY_FILE_ID_NAME = "file_id_name.txt";

	static final int BUFFER_SIZE = 4096;

	private String application = "";
	private final PersistenceConfig persistenceConfig;
	@Override
  public PersistenceConfig getPersistenceConfig() {
		return persistenceConfig;
	}

	public FilePersistenceFs( String base, String path, String app ) throws IOException {
		persistenceConfig = new PersistenceConfig(base, path, "fs");
		application = app;
		/// Ensure folder exists, create directory otherwise
		String location = persistenceConfig.getRepoUrl() + application;
		File file = new File(location);
		if( !file.exists() )
			file.mkdirs();

	}

	@Override
	public InputStream getFileInputStream(String fileUuid) throws SQLException, IOException
	{
		if ( isFileDeleted(fileUuid) ) {
			throw new IOException("resource: " + fileUuid + " was already deleted");
		}

		InputStream fileInputStream = null;

		String filePath = getPersistenceConfig().getRepoUrl() + application + "/" + fileUuid;
		File file = new File(filePath);
		if(!file.exists()){
			throw new IOException("File doesn't exists on server.");
		}
		try{
			fileInputStream = new FileInputStream(file);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new IOException();
		}
		return fileInputStream;
	}

	@Override
	public String saveFile(String fileUuid, InputStream inputStream) throws Exception
	{
		// message will be sent back to client
		if ( (null == fileUuid)  || (fileUuid.length() == 0)  ) {
			// Create the file UUID
			fileUuid = UUID.randomUUID().toString();
		}

		/*
		String fileSaveDirPath = getPersistenceConfig().getRepoUrl() + application + "/" + fileUuid;
		// Creates the save directory if it does not exists
		File fileSaveDir = new File(fileSaveDirPath);
		if (!fileSaveDir.exists()) {
			fileSaveDir.mkdirs();
		}
		//*/

		String filePath = getPersistenceConfig().getRepoUrl() + application +"/" + fileUuid;

		File saveFile = new File(filePath);
		if( !saveFile.exists() )
		  saveFile.createNewFile();

		// opens an output stream for writing file
		FileOutputStream outputStream = new FileOutputStream(saveFile);

		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = -1;
			if (TRACE) System.out.println("Receiving data...");

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			if (TRACE) System.out.println("Data received.");
			outputStream.close();
			inputStream.close();

			if (TRACE) System.out.println("File written to: " + saveFile.getAbsolutePath());

		} catch (Exception e){
			if (TRACE) System.out.println(e.toString());
		}

		return fileUuid;
	}

	@Override
	public String updateFile(String fileUuid, InputStream inputStream) throws Exception
	{
		if ( isFileDeleted(fileUuid) ) {
			throw new IOException("IO error - resource: " + fileUuid + " was already deleted");
		}
		String result_message = saveFile(fileUuid, inputStream);
		return result_message;
	}

	@Override
	public String deleteFile(String fileUuid) throws Exception
	{
		String message = "file " + fileUuid + " not deleted";
		return message;
	}

	@Override
	public boolean isFileDeleted(String fileUuid) throws IOException
	{
		String filePath = getPersistenceConfig().getRepoUrl() + application + "/" + fileUuid;
		if (TRACE) System.out.println("FilePersistenceFs 233 - isFileDeleted : " + filePath);
		File file = new File(filePath);
		if(file.exists()){
			return false;
		}
		return true;
	}
}