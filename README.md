ChemBuddyGlassware
==================

Summary: A glassware geared towards effective STEM Education

This application is designed to help instructors and students in engaging in effective an STEM education experience.
This glassware app uses Mirror API to receive instructions posted by the teachers and posts the laboratory experiments onto the students google glass devices. This will be then used by them during their lab sessions.

Implementation:
---

We adapted the Google Mirror API - <a href="https://developers.google.com/glass/develop/mirror/quickstart/index" >Java Quickstart code</a> and implemented custom addition of timeline cards and also interface with GDK to invoke the native camera app (See ChemBuddy repository for the GDK app).

How to run and test the Glassware WebApp (locally):
---

* To Compile

```
mvn clean; mvn package
```

* To Run the web server

```
mvn jetty:run
```

* Navigate to http://localhost:8080 in the browser
* Paste the contents of input.xml into the textbox and click on Create Experiment button

Demo:
---

<a href="https://www.youtube.com/watch?v=IYHPeKW5Vdk&feature=youtu.be">Youtube screencast</a>
