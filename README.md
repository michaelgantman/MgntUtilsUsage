# MgntUtilsUsage
This repository contains several demos that demonstrate various features of 
<a href="https://github.com/michaelgantman/Mgnt">Open Source MgntUtils library</a>. At the moment this repo is work in progress. 
In particular, it is not well documented yet. However, if you are willing to dive into the code you will find some nice demos for several features.

I am working on the documentation though. So far, two features are better documented in this repo: <b>Stacktrace Filtering</b> and <b>Extensible Multi-Stage Workflows for Multiple Data Types</b>. The stacktrace-filtering integration is presented first because it is the easier, more immediately useful feature. The extensible-workflows infrastructure that follows is, however, the architecturally more significant of the two — built on top of MgntUtils's <b>Self-populating Factory</b> micro-framework — and is the one I would most want a serious architect to look at.

<h2>Stacktrace Filtering</h2>
This demo shows an <b>infrastructure-level</b> integration of the MgntUtils stacktrace filtering feature into a Spring Boot application. The integration:
<ul>
  <li>Requires <b>zero changes to application code</b> — developers keep using their normal <code>logger.error("...", e)</code> calls.</li>
  <li>Filters framework noise (Spring AOP, Hibernate, CGLIB proxies, reflection, Tomcat, Thread.run, etc.) out of every stacktrace logged by the application, leaving only application-level frames plus a single transition frame for context.</li>
  <li>Works in <b>both</b> pattern-based logging (e.g. the default console output) <b>and</b> JSON-structured logging via <code>LogstashEncoder</code>. The second wiring is critical for any production system that ships logs to a structured-logging aggregator (ELK, Datadog, Splunk, OpenSearch, etc.) — and it's easy to miss, because <code>LogstashEncoder</code> bypasses the <code>%ex</code> conversion word entirely and would otherwise emit unfiltered stacktraces in production while the local dev console looks perfectly clean.</li>
  <li>Includes a <b>hot runtime toggle</b> exposed via REST so an Admin or SRE user can turn the filtering on or off without a redeployment.</li>
</ul>

The full integration walkthrough is published as an article on
<a href="https://dev.to/mgantman/zero-code-change-stacktrace-filtering-for-spring-boot-an-infrastructure-level-integration-3fk5">dev.to</a>,
<a href="https://medium.com/@michaelgantman/zero-code-change-stacktrace-filtering-for-spring-boot-an-infrastructure-level-integration-0600cff6ff61">Medium</a>,
and
<a href="https://www.linkedin.com/pulse/zero-code-change-stacktrace-filtering-spring-boot-michael-gantman-sdnaf/">LinkedIn</a>:
<i>Zero-Code-Change Stacktrace Filtering for Spring Boot: An Infrastructure-Level Integration</i>.
For the algorithm itself (which is independent of any logging framework), see the earlier deep-dive
<a href="https://www.linkedin.com/pulse/java-stacktrace-filtering-utility-michael-gantman-t003f/">Java Stacktrace Filtering Utility</a>.

The relevant files in this repo are:
<ul>
  <li><a href="src/main/java/com/example/stamboot/config/logging/MgntStackTraceConverter.java"><code>MgntStackTraceConverter.java</code></a> — the custom Logback converter that runs every stacktrace through MgntUtils filtering. Includes a Throwable-catching guard around the filter call (because Logback's appender silently drops the entire log event if a converter throws) and a three-case fallback to standard Logback rendering so a stacktrace is never lost.</li>
  <li><a href="src/main/resources/logback.xml"><code>logback.xml</code></a> — the configuration that wires the converter into both a pattern-based console appender and a JSON appender (<code>LogstashEncoder</code> via <code>&lt;throwableConverter&gt;</code>), plus all eight throwable conversion words (<code>ex</code>, <code>exception</code>, <code>throwable</code>, <code>xEx</code>, <code>xException</code>, <code>xThrowable</code>, <code>wex</code>, <code>wEx</code>) registered defensively so existing patterns Just Work without modification.</li>
  <li><a href="src/main/java/com/example/stamboot/config/logging/LoggerConfigController.java"><code>LoggerConfigController.java</code></a> — the REST controller that exposes the hot runtime on/off toggle (<code>/config/log/stacktrace</code> and <code>/config/log/stacktrace/filter/status</code>).</li>
  <li><a href="src/main/java/com/example/stamboot/StamBootApplication.java"><code>StamBootApplication.java</code></a> — calls <code>TextUtils.setRelevantPackage("com.example.stamboot.")</code> in <code>main()</code> <i>before</i> <code>SpringApplication.run(...)</code>, so the filter is armed before any Spring lifecycle code (and therefore any startup-time crash) can run.</li>
  <li><a href="src/main/java/com/example/stamboot/controller/LogFilteringDemoController.java"><code>LogFilteringDemoController.java</code></a> — the demo endpoint at <a href="http://localhost:8080/log"><code>/log</code></a> that throws an exception with 50% probability so you can compare filtered vs unfiltered output by toggling the runtime flag and refreshing.</li>
</ul>

<h2>Extensible Multi-Stage Workflows for Multiple Data Types</h2>
A very in-depth article about how to use the <b>Self-populating factory</b> feature to build extensible Multi-Stage Workflows for multiple data types. The article covers:

<ul>
  <li>How to design workflows that support new data types without modifying existing code</li>
  <li>How to add and reorder processing stages independently</li>
  <li>How to introduce conditional workflow logic without coupling stages or implementations</li>
  <li>How self-populating factories eliminate switch statements, manual registries, and configuration overhead</li>
</ul>

The article at first explain in detail what is <b>Self-populating factory pattern</b>, including a walk-through a runnable example in the MgntUtils repo. After that it goes into detail how to build the workflow based on that pattern with a walk-through the runnable real world like example in this repo. <br> Here is the link to the article: 
<a href="https://dev.to/mgantman/infrastructure-for-extensible-multi-stage-workflows-across-multiple-data-types-79k">Infrastructure for Extensible Multi-Stage Workflows Across Multiple Data Types</a> Read it, and clone this repo and you will get good idea on how to build extensible, well designed (I hope) workflow, and run the example from this repo. 
   
