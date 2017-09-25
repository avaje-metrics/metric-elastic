package org.avaje.metric.elastic;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.avaje.metric.report.MetricReporter;
import org.avaje.metric.report.ReportMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Http(s) based Reporter that sends JSON formatted metrics directly to Elastic.
 */
public class ElasticHttpReporter implements MetricReporter {

  private static final Logger logger = LoggerFactory.getLogger(ElasticHttpReporter.class);

  private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

  private static final DateTimeFormatter todayFormat
    = new DateTimeFormatterBuilder()
    .appendPattern("yyyy.MM.dd")
    .toFormatter();


  private final File directory;

  private final OkHttpClient client;

  private final String bulkUrl;

  private final ElasticReporterConfig config;

  public ElasticHttpReporter(ElasticReporterConfig config) {
    this.client = getClient(config);
    this.config = config;
    this.bulkUrl = config.getUrl() + "/_bulk";
    this.directory = checkDirectory(config.getDirectory());

    // put the template to elastic if it is not already there
    new TemplateApply(client, config.getUrl(), config.getTemplateName()).run();
  }

  private File checkDirectory(String directory) {
    File dir = new File(directory);
    if (!dir.exists() && !dir.mkdirs()) {
      throw new IllegalStateException("Unable to access or create directory [" + directory + "]");
    }
    return dir;
  }

  private OkHttpClient getClient(ElasticReporterConfig config) {

    OkHttpClient client = config.getClient();
    if (client != null) {
      return client;
    } else {
      return new OkHttpClient.Builder()
        .connectTimeout(config.getConnectTimeout(), TimeUnit.SECONDS)
        .readTimeout(config.getReadTimeout(), TimeUnit.SECONDS)
        .writeTimeout(config.getWriteTimeout(), TimeUnit.SECONDS)
        .build();
    }
  }

  /**
   * Send the non-empty metrics that were collected to the remote repository.
   */
  @Override
  public void report(ReportMetrics reportMetrics) {

    if (reportMetrics.getMetrics().isEmpty()) {
      return;
    }

    StringWriter writer = new StringWriter(1000);
    BulkJsonWriteVisitor jsonVisitor = new BulkJsonWriteVisitor(writer, reportMetrics, config, today());
    try {
      jsonVisitor.write();
    } catch (IOException e) {
      logger.error("Failed to write Bulk JSON for metrics", e);
      return;
    }
    String bulkJson = writer.toString();
    if (!bulkJson.isEmpty()) {
      sendMetrics(bulkJson, true);
    }
  }

  /**
   * Send the bulk message to ElasticSearch.
   */
  private void sendMetrics(String bulkMessage, boolean withQueued) {
    String json = bulkMessage;
    if (logger.isTraceEnabled()) {
      logger.trace("Sending:\n{}", json);
    }

    RequestBody body = RequestBody.create(JSON, json);
    Request request = new Request.Builder()
      .url(bulkUrl)
      .post(body)
      .build();

    try {
      try (Response response = client.newCall(request).execute()) {
        if (!response.isSuccessful()) {
          logger.warn("Unsuccessful sending metrics payload to server - {}", response.body().string());
          storeJsonForResend(json);
        } else {
          if (logger.isTraceEnabled()) {
            logger.trace("Bulk Response - {}", response.body().string());
          }
          if (withQueued) {
            sendQueued();
          }
        }
      }

    } catch (UnknownHostException e) {
      logger.info("UnknownHostException trying to sending metrics to server: " + e.getMessage());
      storeJsonForResend(json);

    } catch (ConnectException | SocketTimeoutException e) {
      logger.info("Connection error sending metrics to server: " + e.getMessage());
      storeJsonForResend(json);

    } catch (Exception e) {
      logger.warn("Unexpected error sending metrics to server, metrics queued to be sent later", e);
      storeJsonForResend(json);
    }
  }

  /**
   * Send any metrics files that have been queued (as they failed initial send to elasticsearch).
   */
  private void sendQueued() {

    File[] files = directory.listFiles(pathname -> pathname.getName().endsWith(".metric"));
    if (files == null) {
      return;
    }
    for (File heldFile : files) {
      try {
        sendMetrics(readQueuedFile(heldFile), false);
        if (!heldFile.delete()) {
          logger.error("Sent but unable to deleted queued metrics file, possible duplicate metrics for file:{}", heldFile);
        } else {
          logger.info("Sent queued metrics file {}", heldFile.getName());
        }
      } catch (IOException e) {
        // just successfully sent metrics so not really expecting this
        logger.warn("Failed to sent queued metrics file " + heldFile.getName(), e);
        return;
      }
    }
  }

  /**
   * Read and return the content from queued metrics file.
   */
  private String readQueuedFile(File heldFile) throws IOException {
    StringBuilder sb = new StringBuilder(1000);
    List<String> lines = Files.readAllLines(heldFile.toPath());
    for (String line : lines) {
      sb.append(line).append("\n");
    }
    return sb.toString();
  }

  private String today() {
    return todayFormat.format(LocalDate.now());
  }

  protected void storeJsonForResend(String json) {
    try {
      // will be unique file name
      File out = new File(directory, "metrics-" + System.currentTimeMillis() + ".metric");
      FileWriter fw = new FileWriter(out);
      fw.write(json);
      fw.flush();
      fw.close();
    } catch (IOException e) {
      logger.warn("Failed to store metrics file for resending", e);
    }
  }

  @Override
  public void cleanup() {
    // Do nothing
  }

}
