package org.avaje.metric.elastic;

import org.avaje.metric.CounterMetric;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricManager;
import org.avaje.metric.report.HeaderInfo;
import org.avaje.metric.report.ReportMetrics;
import org.testng.annotations.Test;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BulkJsonWriteVisitorTest {

  @Test
  public void testWrite() throws Exception {


    List<Metric> metrics = new ArrayList<>();

    CounterMetric one = MetricManager.getCounterMetric("org.one.Foo.count");
    one.markEvent();

    one.collectStatistics(metrics);

    ReportMetrics reportMetrics = new ReportMetrics(new HeaderInfo(), System.currentTimeMillis(), metrics);

    ElasticReporterConfig config = new ElasticReporterConfig();
    String indexSuffix = "test.metric";


    Writer writer = new StringWriter(1000);
    BulkJsonWriteVisitor bulk = new BulkJsonWriteVisitor(writer, reportMetrics, config, indexSuffix);
    bulk.write();

    assertThat(writer.toString()).contains("{\"index\":{\"_type\":\"metric\",\"_index\":\"metric-test.metric\"}}");
  }

}
