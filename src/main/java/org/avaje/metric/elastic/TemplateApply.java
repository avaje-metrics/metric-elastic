package org.avaje.metric.elastic;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

class TemplateApply {

  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

  private static final Logger log = LoggerFactory.getLogger(TemplateApply.class);

  private final OkHttpClient client;

  private final String baseUrl;

  private final String templateName;

  TemplateApply(OkHttpClient client, String baseUrl, String templateName) {
    this.client = client;
    this.baseUrl = normalise(baseUrl);
    this.templateName = templateName;
  }

  /**
   * Trim trailing slash if supplied.
   */
  private String normalise(String url) {
    if (url.endsWith("/")) {
      url = url.substring(0, url.length()-1);
    }
    return url;
  }

  /**
   * Return true if the template was added to ElasticSearch.
   */
  boolean run() {
    return templateMissing() && putTemplate();
  }

  /**
   * Return if the template was PUT to ElasticSearch.
   */
  private boolean putTemplate() {

    String resourceName = "/elastic-template/" + templateName + ".json";
    URL resource = getClass().getResource(resourceName);
    if (resource == null) {
      log.warn("Could not find template resource {} to apply to ElasticSearch", resourceName);
      return false;
    }

    try {
      String template = read(resource.openStream());

      RequestBody body = RequestBody.create(JSON, template);

      String url = baseUrl + "/_template/" + templateName;
      Request request = new Request.Builder()
          .url(url)
          .put(body).build();

      try (Response response = client.newCall(request).execute()) {
        if (response.code() == 200) {
          log.info("PUT template:{}", templateName);
          return true;
        } else {
          log.warn("failed to PUT template:{} response:{}", templateName, response.body().string());
          return false;
        }
      }

    } catch (IOException e) {
      log.error("Error trying to PUT template:{}", templateName, e);
      return false;
    }
  }

  private static String read(InputStream input) throws IOException {
    try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
      return buffer.lines().collect(Collectors.joining("\n"));
    }
  }

  /**
   * Return true if the template is not defined in ElasticSearch.
   */
  private boolean templateMissing() {

    if (templateName == null) {
      // means don't automatically PUT the template
      return false;
    }

    String url = baseUrl + "/_template/" + templateName;
    Request request = new Request.Builder().url(url).get().build();

    try {
      try (Response response = client.newCall(request).execute()) {
        return response.code() == 404;
      }
    } catch (UnknownHostException e) {
      log.warn("UnknownHostException checking for template: {}", e.getMessage());
      return false;

    } catch (IOException e) {
      log.error("failed to check if template {} exists in elasticsearch", templateName, e);
      return false;
    }
  }

}
