#!/bin/bash

java -cp target/OozieJMSListener-1.0-SNAPSHOT.jar:target/dependency/* com.github.rkanter.ooziejmslistener.OozieJMSListener $@
