[![Apache Sling](https://sling.apache.org/res/logos/sling.png)](https://sling.apache.org)

&#32;[![Build Status](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-resource-presence/job/master/badge/icon)](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-resource-presence/job/master/)&#32;[![Test Status](https://img.shields.io/jenkins/tests.svg?jobUrl=https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-resource-presence/job/master/)](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-resource-presence/job/master/test/?width=800&height=600)&#32;[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=apache_sling-org-apache-sling-resource-presence&metric=coverage)](https://sonarcloud.io/dashboard?id=apache_sling-org-apache-sling-resource-presence)&#32;[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=apache_sling-org-apache-sling-resource-presence&metric=alert_status)](https://sonarcloud.io/dashboard?id=apache_sling-org-apache-sling-resource-presence)&#32;[![JavaDoc](https://www.javadoc.io/badge/org.apache.sling/org.apache.sling.resource.presence.svg)](https://www.javadoc.io/doc/org.apache.sling/org.apache.sling.resource.presence)&#32;[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.resource.presence/badge.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.sling%22%20a%3A%22org.apache.sling.resource.presence%22) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# Apache Sling Resource Presence

This module is part of the [Apache Sling](https://sling.apache.org) project.

This module creates proxy OSGi services that are registered only if specific [resources](https://sling.apache.org/documentation/the-sling-engine/resources.html) are present.

Like for example:

	@Reference(target="(path=/content/foo/bar)")
	private ResourcePresence barIsPresent;
	
The `barIsPresent` service is registered only if the `/content/foo/bar` resource is present, allowing OSGi
components to be dependent on the presence of specific resources.

This is mostly meant for testing, to wait for test content before running specific tests.

Getting Started
---------------

1. Configure a service user mapping for `org.apache.sling.resource.presence` to allow reading resources, using e.g. service user `sling-readall`.
2. Configure a presenter to observe a resource by path, e.g. `path=/apps`
   
   Whenever resource `/apps` is available the presenter will register an OSGi service for it and unregister it whenever `/apps` gets removed.
3. You can depend on that service now, e.g. using a `@Reference` annotation with a `target` on your component:

   ```
       @Reference(
           target = "(path=/apps)"
       )
       private ResourcePresence apps;
   ```

Using Resource Presence with Pax Exam
-------------------------------------

When running tests with resources involved, you can use a resource presence to delay test execution until required resources are available.

    @Inject
    @Filter(value = "(path=/apps)")
    private ResourcePresence apps;

    @Configuration
    public Option[] configuration() {
        return options(
            [...],
            factoryConfiguration("org.apache.sling.resource.presence.internal.ResourcePresenter")
                .put("path", "/apps")
                .asOption()
        );
    }

    @Test
    public void testApps() {
        assertThat(apps.getPath(), is("/apps"));
    }
