# TopBraid SHACL API

**An open source implementation of the evolving W3C Shapes Constraint Language (SHACL) based on the Jena API.**

Contact: Holger Knublauch (holger@topquadrant.com)

Can be used to perform SHACL constraint checking in any Jena-based Java application.
This API also serves as a reference implementation developed in parallel to the SHACL spec.
**The code is totally not optimized for performance, just for correctness. And there are unfinished gaps!**

The same code is used in the TopBraid products (currently ahead of the release cycle, TopBraid 5.2 will catch up).
For interoperability with TopBraid, and during the transition period from SPIN to SHACL, this library
uses code from org.topbraid.spin packages. These will eventually be refactored.
Meanwhile, please don't rely on any class from the org.topbraid.spin packages directly. 

Feedback and questions should go to TopBraid Users mailing list:
https://groups.google.com/forum/#!forum/topbraid-users
Please prefix your messages with [SHACL API]

To get started, look at the classes ModelConstraintValidator and ResourceConstraintValidator in
the package org.topbraid.shacl.constraints. There is also an [Example Test Case](../master/src/test/java/org/topbraid/shacl/ValidationExample.java)

## Running 

This fork includes a simple validator that reads model from stdin and returns with a nonzero exit code if validation fails (if the validation results model is non-empty).

To compile:

```
mvn clean compile assembly:single
```

To run:

```
cat ./src/test/resources/sh/tests/core/property/class-001.test.ttl | java -jar ./target/shacl-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```