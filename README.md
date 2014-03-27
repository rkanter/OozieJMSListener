OozieJMSListener
================

This tool will listen for JMS notification messages for jobs and for SLAs.  
It is based on: http://archive.cloudera.com/cdh5/cdh/5/oozie/DG_JMSNotifications.html

Build
-----
``./build.sh``

Run
---
``./run.sh <oozie-url> [username]``


Sample Output
=============
````
=== Job Message ===
JMSTimestamp        : Thu Mar 27 16:03:12 PDT 2014
JMSMessageID        : ID:rkanter-5.ent.cloudera.com-40040-1395958622929-1:1:6:7:1
appType             : WORKFLOW_JOB
id                  : 0000012-140327151246791-oozie-oozi-W
parentId            : 0000009-140327151246791-oozie-oozi-C@3
startTime           : Thu Mar 27 16:03:05 PDT 2014
endTime             : null
eventStatus         : STARTED
appName             : MapReduce
user                : admin
````
