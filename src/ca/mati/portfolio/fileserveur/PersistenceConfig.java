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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceConfig {
	final Logger logger = LoggerFactory.getLogger(PersistenceConfig.class);

	public static final boolean TRACE = true;

	public static String PERSISTENCE_CONFIG_FILE = "";

	public PersistenceConfig(String tomcatFolder, String path, String newPersistenceType) throws IOException {
		persistenceType = newPersistenceType;
		baseFolder = tomcatFolder;
//		path = path.replaceFirst(File.separator+"$", "_config"+File.separator);
		PERSISTENCE_CONFIG_FILE = path+"persistence_config.properties";
		logger.info("Persistence location: "+PERSISTENCE_CONFIG_FILE);
		this.setPersistenceConfigFromPropertiesFile();
	}

	private String persistenceType = null;
	public String getPersistenceType() {
		return persistenceType;
	}

	private String baseFolder = null;
	private String repoName = null;
	private void setRepoName(String newRepoName) {
		this.repoName = newRepoName;
	}
	public String getRepoName() {
		return repoName;
	}

	private String repoUrl = null;
	public void setRepoUrl(String newRepoUrl) {
		this.repoUrl = newRepoUrl;
	}
	public String getRepoUrl() {
		return repoUrl;
	}

	private String user = null;
	public void setUser(String newUser) {
		this.user = newUser;
	}
	public String getUser() {
		return user;
	}

	private String password = null;
	public void setPassword(String newPassword) {
		this.password = newPassword;
	}
	public String getPassword() {
		return password;
	}

	public void setPersistenceConfigFromPropertiesFile() throws IOException {
	    Properties prop = new Properties();
	    String fileName = PERSISTENCE_CONFIG_FILE;
	    InputStream is = null;
		try {
			is = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			String errorMessage = "ERROR - persistence_config.properties file not found";
			if (TRACE) System.out.println(errorMessage);
			throw new FileNotFoundException(errorMessage);
		}
	    try {
			prop.load(is);
		} catch (IOException e) {
			String errorMessage = "ERROR - Problem attempting to read persistence_config.properties file";
			if (TRACE) System.out.println(errorMessage);
			throw new IOException(errorMessage);
		}
	    setRepoName(prop.getProperty(getPersistenceType() + ".name"));
	    if (TRACE) System.out.println(getPersistenceType() + ".name: " + getRepoName());
	    String repoUrlRoot = prop.getProperty(getPersistenceType() + ".dir");
	    if (TRACE) System.out.println(getPersistenceType() + ".dir: " + repoUrlRoot);
	    // If not directory not set, put it under {tomcat_root}/{fs.name}
	    String completeRepoUrl = "";
	    if( repoUrlRoot == null || "".equals(repoUrlRoot) )
	    	completeRepoUrl = baseFolder + File.separator + getRepoName();
	    else
	    	completeRepoUrl = repoUrlRoot + File.separator + getRepoName();
	    setRepoUrl(completeRepoUrl);
	    if (TRACE) System.out.println("Complete repo Url: " + getRepoUrl());
	    System.out.println("ATTENTION! - Don't forget to create all the folders along the path: " + getRepoUrl());
	    setUser(prop.getProperty(getPersistenceType() + ".user.id"));
	    if (TRACE) System.out.println(getPersistenceType() + ".user.id: " + getUser());
	    setPassword(prop.getProperty(getPersistenceType() + ".user.password"));
	    if (TRACE) System.out.println(getPersistenceType() + ".user.password: " + getPassword());
	}

}
