# word-counter

## Overview

### This application downloads files from a specific google-cloud bucket, reads their content and counts the occurrence of each word.

## Bucket URL
[word-counter](https://console.cloud.google.com/storage/browser/word-counter-pld/inbound/?folder&organizationId&project=word-counter-pld)

## Dependencies
* Spring Boot 2.1.0
* Apache Camel 2.23.0
* Google Cloud Storage 3.0.0
* Mockito 2.15.0
* JUnit 4.12

## Running locally

In order to run the application locally, first you have to have set GOOGLE_APPLICATION_CREDENTIALS
environment variable with the value of the generated json file from the storage, in order to authenticate yourself.

After that you have to set a few VM options:
* -Dspring.profiles.active=dev
* -Dspring.config.name=word-counter

You can also set the PREFIX environment variable, used for downloading a list of objects
matching that prefix. If not set, the default value is Dan Millman.
