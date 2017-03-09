package org.avaje.metric.elastic;


import org.avaje.metric.BucketTimedMetric;
import org.avaje.metric.CounterMetric;
import org.avaje.metric.CounterStatistics;
import org.avaje.metric.GaugeDoubleMetric;
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

  private final ReportMetrics reportMetrics;

  private final String header;

  private final ElasticReporterConfig config;

  private final Map<String,String> tags;

  private final long epochNow = System.currentTimeMillis();

  /**
   * Construct with default formatting of 2 decimal places.
   */
  BulkJsonWriteVisitor(Writer writer, ReportMetrics metrics, ElasticReporterConfig config, String indexSuffix) {
    this(2, writer, metrics, config, indexSuffix);
  }

  private BulkJsonWriteVisitor(int decimalPlaces, Writer writer, ReportMetrics metrics, ElasticReporterConfig config, String indexSuffix) {
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
      metric.visit(this);
    }
  }

  private String deriveHeader(ElasticReporterConfig config, String indexSuffix) {
    return "{\"index\":{\"_type\":\""+config.getIndexType()+"\",\"_index\":\""+ config.getIndexPrefix() + indexSuffix +"\"}}";
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

    appendBulkHeader();
    buffer.append("\n{");
    appendTags();
    writeHeader(config.getTypeField(), type);
    writeHeader(config.getNameField(), metric.getName().getSimpleName());
  }

  private void writeMetricEnd() throws IOException {
    buffer.append("}\n");
  }

  @Override
  public void visit(TimedMetric metric) throws IOException {

    ValueStatistics normStats = metric.getCollectedSuccessStatistics();
    ValueStatistics errorStats = metric.getCollectedErrorStatistics();
    long count = (normStats == null) ? 0 : normStats.getCount();
    long errCount = (errorStats == null) ? 0 : errorStats.getCount();

    if (count == 0 && errCount == 0) {
      // a bucket range with no counts at all so skip the whole metric
      return;
    }

    writeMetricStart("timed", metric);
    if (metric.isBucket()) {
      writeHeader("bucket", metric.getBucketRange());
    }
    if (count > 0) {
      writeSummary("norm", normStats);
      if (errCount > 0) {
        buffer.append(",");
      }
    }
    if (errCount > 0) {
      writeSummary("error", errorStats);
    }
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
    writeSummary("norm", metric.getCollectedStatistics());
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
  public void visit(GaugeDoubleMetric metric) throws IOException {

    writeMetricStart("gauge", metric);
    writeKeyNumber("val", format(metric.getValue()));
    writeMetricEnd();
  }

  @Override
  public void visit(GaugeLongMetric metric) throws IOException {

    writeMetricStart("gaugeLong", metric);
    writeKeyNumber("val", metric.getValue());
    writeMetricEnd();
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
    return Math.round((epochNow - startTime) / 1000L);
  }

}
