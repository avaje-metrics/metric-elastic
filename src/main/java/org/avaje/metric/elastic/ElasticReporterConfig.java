package org.avaje.metric.elastic;

import okhttp3.OkHttpClient;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration for pushing metrics to ElasticSearch.
 */
public class ElasticReporterConfig {

  private String timestampField = "ts";
  private String typeField = "type";
  private String nameField = "name";
  private String indexType = "metric";
  private String indexPrefix = "metric-";
  private String url = "http://localhost:9200";
  private String templateName = "metric-1";

  /**
   * Directory holding metrics that failed to be sent at report time.
   */
  private String directory = "queued-metrics";

  /**
   * Connect timeout - default 10 seconds.
   */
  private int connectTimeout = 10;

  /**
   * Read timeout - default 30 seconds.
   */
  private int readTimeout = 30;

  /**
   * Write timeout - default 30 seconds.
   */
  private int writeTimeout = 30;

  private Map<String, String> tags = new LinkedHashMap<>();

  private OkHttpClient client;

  /**
   * Return the base url for the ElasticSearch instance.
   */
  public String getUrl() {
    return url;
  }

  /**
   * Set the base url for the ElasticSearch instance.
   */
  public ElasticReporterConfig setUrl(String url) {
    this.url = normalise(url);
    return this;
  }

  /**
   * Trim trailing slash if supplied.
   */
  String normalise(String url) {
    if (url.endsWith("/")) {
      url = url.substring(0, url.length() - 1);
    }
    return url;
  }

  /**
   * Return the index type (defaults to "metric").
   */
  public String getIndexType() {
    return indexType;
  }

  /**
   * Set the index type.
   */
  public ElasticReporterConfig setIndexType(String indexType) {
    this.indexType = indexType;
    return this;
  }

  /**
   * Return the index name prefix (defaults to "metric-").
   */
  public String getIndexPrefix() {
    return indexPrefix;
  }

  /**
   * Set the index name prefix (defaults to "metric-").
   */
  public ElasticReporterConfig setIndexPrefix(String indexPrefix) {
    this.indexPrefix = indexPrefix;
    return this;
  }

  /**
   * Add a name value pair to include in each metric entry.
   */
  public ElasticReporterConfig addTag(String key, String value) {
    this.tags.put(key, value);
    return this;
  }

  /**
   * Return all the tags.
   */
  public Map<String, String> getTags() {
    return tags;
  }

  public ElasticReporterConfig setTags(Map<String, String> tags) {
    this.tags = tags;
    return this;
  }

  /**
   * Return the name of the timestamp field (defaults to "ts").
   */
  public String getTimestampField() {
    return timestampField;
  }

  /**
   * Set the name of the timestamp field.
   */
  public ElasticReporterConfig setTimestampField(String timestampField) {
    this.timestampField = timestampField;
    return this;
  }

  /**
   * Return the name of the metric type field (defaults to "type").
   */
  public String getTypeField() {
    return typeField;
  }

  /**
   * Set the name of the metric type field (defaults to "type").
   */
  public ElasticReporterConfig setTypeField(String typeField) {
    this.typeField = typeField;
    return this;
  }

  /**
   * Return the name of the field that holds the metric name (defaults to "name").
   */
  public String getNameField() {
    return nameField;
  }

  /**
   * Set the name of the field that holds the metric name.
   */
  public ElasticReporterConfig setNameField(String nameField) {
    this.nameField = nameField;
    return this;
  }

  /**
   * Return the name of the elastic template (defaults to "metric-1").
   */
  public String getTemplateName() {
    return templateName;
  }

  /**
   * Set the name of the Elastic template.
   * <p>
   * Set this to null means the template will not be checked and set on startup.
   * </p>
   * <p>
   * When changing this value you may want to add a template as a resource under
   * the elastic-template path such that it is checked and set if it does not exist
   * in ElasticSearch.
   * </p>
   */
  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  /**
   * Return the connect timeout in seconds.
   */
  public int getConnectTimeout() {
    return connectTimeout;
  }

  /**
   * Set the connect timeout in seconds.
   */
  public ElasticReporterConfig setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  /**
   * Return the read timeout in seconds.
   */
  public int getReadTimeout() {
    return readTimeout;
  }

  /**
   * Set the read timeout in seconds.
   */
  public ElasticReporterConfig setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
    return this;
  }

  /**
   * Return the write timeout in seconds.
   */
  public int getWriteTimeout() {
    return writeTimeout;
  }

  /**
   * Set the write timeout in seconds.
   */
  public ElasticReporterConfig setWriteTimeout(int writeTimeout) {
    this.writeTimeout = writeTimeout;
    return this;
  }

  /**
   * Return the directory to put metrics into when they fail to be sent at report time.
   */
  public String getDirectory() {
    return directory;
  }

  /**
   * Set the directory where metrics are put if they fail to be sent at report time.
   */
  public ElasticReporterConfig setDirectory(String directory) {
    this.directory = directory;
    return this;
  }

  /**
   * Return the client to use (If null one will be created).
   */
  public OkHttpClient getClient() {
    return client;
  }

  /**
   * Set the client to use (If not set one will be created).
   */
  public ElasticReporterConfig setClient(OkHttpClient client) {
    this.client = client;
    return this;
  }
}
