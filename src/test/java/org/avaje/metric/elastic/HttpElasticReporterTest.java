package org.avaje.metric.elastic;

import org.avaje.metric.Metric;
import org.avaje.metric.MetricManager;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.core.DefaultTimedMetric;
import org.avaje.metric.report.HeaderInfo;
import org.avaje.metric.report.ReportMetrics;
import org.junit.Ignore;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpElasticReporterTest {

  private static long NANOS_TO_MICROS = 1000L;

  @Ignore
  @Test
  public void testIntegrationWithLocalRepository() throws MalformedURLException {

    ElasticReporterConfig config = new ElasticReporterConfig()
        .setUrl("http://127.0.0.1:9200/_bulk")
        .addTag("host", "rob")
        .addTag("app", "test-app");

    ElasticHttpReporter reporter = new ElasticHttpReporter(config);

    reporter.report(metrics());
  }


  private ReportMetrics metrics() {

    HeaderInfo headerInfo = new HeaderInfo();

    List<Metric> metrics = new ArrayList<>();
    metrics.add(createTimedMetric());

    long collectTime = System.currentTimeMillis();

    return new ReportMetrics(headerInfo, collectTime, metrics);
  }

  private TimedMetric createTimedMetric() {

    TimedMetric metric = new DefaultTimedMetric(MetricManager.name("org.test.TimedFoo.doStuff"));

    // add duration times in nanos
    metric.addEventDuration(true, 100 * NANOS_TO_MICROS); // 100 micros
    metric.addEventDuration(true, 120 * NANOS_TO_MICROS); // 120 micros
    metric.addEventDuration(true, 140 * NANOS_TO_MICROS);
    metric.addEventDuration(false, 200 * NANOS_TO_MICROS);
    metric.addEventDuration(false, 220 * NANOS_TO_MICROS);

    metric.collectStatistics();
    return metric;
  }

}