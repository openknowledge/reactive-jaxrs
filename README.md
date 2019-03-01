[![Build Status](https://travis-ci.com/openknowledge/reactive-jaxrs.svg?branch=master)](https://travis-ci.com/openknowledge/reactive-jaxrs) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=de.openknowledge.jaxrs%3Areactive-jaxrs-modules&metric=security_rating)](https://sonarcloud.io/dashboard?id=de.openknowledge.jaxrs%3Areactive-jaxrs-modules) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=de.openknowledge.jaxrs%3Areactive-jaxrs-modules&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=de.openknowledge.jaxrs%3Areactive-jaxrs-modules) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=de.openknowledge.jaxrs%3Areactive-jaxrs-modules&metric=bugs)](https://sonarcloud.io/dashboard?id=de.openknowledge.jaxrs%3Areactive-jaxrs-modules) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=de.openknowledge.jaxrs%3Areactive-jaxrs-modules&metric=coverage)](https://sonarcloud.io/dashboard?id=de.openknowledge.jaxrs%3Areactive-jaxrs-modules)

Why reactive JAX-RS?
=========================
tldr; consume and produce stream of object instances to safe memory

TBD

How to integrate the framework
==============================
Use Java 9 to have the Flow API available, which is required for the library.

Following types are supported:

- `Flow.Publisher` for collections
- `CompletionStage` for single item
