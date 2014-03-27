#!/bin/bash

# build code
mvn clean package

# download dependencies
mvn dependency:copy-dependencies
