OozieJMSListener
================

This tool will listen for JMS notification messages for jobs and for SLAs.  
It is based on: http://archive.cloudera.com/cdh5/cdh/5/oozie/DG_JMSNotifications.html

Build
-----
````
./build.sh
````

Run
---
````
./run.sh <oozie-url> [username]
```


Sample Output
=============
````
$ ./run.sh http://rkanter-5.ent.cloudera.com:11000/oozie admin
JNDI Properties
===============
-- listing properties --
java.naming.factory.initial=org.apache.activemq.jndi.ActiveMQInit...
java.naming.provider.url=tcp://rkanter-5.ent.cloudera.com:61616
connectionFactoryNames=ConnectionFactory

Listening on topics:
	admin
	flows


=== Job Message ===
JMSTopic            : flows
JMSTimestamp        : Fri Mar 28 14:13:12 PDT 2014
JMSMessageID        : ID:rkanter-5.ent.cloudera.com-58838-1396040552669-1:1:4:3:1
appType             : WORKFLOW_JOB
id                  : 0000002-140328140155969-oozie-oozi-W
parentId            : 0000000-140328135718092-oozie-oozi-C@4
startTime           : Fri Mar 28 14:12:13 PDT 2014
endTime             : Fri Mar 28 14:13:10 PDT 2014
eventStatus         : SUCCESS
appName             : MapReduce
user                : admin

...
````
