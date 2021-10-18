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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet to store and retrieve files from different persistent backends.
 * Implemented: SQL backend (MySQL) and File System backend
 */

// @WebServlet("/user/*")
// Moved to <servlet> in the web.xml
// @MultipartConfig(maxFileSize = 104857600)    // upload file's size up to 100 MB
// Moved to <multipart-config> in the web.xml
public class FileServerServlet extends HttpServlet
{
	public static final String KARUTA_ENV_HOME = "KARUTA_HOME";
	public static final String KARUTA_PROP_HOME = "karuta.home";
	final Logger logger = LoggerFactory.getLogger(FileServerServlet.class);

	private String serverType;
	private String configPath;
	private String baseFolder;

	public static final boolean TRACE = false;

	private static final long serialVersionUID = 1L;

	// size of byte buffer to send file
	private static final int BUFFER_SIZE = 4096;

	@Override
	public void init() throws ServletException {
		logger.info("FileServerServlet starting");
		serverType = getServletConfig().getServletContext().getInitParameter("serverType");
		try {
			this.loadConfigDirectory();
		} catch (Exception e) {
			logger.error("Error in initializing servlet: ", e);
			throw new ServletException(e);
		}
	}

	private void loadConfigDirectory() throws IOException, InternalError {
		final String configEnvDir = System.getenv(KARUTA_ENV_HOME);
		final String configPropDir = System.getProperty(KARUTA_PROP_HOME);
		// The jvm property override the environment property if set
		final String configDir = (configPropDir != null && !configPropDir.trim().isEmpty()) ? configPropDir : configEnvDir;
		final String servName = getServletConfig().getServletContext().getContextPath();
		if (configDir != null && !configDir.trim().isEmpty()) {
			final File base = new File(configDir.trim());
			if (base.exists() && base.isDirectory() && base.canWrite()) {
				try {
					baseFolder = base.getCanonicalPath();
					configPath = baseFolder + servName +"_config" + File.separatorChar;
					logger.info("FileServerServlet configpath @ " + configPath);
				} catch (IOException e) {
					logger.error("The Configuration directory '" + configDir + "' wasn't defined", e);
					throw e;
				}
			} else {
				throw new IllegalArgumentException("The environment variable '" + KARUTA_ENV_HOME + "' '" + configEnvDir
						+ "' or the jvm property '" + KARUTA_PROP_HOME + "' '" + configPropDir
						+ "' doesn't exist or isn't writable. Please provide a writable directory path !");
			}
		} else {
			throw new IllegalArgumentException("The environment variable '" + KARUTA_ENV_HOME
					+ "' or the jvm property '" + KARUTA_PROP_HOME + "' wasn't set.");
		}
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String uuid = request.getPathInfo();
		uuid = uuid.substring(1);

		try
		{
//			String path = getServletContext().getRealPath("/");
			PersistenceFactory factory = new PersistenceFactory(serverType);
			String app = request.getHeader("app");
			ApiFilePersistence filePersistence = factory.createFilePersistence(baseFolder, configPath, app);

			String message;
			if ( filePersistence.isFileDeleted(uuid) ) {
				response.setStatus(400);
				message = "400 - ERROR - resource: " + uuid + " is already deleted";
			} else {
				message = "200 - " + filePersistence.deleteFile(uuid);
			}

			PrintWriter out = response.getWriter();
			out.println(message);
			out.close();
			request.getInputStream().close();

		} catch (Exception e) {
			response.setStatus(500);
			response.getWriter().print("500 - ERROR: " + e.getMessage());
		}
	}

	@Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
		String uuid = request.getPathInfo();
		uuid = uuid.substring(1);

		if( "".equals(uuid) )
		{
			request.getInputStream().close();
			response.getWriter().close();
			return;
		}

		String split[] = uuid.split("/");
		uuid = split[0];
		String thumbvar=null;
		if( split.length > 1 )
			thumbvar = split[1];
		boolean isThumbnail = false;
		if("thumb".equals(thumbvar))
		isThumbnail = true;

		OutputStream outStream = response.getOutputStream();
		try
		{
//			String path = getServletContext().getRealPath("/");
			PersistenceFactory factory = new PersistenceFactory(serverType);
			String app = request.getHeader("app");
			ApiFilePersistence filePersistence = factory.createFilePersistence(baseFolder, configPath, app);

			InputStream inputStream = filePersistence.getFileInputStream(uuid, isThumbnail);
			int fileLength = inputStream.available();
			if (TRACE) logger.info("fileLength = " + fileLength);

			response.setContentLength(fileLength);

			// write the file to the client

			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = -1;
			int total = 0;

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
				total += bytesRead;
			}
			logger.info("Total = " + total);
			outStream.flush();
			outStream.close();
			inputStream.close();
		} catch (IOException exIO) {
			response.setStatus(500);
			PrintWriter writer = new PrintWriter(outStream);
			writer.print("500 - ERROR - IO Error: " + exIO.getMessage());
		} catch (SQLException exSql ) {
			response.setStatus(500);
			PrintWriter writer = new PrintWriter(outStream);
			writer.print("500 - ERROR - SQL Error: " + exSql.getMessage());
		} catch (Exception e) {
			response.setStatus(500);
			PrintWriter writer = new PrintWriter(outStream);
			writer.print("500 - ERROR: " + e.getMessage());
		}
		finally
		{
			outStream.close();
			request.getInputStream().close();
		}
	}

	@Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
		String uuid = request.getPathInfo();
		uuid = uuid.substring(1);

		/// Ensure file id is a uuid
		if( "".equals(uuid) )
			uuid = UUID.randomUUID().toString();
		else
			try
			{
				UUID test = UUID.fromString(uuid);
				uuid = test.toString();
			}
			catch( IllegalArgumentException e )
			{
				// Invalid create a new one
				uuid = UUID.randomUUID().toString();
			}

		try
		{
//			String path = getServletContext().getRealPath("/");
			PersistenceFactory factory = new PersistenceFactory(serverType);
			String app = request.getHeader("app");
			logger.debug("APP: "+app+" "+configPath);
			ApiFilePersistence filePersistence = factory.createFilePersistence(baseFolder, configPath, app);

			// get input stream of the upload file
			String doCopy = request.getParameter("copy");
			InputStream inputStream = null;
			if( doCopy != null )
			{
				/// Read file directly
				inputStream = filePersistence.getFileInputStream(uuid, false);

				/// Generate a new legit uuid
				uuid = UUID.randomUUID().toString();
			}
			else
				inputStream = request.getInputStream();

			logger.debug("Input stream for: "+uuid);
			uuid = filePersistence.saveFile(uuid, inputStream);

		} catch (SQLException exSql) {
			response.setStatus(500);
			response.getWriter().print("500 - ERROR - IO Error: " + exSql.getMessage());
		} catch (Exception e) {
			response.setStatus(500);
			response.getWriter().print("500 - ERROR: " + e.getMessage());
		}

		PrintWriter writer = response.getWriter();
		writer.write(uuid);
		writer.close();
		request.getInputStream().close();
	}

	@Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
		String uuid = request.getPathInfo();
		uuid = uuid.substring(1);

		/// Ensure file id is a uuid
		if( "".equals(uuid) )
			uuid = UUID.randomUUID().toString();
		else
			try
			{
				UUID test = UUID.fromString(uuid);
				uuid = test.toString();
			}
			catch( IllegalArgumentException e )
			{
				// Invalid create a new one
				uuid = UUID.randomUUID().toString();
			}

		try
		{
//			String path = getServletContext().getRealPath("/");
			PersistenceFactory factory = new PersistenceFactory(serverType);
			String app = request.getHeader("app");
			logger.debug("APP: "+app+" "+configPath);
			ApiFilePersistence filePersistence = factory.createFilePersistence(baseFolder, configPath, app);

			// get input stream of the upload file
			String doCopy = request.getParameter("copy");
			InputStream inputStream = null;
			if( doCopy != null )
			{
				/// Read file directly
				inputStream = filePersistence.getFileInputStream(uuid, false);

				/// Generate a new legit uuid
				uuid = UUID.randomUUID().toString();
			}
			else
				inputStream = request.getInputStream();

			logger.debug("Input stream for: "+uuid);
			uuid = filePersistence.saveFile(uuid, inputStream);

		} catch (SQLException exSql) {
			response.setStatus(500);
			response.getWriter().print("500 - ERROR - IO Error: " + exSql.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(500);
			response.getWriter().print("500 - ERROR: " + e.getMessage());
		}

		PrintWriter writer = response.getWriter();
		writer.write(uuid);
		writer.close();
		request.getInputStream().close();
}

}