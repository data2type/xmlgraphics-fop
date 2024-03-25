# Apache FOP (Accessibility Fork)

This Readme describes the derivative work of the fork only. Please find the origin Readme of FOP [here](README).

## Fork Disclaimer

This repository is a fork of the [official Apache FOP](https://github.com/apache/xmlgraphics-fop) repository. It contains adjustments to increase the Accessibility mode of FOP.

There are three JIRA issues created to offer the adjustments/bug fixes as contribution to the baseline project:

* [FOP-3007](https://issues.apache.org/jira/browse/FOP-3007)
* [FOP-3165](https://issues.apache.org/jira/browse/FOP-3165)
* [FOP-3167](https://issues.apache.org/jira/browse/FOP-3167)

The provided patches or mentioned adjustments are applied to this project and maybe taken over into the baseline project in future.

### Samples

Samples for the usage of the adjusted accessibility feature can be found in [fop/examples/accessibility/](fop/examples/accessibility/Readme.md).

## How to get

The adjusted version of FOP is deployed as Maven artifacts on a public Maven repository of the data2type GmbH. To include the FOP into your Maven project you have to add the following to the `<repositorys>` section of your pom.xml:

```xml
<repository>
    <id>de.data2type.repo</id>
    <url>https://repo.data2type.de/repository/maven-public/</url>
</repository>
```

Now you are able to add the FOP as project dependency:

```xml
<dependency>
    <groupId>de.data2type.forks.fop</groupId>
    <artifactId>fop</artifactId>
    <version>2.9a</version>
</dependency>
```

### Versioning

The distributions published Maven artifacts are marked with the base FOP version suffixed by an `a`. For instance the version `2.9a` is based on FOP `2.9`.
 

## Authorship of the Derivation Work

The adjustmens of this project was commissioned by the [BSI](https://www.bsi.bund.de/EN/Home/home_node.html) (German Federal Office for Information Security). Data2type as publisher is just one of the implementers.
