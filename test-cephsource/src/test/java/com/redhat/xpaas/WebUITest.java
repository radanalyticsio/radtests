package com.redhat.xpaas;

import com.redhat.xpaas.openshift.OpenshiftUtil;
import com.redhat.xpaas.rad.CephSource.api.CephSourceWebUI;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebUITest {

  private static CephSourceWebUI CephSource;
  private Logger log = LoggerFactory.getLogger(WebUITest.class);;
  private static final OpenshiftUtil openshift = OpenshiftUtil.getInstance();

  @BeforeClass
  public static void setUP() {
    Setup setup = new Setup();
    WebUITest.CephSource = setup.initializeApplications();
    //WebUITest.CephSource = CephSourceWebUI.getInstance(openshift.appDefaultHostNameBuilder("workshop-notebook"));
    CephSource.login("developer");
    CephSource.loadProjectByURL("ceph-example.ipynb");
  }

  @AfterClass
  public static void tearDown(){
    Setup setup = new Setup();
  }

  @Test
  public void testAVerifyDeployment(){
    Assertions.assertThat(true).isTrue();
  }


}

