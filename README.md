# Price Tracker Optimization Benchmark

A serverless price tracking system built with **Kotlin**, **Gradle Multi-Project Builds**, **AWS Lambda**, **DynamoDB**, and **Terraform**.

The primary purpose of this repository is to measure the impact of different AWS Lambda startup optimizations in a realistic workload. The price tracker acts as the benchmark application for these measurements. Each optimization is implemented in a separate Git branch for direct comparison.

## Benchmark Results

The benchmark focuses on:

* Cold start invocation duration
* Second invocation duration (warm start)

All lambdas are using 128MB memory size and `java25` runtime, except for GraalVM native image which uses OS-only runtime.

| Optimization                                       | Cold Start (ms) | Warm Start (ms) |
| -------------------------------------------------- | --------------: | --------------: |
| http4k serverless runtime (GraalVM native image)   |             776 |              72 |
| AWS CRT-based HTTP client                          |           7,110 |             623 |
| http4k connect                                     |          13,148 |             631 |
| Explicit environment credential & region providers |          16,077 |             452 |
| http4k connect + URLConnectionHttpClient           |          16,234 |             586 |
| Baseline (AWS SDK, Log4j2, dependencies in `/lib`) |          17,256 |             453 |
| SLF4J Simple instead of Log4j2                     |          17,571 |             520 |
| Shadow JAR (Uber JAR)                              |          22,148 |             403 |
| Minimized Shadow JAR                               |          22,407 |             460 |
| http4k serverless Lambda library                   |          31,567 |             610 |

## Findings

### GraalVM Native Image

GraalVM native image reduces cold start time from ~17,000 ms to under 1,000 ms. Warm start is reduced from ~450 ms to ~70 ms.

This is the largest reduction observed in the benchmark.

### AWS CRT HTTP Client

Using the AWS CRT-based HTTP client reduces cold start time compared to the baseline AWS SDK configuration.

| Configuration             | Cold Start (ms) |
| ------------------------- | --------------: |
| Baseline AWS SDK          |          17,256 |
| AWS CRT-based HTTP client |           7,110 |

This is the largest improvement among JVM-based configurations.

### Shadow JAR Packaging

Packaging all dependencies into a Shadow JAR results in higher cold start times compared to keeping dependencies in `/lib`.

| Packaging Strategy     | Cold Start (ms) |
| ---------------------- | --------------: |
| Dependencies in `/lib` |          17,256 |
| Shadow JAR             |          22,148 |
| Minimized Shadow JAR   |          22,407 |

Keeping dependencies as separate JARs in `/lib` performs better in this benchmark.

### Logging Framework

Replacing Log4j2 with SLF4J Simple does not materially affect cold start time.

### AWS SDK Configuration

Explicit credential and region provider configuration reduces cold start time compared to default resolution.

### http4k Connect vs AWS SDK

http4k connect reduces cold start time compared to the baseline AWS SDK configuration.

| Configuration    | Cold Start (ms) |
| ---------------- | --------------: |
| AWS SDK baseline |          17,256 |
| http4k connect   |          13,148 |

With AWS CRT enabled, the AWS SDK performs better than http4k connect, which does not yet support an AWS CRT HTTP client adapter.

| Configuration     | Cold Start (ms) |
| ----------------- | --------------: |
| http4k connect    |          13,148 |
| AWS SDK + AWS CRT |           7,110 |

### http4k Serverless Library

The http4k serverless Lambda library shows the highest cold start time among the JVM-based configurations.

## System Overview

The system collects product prices from a browser extension and maintains hourly and daily aggregates. Both layers use incremental aggregation (mean, min, max), where each Lambda fetches the current record, updates aggregates, and writes it back.

### AWS Resources

| Resource             | Count |
| -------------------- | ----- |
| AWS Lambda Functions | 4     |
| AWS DynamoDB Tables  | 2     |
| AWS DynamoDB Streams | 1     |

## API Endpoints

### GET /hourly/{skuId}

Returns hourly aggregated price data.

### POST /hourly

Accepts batched price observations from the browser extension.

### GET /daily/{skuId}

Returns daily aggregated price data.
