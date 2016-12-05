package org.avaje.metric.elastic;

import org.avaje.metric.BucketTimedMetric;
import org.avaje.metric.Metric;
import org.avaje.metric.MetricManager;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.core.DefaultTimedMetric;
import org.avaje.metric.report.HeaderInfo;
import org.avaje.metric.report.ReportMetrics;
import org.junit.Ignore;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class HttpElasticReporterTest {

  private static long MILLIS_TO_NANOS = 1000000L;

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
    metrics.add(createBucketTimedMetric());
    //metrics.add(createBucketTimedPartial());
    //metrics.add(createBucketTimedPartialErr());

    long collectTime = System.currentTimeMillis();

    return new ReportMetrics(headerInfo, collectTime, metrics);
  }

  private BucketTimedMetric createBucketTimedMetric() {
    BucketTimedMetric timedMetric = MetricManager.getTimedMetric("org.test.BucketTimedFoo.doStuff", 100, 1000);

    timedMetric.addEventDuration(true, 80 * MILLIS_TO_NANOS);
    timedMetric.addEventDuration(true, 225 * MILLIS_TO_NANOS); // 120 micros
    timedMetric.addEventDuration(true, 205 * MILLIS_TO_NANOS);
//    timedMetric.addEventDuration(true, 1505 * MILLIS_TO_NANOS);
//    timedMetric.addEventDuration(false, 205 * MILLIS_TO_NANOS);
//    timedMetric.addEventDuration(false, 225 * MILLIS_TO_NANOS);

    timedMetric.collectStatistics();
    return timedMetric;
  }

  private BucketTimedMetric createBucketTimedPartial() {
    BucketTimedMetric timedMetric = MetricManager.getTimedMetric("org.test.BucketPartial.doOther", 100, 1000);

    timedMetric.addEventDuration(true, 125 * MILLIS_TO_NANOS); // 120 micros
    timedMetric.addEventDuration(false, 505 * MILLIS_TO_NANOS);
    timedMetric.addEventDuration(true, 1505 * MILLIS_TO_NANOS);

    timedMetric.collectStatistics();
    return timedMetric;
  }

  private BucketTimedMetric createBucketTimedPartialErr() {
    BucketTimedMetric timedMetric = MetricManager.getTimedMetric("org.test.BucketErr.justErr", 100, 1000);
    timedMetric.addEventDuration(false, 5000 * MILLIS_TO_NANOS);
    timedMetric.collectStatistics();
    return timedMetric;
  }

  private TimedMetric createTimedMetric() {

    TimedMetric metric = new DefaultTimedMetric(MetricManager.name("org.test.TimedFoo.doStuff"));

    // add duration times in nanos
    metric.addEventDuration(true, 105 * MILLIS_TO_NANOS); // 100 micros
    metric.addEventDuration(true, 125 * MILLIS_TO_NANOS); // 120 micros
    metric.addEventDuration(true, 145 * MILLIS_TO_NANOS);
    metric.addEventDuration(false, 205 * MILLIS_TO_NANOS);
    metric.addEventDuration(false, 225 * MILLIS_TO_NANOS);

    metric.collectStatistics();

    return metric;
  }

}