Apache Sling Resource Presence
==============================

This module allows presentation of `Resource`s as OSGi services and comes with a simple presenter and presence.

Getting Started
---------------

1. Configure a service user mapping for `org.apache.sling.resource.presence` to allow reading resources, using e.g. service user `sling-readall`.
2. Configure a presenter to observe a resource by path, e.g. `path=/apps`
   
   Whenever resource `/apps` is available the presenter will register an OSGi service for it and unregisters the service whenever `/apps` gets removed.
3. You can depend on that service now, e.g. using a `@Reference` annotation on your component:

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
        return new Option[]{
            [...],
            factoryConfiguration("org.apache.sling.resource.presence.internal.ResourcePresenter")
                .put("path", "/apps")
                .asOption()
        };
    }

    @Test
    public void testApps() {
        assertThat(apps.getPath(), is("/apps"));
    }
