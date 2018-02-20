package com.redhat.xpaas.openshift;

import com.redhat.xpaas.RadConfiguration;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.openshift.api.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.redhat.xpaas.wait.WaitUtil;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;

public class OpenshiftUtil implements AutoCloseable {
  private static final Logger LOGGER = LoggerFactory
    .getLogger(OpenshiftUtil.class);
  private static final String ANNOTATION_BUILD_POD = "openshift.io/build.pod-name";

  private static OpenshiftUtil INSTANCE;
  private static final String NAMESPACE = RadConfiguration.masterNamespace();
  private final String server;
  private NamespacedOpenShiftClient defaultClient;
  private NamespacedOpenShiftClient adminClient;
  private OpenShiftContext context;
  private static final String AUTH_TOKEN = RadConfiguration.AuthToken();
  private static final Boolean USE_TOKEN = RadConfiguration.useToken();

  private OpenshiftUtil(String server, OpenShiftContext context)
    throws MalformedURLException {
    // validate the server URL
    new URL(server);

    this.server = server;
    this.context = context;
  }

  public static OpenshiftUtil getInstance() {
    if (INSTANCE == null) {
      try {
        INSTANCE = new OpenshiftUtil(RadConfiguration.masterUrl(),
          OpenShiftContext.getContext());
      } catch (MalformedURLException ex) {
        throw new IllegalArgumentException(
          "OpenShift Master URL is malformed", ex);
      }
    }

    return INSTANCE;
  }

  public OpenShiftContext getContext() {
    return context;
  }

  public <R> R withAdminUser(Function<NamespacedOpenShiftClient, R> f) {
    if (RadConfiguration.openshiftOnline()) {
      throw new IllegalArgumentException("Openshift online does not support admin users.");
    }
    if (adminClient == null) {
      OpenShiftConfig config;
      if (USE_TOKEN){
        config = new OpenShiftConfigBuilder()
          .withMasterUrl(RadConfiguration.masterUrl())
          .withTrustCerts(true).withOauthToken(AUTH_TOKEN).build();
      } else {
        config = new OpenShiftConfigBuilder()
          .withMasterUrl(RadConfiguration.masterUrl())
          .withTrustCerts(true)
          .withUsername(RadConfiguration.masterUsername())
          .withPassword(RadConfiguration.masterPassword()).build();
      }
      adminClient = new DefaultOpenShiftClient(config);
    }
    return f.apply(adminClient);
  }

  // general purpose methods
  public KubernetesList createResources(KubernetesList list) {
    return withAdminUser(client -> client.lists().create(list));
  }

  public String getHostIP(){
    InetAddress address =
      withAdminUser(client -> {
        try {
          return InetAddress.getByName(client.getOpenshiftUrl().getHost());
        } catch (UnknownHostException e) {
          e.printStackTrace();
        }
        return null;
      });
    return address.getHostAddress();
  }

  public String appDefaultHostNameBuilder(String routeName){
    String namespace = RadConfiguration.masterNamespace();
    String hostIP = RadConfiguration.HostIP();
    String suffix = RadConfiguration.RouteSuffix();
    return String.format("%s-%s.%s%s", routeName, namespace, hostIP, suffix);
  }

  // Templates
  public void loadTemplate(Template template, Map<String, String> parameters){
    withAdminUser(client -> {
        KubernetesList l = client.templates()
          .inNamespace(NAMESPACE).withName(template.getMetadata().getName()).process(parameters);
        return client.inNamespace(NAMESPACE).lists().create(l);
      }
    );

  }

  // project
  public Project getProject(String name) {
    return withAdminUser(client -> {
      Optional<Project> opt = client.projects().list().getItems()
        .stream()
        .filter(proj -> proj.getMetadata().getName().equals(name))
        .findFirst();
      if (opt.isPresent()) {
        return opt.get();
      } else {
        return null;
      }
    });
  }

  public void deleteProject(String name) {
    deleteProject(getProject(name));
  }

  public void deleteProject(Project project) {
    withAdminUser(client -> client.projects().delete(project));
    String name = project.getMetadata().getName();

    if(RadConfiguration.waitForProjectDeletion()){
      try {
        WaitUtil.waitFor(() -> getProject(name) == null);
      } catch (TimeoutException | InterruptedException e) {
        throw new IllegalStateException("Unable to delete project " + name);
      }
    }
  }

  public Project createProject(String name, boolean recreateIfExists) {
    Project existing = getProject(name);
    if (existing != null) {
      if (recreateIfExists) {
        deleteProject(existing);
      } else {
        return existing;
      }
    }

    int attempts = 0;
    while(getProject(name) == null && attempts <= RadConfiguration.maxLoginTries()) {
      try {
        withAdminUser(client -> client.projectrequests().createNew()
          .withNewMetadata()
          .withName(name)
          .endMetadata()
          .done()
        );
      } catch (Exception e) {
        attempts++;
        LOGGER.info(String.format("Unable to create project, trying again, attempted %s time(s).", Integer.toString(attempts)));
        try {
          if(attempts > RadConfiguration.maxLoginTries()){
            break;
          }
          Thread.sleep(10000L);
        } catch (InterruptedException e1) {
          LOGGER.warn("Interrupted while attempting to create namespace.");
        }
      }
    }

    if(getProject(name) == null){
      LOGGER.error("Unable to create project after max attempts");
      throw new RuntimeException("Unable to create project after max attempts");
    }

    addRoleToUser(name, "admin", RadConfiguration.masterUsername());

    return getProject(name);
  }

  public void addRoleToUser(String namespace, String role, String name) {
    RoleBinding roleBinding = getOrCreateRoleBinding(namespace, role);

    addSubjectToRoleBinding(roleBinding, "User", name);
    addUserNameToRoleBinding(roleBinding, name);

    updateRoleBinding(roleBinding);
  }

  // pods
  public Collection<Pod> getPods() {
    return getPods(NAMESPACE);
  }

  public Collection<Pod> getPods(String namespace) {
    LOGGER.debug("Getting pods for namespace {}", namespace);
    return withAdminUser(client -> client.inNamespace(namespace).pods()
      .list().getItems());
  }

  public Optional<Pod> getPod(String labelKey, String labelValue){
    return withAdminUser(client -> client.inNamespace(NAMESPACE).pods()
      .list().getItems().stream().filter(p -> {
        Map<String, String> labels = p.getMetadata().getLabels();
        return labels.containsKey(labelKey) && labels.get(labelKey).equals(labelValue);
      }).findFirst()
    );
  }

  public List<Pod> findPods(Map<String, String> labels) {
    return findPods(labels, NAMESPACE);
  }

  private List<Pod> findPods(Map<String, String> labels, String namespace) {
    return withAdminUser(client -> client.inNamespace(namespace).pods()
      .withLabels(labels).list().getItems());
  }

  public List<Pod> findNamedPods(String name) {
    return findPods(Collections.singletonMap("name", name));
  }

  public boolean podRunning(String labelKey, String labelValue){
    return podInPhase(labelKey, labelValue, "Running");
  }

  public boolean podIsHealthy(String labelKey, String labelValue){
    Optional<Pod> pod = getPod(labelKey, labelValue);
    List<PodCondition> conditions;

    if(pod.isPresent()){
      conditions = pod.get().getStatus().getConditions();
    } else {
      throw new IllegalStateException(String.format("Pod with label [%s:%s] does not exist", labelKey, labelValue));
    }
    return conditions.stream().filter(c -> !c.getStatus().equals("True")).count() == 0;
  }

  public boolean podInPhase(String labelKey, String labelValue, String phase){
    Optional<Pod> pod = getPod(labelKey, labelValue);
    return pod.map(p -> p.getStatus().getPhase().equals(phase)).orElse(false);
  }

  // builds
  public Collection<Build> getBuilds() {
    return getBuilds(NAMESPACE);
  }

  public Collection<Build> getBuilds(String namespace) {
    return withAdminUser(client -> client.inNamespace(namespace).builds().list().getItems());
  }

  public boolean buildCompleted(String labelKey, String labelValue){
    Optional<Build> build = withAdminUser(client -> client.inNamespace(NAMESPACE).builds()
      .list().getItems().stream().filter(b -> {
        Map<String, String> labels = b.getMetadata().getLabels();
        return labels.containsKey(labelKey) && labels.get(labelKey).equals(labelValue);
      }).findFirst()
    );
    return build.map(b -> b.getStatus().getPhase().equals("Complete")).orElse(false);
  }

  // routes
  public Route getRoute(String routeName){
    Optional<Route> route = withAdminUser(client ->
      client.inNamespace(NAMESPACE).routes().list().getItems().stream().filter(r ->
        r.getMetadata().getName().equals(routeName))
        .findFirst()
    );
    return route.orElse(null);
  }

  public Boolean getRouteStatus(String route){
    return getRouteStatus(getRoute(route));
  }
  public Boolean getRouteStatus(Route route){
    return Boolean.valueOf(route.getStatus().getIngress().get(0).getConditions().get(0).getStatus());
  }

  // roleBindings
  public void addRoleToServiceAccount(String role, String serviceAccountName) {
    addRoleToServiceAccount(getContext().getNamespace(), role, serviceAccountName);
  }

  public void addRoleToServiceAccount(String namespace, String role, String serviceAccountName) {
    RoleBinding roleBinding = getOrCreateRoleBinding(namespace, role);

    addSubjectToRoleBinding(roleBinding, "ServiceAccount", serviceAccountName);
    addUserNameToRoleBinding(roleBinding, String.format("system:serviceaccount:%s:%s", namespace, serviceAccountName));

    updateRoleBinding(roleBinding);
  }

  private RoleBinding getOrCreateRoleBinding(String namespace, String role) {
    RoleBinding roleBinding = withAdminUser(client -> client.inNamespace(namespace).roleBindings().withName(role).get());

    if(roleBinding == null) {
      return withAdminUser(client -> client.inNamespace(namespace).roleBindings().createNew()
        .withNewMetadata().withName(role).endMetadata()
        .withNewRoleRef().withName(role).endRoleRef()
        .done());
    }
    return roleBinding;
  }

  public RoleBinding updateRoleBinding(RoleBinding roleBinding) {
    return withAdminUser(client -> client.inNamespace(roleBinding.getMetadata().getNamespace())
      .roleBindings()
      .createOrReplace(roleBinding));
  }

  private void addSubjectToRoleBinding(RoleBinding roleBinding, String entityKind, String entityName) {
    ObjectReference subject = new ObjectReferenceBuilder().withKind(entityKind).withName(entityName).build();

    if(!roleBinding.getSubjects().stream().anyMatch(x -> x.getName().equals(subject.getName()) && x.getKind().equals(subject.getKind()))) {
      roleBinding.getSubjects().add(subject);
    }
  }

  private void addUserNameToRoleBinding(RoleBinding roleBinding, String userName) {
    if( roleBinding.getUserNames() == null) {
      roleBinding.setUserNames(new ArrayList<>());
    }
    if( !roleBinding.getUserNames().contains(userName)) {
      roleBinding.getUserNames().add(userName);
    }
  }

  public PersistentVolumeClaim getPersistentVolumeClaim(String name) {
    return withAdminUser(client -> client.persistentVolumeClaims().withName(name).get());
  }

  @Override
  public void close() throws Exception {
    if (defaultClient != null) {
      defaultClient.close();
    }
    if (adminClient != null) {
      adminClient.close();
    }
  }

}
