package com.crivano.swaggerservlet;

import junit.framework.TestCase;

import org.json.JSONObject;

public abstract class SwaggerTestSupport extends TestCase {
	private SwaggerServlet ss = null;

	protected abstract String getPackage();

	protected String getSwaggerFilePathName() {
		return "/swagger.yaml";
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		buildTestServlet(getPackage(), getSwaggerFilePathName());
	}

	@SuppressWarnings("serial")
	public void buildTestServlet(String packag, String file) {
		ss = new SwaggerServlet() {
			@Override
			protected String getContext() {
				return "test";
			}
		};
		Swagger sv = null;
		sv = new Swagger();
		sv.loadFromInputStream(this.getClass().getResourceAsStream(file));

		ss.setSwagger(sv);
		ss.setActionPackage(packag);
	}

	public void run(String method, String pathInfo, ISwaggerRequest req,
			ISwaggerResponse resp) {
		try {
			ss.prepare(method, pathInfo);
			ss.run(req, resp);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
