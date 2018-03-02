package com.redhat.xpaas.rad.postgresql;

import com.redhat.xpaas.RadConfiguration;
import com.redhat.xpaas.logger.LoggerUtil;
import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.winemap.api.WinemapWebUI;
import com.redhat.xpaas.wait.WaitUtil;
import io.fabric8.openshift.api.model.Template;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class PostgreSQL {
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();
  private static final String NAMESPACE = RadConfiguration.masterNamespace();

  public static void deployPostgreSQL() throws TimeoutException, InterruptedException {
    String postgresqlTemplate = "/postgresql.yaml";

    Template template = openshift.withAdminUser(client ->
      client.templates().inNamespace(NAMESPACE).load(PostgreSQL.class.getResourceAsStream(postgresqlTemplate)).createOrReplace()
    );

    Map<String, String> parameters = new HashMap<>();
    parameters.put("NAMESPACE", NAMESPACE);
    parameters.put("POSTGRESQL_USER", "username");
    parameters.put("POSTGRESQL_PASSWORD", "password");
    parameters.put("POSTGRESQL_DATABASE", "wineDb");

    openshift.loadTemplate(template, parameters);

    boolean succeeded = WaitUtil.waitForPodsToReachRunningState("name", "postgresql", 1);
    if(!succeeded){
      throw new IllegalStateException(LoggerUtil.openshiftError("postgresql deployment", "pod"));
    }
  }
}
