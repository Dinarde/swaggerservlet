package com.crivano.swaggerservlet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.crivano.restservlet.IRestAction;
import com.crivano.restservlet.RestServlet;

public class SwaggerServlet extends RestServlet {
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// Return the swagger.yaml that is placed at
		// src/main/webapp/{servletpath}/swagger.yaml
		//
		if (req.getPathInfo().equals("/swagger.yaml")) {
			InputStream is = this.getServletContext().getResourceAsStream(
					req.getServletPath() + req.getPathInfo());
			String sSwagger = convertStreamToString(is);
			byte[] ab = sSwagger.getBytes();
			resp.setContentType("text/x-yaml");
			resp.setContentLength(ab.length);
			resp.getOutputStream().write(ab);
			resp.getOutputStream().flush();
		} else
			super.doGet(req, resp);
	}

	static String convertStreamToString(java.io.InputStream is) {
		try (java.util.Scanner s = new java.util.Scanner(is)) {
			return s.useDelimiter("\\A").hasNext() ? s.next() : "";
		}
	}

	private static final long serialVersionUID = 4436503480265700847L;

	private Swagger swagger = null;
	private String actionpackage = null;
	private ThreadLocal<String> currentContext = new ThreadLocal<String>();

	@Override
	protected void run(HttpServletRequest request,
			HttpServletResponse response, JSONObject req, JSONObject resp)
			throws Exception {
		String method = request.getMethod().toLowerCase();
		String path = swagger.checkRequest(request.getPathInfo(), method, req);

		path = toCamelCase(path + " " + method);

		Class<?> clazz = Class.forName(actionpackage + "." + path);
		Constructor<?> ctor = clazz.getConstructor();
		IRestAction action = (IRestAction) ctor.newInstance();

		currentContext.set(action.getContext());

		action.run(request, response, req, resp);

		action = null;
	}

	public String toCamelCase(String path) {
		path = path.replaceAll("[^A-Za-z0-9]", " ");
		path = path.trim();
		path = path.replaceAll("\\s+", "_");

		StringBuilder sb = new StringBuilder();
		for (String oneString : path.split("_")) {
			sb.append(oneString.substring(0, 1).toUpperCase());
			sb.append(oneString.substring(1));
		}
		return sb.toString();
	}

	@Override
	protected String getContext() {
		String context = currentContext.get();
		return context;
	}

	@Override
	protected String getService() {
		return swagger.getInfoTitle();
	}

	public void setSwagger(Swagger sw) {
		this.swagger = sw;
	}

	public void setActionPackage(String ap) {
		this.actionpackage = ap;
	}
}