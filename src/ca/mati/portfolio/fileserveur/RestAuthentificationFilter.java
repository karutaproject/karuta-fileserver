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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestAuthentificationFilter implements Filter {

	/**
	 * @param request The servlet request we are processing
	 * @param response The servlet response we are creating
	 * @param chain The filter chain we are processing
	 *
	 * @exception IOException if an input/output error occurs
	 * @exception ServletException if a servlet error occurs
	 */

	Logger logger = null;
	HttpServletRequest  requestHttp = null;
	HttpServletResponse respHttp = null;
	public static String dbName = "";
	public static String userId = "";
	public static String password = "";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		requestHttp = (HttpServletRequest) request;
		respHttp = (HttpServletResponse) response;

		dbName = requestHttp.getHeader("bd");
		if ( null == dbName ) {
			dbName = "";
		}

		logger = Logger.getLogger("");
		logger.log(Level.INFO, "info recuperees :  " + dbName );
		System.out.println("info recuperees :  " + dbName );

		String authHeader = requestHttp.getHeader("Authorization");
		System.out.println("authHeader :  '" + authHeader + "'");

		logger.log(Level.INFO, "Basic Authentication Authorization - authHeader=" + authHeader );

		if ((authHeader != null)) {
			byte[] base64Token = authHeader.getBytes("UTF-8");

			String token = new String(Base64.decode(base64Token), "UTF-8");

			logger.log(Level.INFO, "Basic Authentication Authorization - base64Token=" + base64Token + " - String token=" + token);

			int delim = token.indexOf(":");

			if (delim != -1) {
				userId = token.substring(0, delim);
				password = token.substring(delim + 1);
			}

			logger.log(Level.INFO, "Basic Authentication Authorization header found for user - userId=" + userId + " - password=" + password);

			if (isAuthorized(userId,password)) {
				//propagate to next element in the filter chain, ultimately JSP/ servlet gets executed
				chain.doFilter(request, response);
			} else {
				//break filter chain, requested JSP/servlet will not be executed
				respHttp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
		}

	}

	/**
	 * logic to accept or reject access to the page, check log in status
	 * @return true when authentication is deemed valid
	 */
	protected boolean isAuthorized(String nomUtilisateur, String motDePasse) {
		if (nomUtilisateur.equals("wad") && motDePasse.equals("mati") ) {
			logger.log(Level.INFO, "utilisateur reconnu");
			System.out.println("utilisateur reconnu");
			System.out.println("request.getRequestURL():" + requestHttp.getRequestURL() );
			System.out.println("request.getServletPath():" + requestHttp.getServletPath() );
			System.out.println("request.getRequestURI():" + requestHttp.getRequestURI() );
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Le filtre de controle d'identification ne
	 * necessite pas d'initialisation
	 */
	@Override
	public void init(FilterConfig filterConfig) {
	}


	/**
	 * Le filtre de controle d'identification ne
	 * necessite pas d'initialisation
	 */
	@Override
	public void destroy() {
	}

}