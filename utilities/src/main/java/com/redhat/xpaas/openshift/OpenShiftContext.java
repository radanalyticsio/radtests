package com.redhat.xpaas.openshift;

import com.redhat.xpaas.RadConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

public class OpenShiftContext {
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenShiftContext.class);
	public static final String DEFAULT_CONTEXT_NAME = "default";
	public static final String ADMIN_CONTEXT_NAME = "cluster-admin";

	private static final Map<String, OpenShiftContext> REGISTRY;

	static {
		REGISTRY = new HashMap<>();

		String username = RadConfiguration.masterUsername();
		String password = RadConfiguration.masterPassword();
		String namespace = RadConfiguration.masterNamespace();
		String token = RadConfiguration.getMasterToken();

		REGISTRY.put(DEFAULT_CONTEXT_NAME, new OpenShiftContext(username, password, namespace, token));
		if (!RadConfiguration.openshiftOnline()) {
			String adminName = RadConfiguration.masterUsername();
			String adminPass = RadConfiguration.masterPassword();

			REGISTRY.put(ADMIN_CONTEXT_NAME, new OpenShiftContext(adminName, adminPass, "default", null));
		}
	}

	public static OpenShiftContext getContext() {
		return getContext(DEFAULT_CONTEXT_NAME);
	}

	public static OpenShiftContext getContext(String contextName) {
		return REGISTRY.get(contextName);
	}

	public static OpenShiftContext newContext(String contextName, String username, String password, String namespace) {
		if (DEFAULT_CONTEXT_NAME.equals(contextName)) {
			throw new IllegalArgumentException("Can't override the default context!");
		}
		if (ADMIN_CONTEXT_NAME.equals(contextName)) {
			throw new IllegalArgumentException("Can't override the admin context!");
		}
		if (REGISTRY.containsKey(contextName)) {
			LOGGER.warn("Overriding context '{}'", contextName);
		}
		OpenShiftContext result = new OpenShiftContext(username, password, namespace, null);
		REGISTRY.put(contextName, result);

		return result;
	}

	private final String username;
	private final String password;
	private final String namespace;
	private String token;

	public OpenShiftContext(String username, String password, String namespace, String token) {
		this.username = username;
		this.password = password;
		this.namespace = namespace;
		this.token = token;
	}

	public String getUsername() {
		return username;
	}

	String getPassword() {
		return password;
	}

	public String getNamespace() {
		return namespace;
	}

	void setToken(String token) {
		this.token = token;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OpenShiftContext other = (OpenShiftContext) obj;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

}
