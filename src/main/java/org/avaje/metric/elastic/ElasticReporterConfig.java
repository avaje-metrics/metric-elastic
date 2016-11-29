package org.avaje.metric.elastic;

import java.util.LinkedHashMap;
import java.util.Map;

public class ElasticReporterConfig {

  private String timestampField = "ts";
  private String typeField = "type";
  private String nameField = "name";

  private String indexType = "metric";
  private String indexPrefix = "metric-";

  private String url = "http://localhost:9200/_bulk";

  private Map<String,String> tags = new LinkedHashMap<>();

  public String getUrl() {
    return url;
  }

  public ElasticReporterConfig setUrl(String url) {
    this.url = url;
    return this;
  }

  public String getIndexType() {
    return indexType;
  }

  public ElasticReporterConfig setIndexType(String indexType) {
    this.indexType = indexType;
    return this;
  }

  public String getIndexPrefix() {
    return indexPrefix;
  }

  public ElasticReporterConfig setIndexPrefix(String indexPrefix) {
    this.indexPrefix = indexPrefix;
    return this;
  }

  public ElasticReporterConfig addTag(String key, String value) {
    this.tags.put(key, value);
    return this;
  }

  public Map<String, String> getTags() {
    return tags;
  }

  public ElasticReporterConfig setTags(Map<String, String> tags) {
    this.tags = tags;
    return this;
  }

  public String getTimestampField() {
    return timestampField;
  }

  public ElasticReporterConfig setTimestampField(String timestampField) {
    this.timestampField = timestampField;
    return this;
  }

  public String getTypeField() {
    return typeField;
  }

  public ElasticReporterConfig setTypeField(String typeField) {
    this.typeField = typeField;
    return this;
  }

  public String getNameField() {
    return nameField;
  }

  public ElasticReporterConfig setNameField(String nameField) {
    this.nameField = nameField;
    return this;
  }
}
