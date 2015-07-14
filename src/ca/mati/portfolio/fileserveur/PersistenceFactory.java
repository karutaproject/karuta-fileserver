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

public class PersistenceFactory {


	protected String persistenceType;

	public PersistenceFactory(String newPersistenceType) {
		persistenceType = newPersistenceType;
	}

	public ApiFilePersistence createFilePersistence( String base, String path, String app ) throws IOException {
		if ( persistenceType.equals(ApiFilePersistence.SQL_PERSISTENCE) ) {
			return new FilePersistenceSql(base, path, app);
		}
		else if ( persistenceType.equals(ApiFilePersistence.FILE_SYSTEM_PERSISTENCE) ) {
				return new FilePersistenceFs(base, path, app);
		}
		else {
			return null;
		}
	}


}
