[![Build Status](https://travis-ci.org/openknowledge/reactive-jaxrs.svg?branch=master)](https://travis-ci.org/openknowledge/reactive-jaxrs)

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
