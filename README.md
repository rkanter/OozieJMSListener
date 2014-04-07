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
./run.sh <oozie-url> [-username <username>] [-jobid <jobId>]
```


Sample Output
=============
````
$ ./run.sh http://rkanter-5.ent.cloudera.com:11000/oozie -username admin
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

=== SLA Message ===
JMSTopic            : admin
JMSTimestamp        : Mon Apr 07 15:52:43 PDT 2014
JMSMessageID        : ID:rkanter-5.ent.cloudera.com-52410-1396574282684-1:1:8:18:1
appType             : WORKFLOW_JOB
id                  : 0000012-140403181356152-oozie-oozi-W
parentId            : null
nominalTime         : Wed Apr 02 23:00:00 PDT 2014
expectedStartTime   : Wed Apr 02 23:10:00 PDT 2014
actualStartTime     : Mon Apr 07 15:51:07 PDT 2014
expectedEndTime     : Wed Apr 02 23:30:00 PDT 2014
actualEndTime       : Mon Apr 07 15:52:16 PDT 2014
expectedDuration    : 1800000
actualDuration      : 69218
notificationMessage : woot
upstreamApps        : blah
user                : admin
appName             : MapReduce-SLA
eventStatus         : DURATION_MET
slaStatus           : MISS

...
````
