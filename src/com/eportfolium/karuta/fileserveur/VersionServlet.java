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
package com.eportfolium.karuta.fileserveur;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.StringJoiner;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(VersionServlet.class);
    private Gson gson = new Gson();
    private BuildInfo buildInfo;

    @Override
    public void init() throws ServletException {
        super.init();
        this.loadBuildedInfo(this.getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String employeeJsonString = this.gson.toJson(buildInfo);

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(employeeJsonString);
        out.flush();
    }

    public BuildInfo getBuildInfo() {
        return buildInfo;
    }

    private void loadBuildedInfo(final ServletContext context){
        InputStream inputStream = context.getResourceAsStream("/META-INF/MANIFEST.MF");
        Manifest manifest = null;
        try {
            manifest = new Manifest(inputStream);
        } catch (IOException e) {
            logger.error("The war have a build problem in generating Manifest.mf file !");
            return;
        }
        Attributes attr = manifest.getMainAttributes();

        BuildInfo bi = new BuildInfo();
        bi.version = attr.getValue("Implementation-Version");
        bi.buildTime = attr.getValue("Build-Time");
        bi.builtBy = attr.getValue("Built-By");
        this.buildInfo = bi;
        logger.info("Loaded from META-INF/MANIFEST.MF build information: {}", this.buildInfo);
    }

    public class BuildInfo {
        protected String version;
        protected String buildTime;
        protected String builtBy;

        public String getVersion() {
            return version;
        }

        public String getBuildTime() {
            return buildTime;
        }

        public String getBuiltBy() {
            return builtBy;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", BuildInfo.class.getSimpleName() + "[", "]")
                    .add("version='" + version + "'")
                    .add("buildTime='" + buildTime + "'")
                    .add("builtBy='" + builtBy + "'")
                    .toString();
        }
    }
}
