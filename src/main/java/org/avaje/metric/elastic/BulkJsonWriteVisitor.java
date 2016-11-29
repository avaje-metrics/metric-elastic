package org.avaje.metric.elastic;


import org.avaje.metric.BucketTimedMetric;
import org.avaje.metric.CounterMetric;
import org.avaje.metric.CounterStatistics;
import org.avaje.metric.GaugeDoubleGroup;
import org.avaje.metric.GaugeDoubleMetric;
import org.avaje.metric.GaugeLongGroup;
import org.avaje.metric.GaugeLongMetric;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricVisitor;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.ValueMetric;
import org.avaje.metric.ValueStatistics;
import org.avaje.metric.report.NumFormat;
import org.avaje.metric.report.ReportMetrics;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

/**
 * Writes the metric information as JSON to a buffer for sending.
 */
class BulkJsonWriteVisitor implements MetricVisitor {

  private final int decimalPlaces;

  private final Writer buffer;

  //private final long collectionTime;

  private final ReportMetrics reportMetrics;

  private final String header;

  private final ElasticReporterConfig config;

  private final Map<String,String> tags;

  /**
   * Construct with default formatting of 2 decimal places.
   */
  BulkJsonWriteVisitor(Writer writer, ReportMetrics metrics, ElasticReporterConfig config, String indexSuffix) {
    this(2, writer, metrics, config, indexSuffix);
  }

  BulkJsonWriteVisitor(int decimalPlaces, Writer writer, ReportMetrics metrics, ElasticReporterConfig config, String indexSuffix) {
    this.decimalPlaces = decimalPlaces;
    this.buffer = writer;
    this.reportMetrics = metrics;
    //this.collectionTime = reportMetrics.getCollectionTime();
    this.config = config;
    this.header = deriveHeader(config, indexSuffix);
    this.tags = config.getTags();
  }

  void write() throws IOException {
    for (Metric metric : reportMetrics.getMetrics()) {
      appendBulkHeader();
      metric.visit(this);
      buffer.append("\n");
    }
  }

  private String deriveHeader(ElasticReporterConfig config, String indexSuffix) {
    return "{\"index\":{\"_type\":\""+config.getIndexType()+"\",\"_index\":\""+ config.getIndexPrefix() + indexSuffix +"\"}}\n";
  }

  private void appendBulkHeader() throws IOException {
    buffer.append(header);
  }

  private void appendTags() throws IOException {
    writeHeader(config.getTimestampField(), reportMetrics.getCollectionTime());
    if (tags != null) {
      Set<Map.Entry<String, String>> entries = tags.entrySet();
      for (Map.Entry<String, String> entry : entries) {
        writeHeader(entry.getKey(), entry.getValue());
      }
    }
  }

  private void writeMetricStart(String type, Metric metric) throws IOException {

    buffer.append("{");
    appendTags();
    writeHeader(config.getTypeField(), type);
    writeHeader(config.getNameField(), metric.getName().getSimpleName());
  }

  private void writeMetricEnd() throws IOException {
    buffer.append("}");
  }

  @Override
  public void visit(TimedMetric metric) throws IOException {

    writeMetricStart("timed", metric);
    writeSummary("n", metric.getCollectedSuccessStatistics());
    buffer.append(",");
    writeSummary("e", metric.getCollectedErrorStatistics());
    writeMetricEnd();
  }

  @Override
  public void visit(BucketTimedMetric metric) throws IOException {
    for (TimedMetric bucket : metric.getBuckets()) {
      visit(bucket);
    }
  }

  @Override
  public void visit(ValueMetric metric) throws IOException {

    writeMetricStart("value", metric);
    writeSummary("n", metric.getCollectedStatistics());
    writeMetricEnd();
  }

  @Override
  public void visit(CounterMetric metric) throws IOException {

    writeMetricStart("counter", metric);
    CounterStatistics counterStatistics = metric.getCollectedStatistics();
    writeKeyNumber("count", counterStatistics.getCount());
    buffer.append(",");
    writeKeyNumber("dur", getDuration(counterStatistics.getStartTime()));
    writeMetricEnd();
  }

  @Override
  public void visit(GaugeDoubleGroup gaugeMetricGroup) throws IOException {
    for (GaugeDoubleMetric gaugeMetric : gaugeMetricGroup.getGaugeMetrics()) {
      visit(gaugeMetric);
    }
  }

  @Override
  public void visit(GaugeDoubleMetric metric) throws IOException {

    writeMetricStart("gauge", metric);
    writeKeyNumber("value", format(metric.getValue()));
    writeMetricEnd();
  }

  @Override
  public void visit(GaugeLongMetric metric) throws IOException {

    writeMetricStart("gaugeCounter", metric);
    writeKeyNumber("value", metric.getValue());
    writeMetricEnd();
  }

  @Override
  public void visit(GaugeLongGroup gaugeMetricGroup) throws IOException {
    for (GaugeLongMetric gaugeMetric : gaugeMetricGroup.getGaugeMetrics()) {
      visit(gaugeMetric);
    }
  }

  private void writeSummary(String prefix, ValueStatistics valueStats) throws IOException {

    // valueStats == null when BucketTimedMetric and the bucket is empty
    long count = (valueStats == null) ? 0 : valueStats.getCount();
    
    writeKey(prefix);
    buffer.append("{");
    writeKeyNumber("count", count);
    if (count != 0) {
      buffer.append(",");
      writeKeyNumber("avg", valueStats.getMean());
      buffer.append(",");
      writeKeyNumber("max", valueStats.getMax());
      buffer.append(",");
      writeKeyNumber("sum", valueStats.getTotal());
      buffer.append(",");
      writeKeyNumber("dur", getDuration(valueStats.getStartTime()));
    }

    buffer.append("}");
  }

  private String format(double value) {
    return NumFormat.dp(decimalPlaces, value);
  }

  private void writeKeyNumber(String key, long numberValue) throws IOException {
    writeKeyNumber(key, String.valueOf(numberValue));
  }

  private void writeKeyNumber(String key, String numberValue) throws IOException {
    writeKey(key);
    writeNumberValue(numberValue);
  }

  private void writeHeader(String key, String value) throws IOException {
    writeKey(key);
    writeValue(value);
    buffer.append(",");
  }

  private void writeHeader(String key, long value) throws IOException {
    writeKey(key);
    buffer.append(String.valueOf(value));
    buffer.append(",");
  }

  private void writeKey(String key) throws IOException {
    buffer.append("\"");
    buffer.append(key);
    buffer.append("\":");
  }

  private void writeValue(String val) throws IOException {
    buffer.append("\"");
    buffer.append(val);
    buffer.append("\"");
  }

  private void writeNumberValue(String val) throws IOException {
    buffer.append(val);
  }

  private long getDuration(long startTime) {
    return Math.round((System.currentTimeMillis() - startTime) / 1000d);
  }

}
