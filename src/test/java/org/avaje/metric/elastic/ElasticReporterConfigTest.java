package org.avaje.metric.elastic;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ElasticReporterConfigTest {

  @Test
  public void setUrl() throws Exception {

    ElasticReporterConfig config = new ElasticReporterConfig();
    config.setUrl("http://foo:9400");

    assertThat(config.getUrl()).isEqualTo("http://foo:9400");
  }

  @Test
  public void setUrl_trailingSlash() throws Exception {

    ElasticReporterConfig config = new ElasticReporterConfig();
    config.setUrl("http://foo/");

    assertThat(config.getUrl()).isEqualTo("http://foo");
  }

}