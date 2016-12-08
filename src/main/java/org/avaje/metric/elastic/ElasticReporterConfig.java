package org.avaje.metric.elastic;

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

  private Map<String,String> tags = new LinkedHashMap<>();

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
      url = url.substring(0, url.length()-1);
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
}
