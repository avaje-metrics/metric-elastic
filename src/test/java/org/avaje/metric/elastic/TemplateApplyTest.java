package org.avaje.metric.elastic;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateApplyTest {

  private OkHttpClient client = new OkHttpClient();

  private String templateName = "metric-1";

  @Test(enabled = false)
  public void full_integration_test() {

    TemplateApply apply = new TemplateApply(client, "http://localhost:9200", templateName);
    apply.run();
  }

  @Test(enabled = false)
  public void when_nullTemplate_expect_notRun() {

    ElasticReporterConfig config = new ElasticReporterConfig();
    config.setTemplateName(null);

    TemplateApply apply = new TemplateApply(client, "http://localhost:9200", templateName);
    assertThat(apply.run()).isFalse();
  }

  @Test
  public void when_http404_http200_expect_added() throws IOException {

    MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setResponseCode(404));
    server.enqueue(new MockResponse().setResponseCode(200));
    server.start();
    HttpUrl baseUrl = server.url("");


    TemplateApply apply = new TemplateApply(client, baseUrl.toString(), templateName);
    assertThat(apply.run()).isTrue();

    server.shutdown();
  }

  @Test
  public void when_http500_expect_notAdded() throws IOException {

    MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setResponseCode(500));
    server.start();
    HttpUrl baseUrl = server.url("");

    TemplateApply apply = new TemplateApply(client, baseUrl.toString(), templateName);
    assertThat(apply.run()).isFalse();

    server.shutdown();
  }

  @Test
  public void when_http200_expect_alreadyExistsSoNotAdded() throws IOException {

    MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setResponseCode(200));
    server.start();
    HttpUrl baseUrl = server.url("");

    TemplateApply apply = new TemplateApply(client, baseUrl.toString(), templateName);
    assertThat(apply.run()).isFalse();

    server.shutdown();
  }

  @Test
  public void when_http404_http500_expect_failed() throws IOException {

    MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse().setResponseCode(404));
    server.enqueue(new MockResponse().setResponseCode(500));
    server.start();
    HttpUrl baseUrl = server.url("");

    TemplateApply apply = new TemplateApply(client, baseUrl.toString(), templateName);
    assertThat(apply.run()).isFalse();

    server.shutdown();
  }

}