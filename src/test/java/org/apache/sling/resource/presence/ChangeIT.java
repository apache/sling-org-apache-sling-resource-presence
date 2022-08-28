/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.resource.presence;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.sling.testing.paxexam.SlingOptions.awaitility;
import static org.awaitility.Awaitility.with;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.factoryConfiguration;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.newConfiguration;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class ChangeIT extends ResourcePresenterTestSupport {

    @Inject
    private BundleContext bundleContext;

    @Inject
    private ResourceResolverFactory resourceResolverFactory;

    @Configuration
    public Option[] configuration() {
        return options(
            baseConfiguration(),
            factoryConfiguration(FACTORY_PID)
                .put("path", "/test")
                .asOption(),
            newConfiguration("org.apache.sling.jcr.base.internal.LoginAdminWhitelist")
                .put("whitelist.bundles.regexp", "PAXEXAM-PROBE-.*")
                .asOption(),
            awaitility()
        );
    }

    @Test
    public void testChange() throws Exception {
        final CountingServiceListener countingServiceListener = new CountingServiceListener();
        final String filter = "(&(objectClass=org.apache.sling.resource.presence.ResourcePresence)(path=/test))";
        bundleContext.addServiceListener(countingServiceListener, filter);

        assertThat(countingServiceListener.registeredCount(), is(0));
        assertThat(countingServiceListener.unregisteredCount(), is(0));

        // create/register
        try (ResourceResolver resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null)) {
            final Resource root = resourceResolver.getResource("/");
            final Resource test = resourceResolver.create(root, "test", null);
            resourceResolver.commit();
            with().
                pollInterval(100, MILLISECONDS).
                then().
                await().
                alias("counting register events").
                atMost(1, SECONDS).
                until(() -> countingServiceListener.registeredCount() == 1);

            // delete/unregister
            resourceResolver.delete(test);
            resourceResolver.commit();
            with().
                pollInterval(100, MILLISECONDS).
                then().
                await().
                alias("counting unregister events").
                atMost(1, SECONDS).
                until(() -> countingServiceListener.unregisteredCount() == 1);
        }
    }

    private static class CountingServiceListener implements ServiceListener {

        private final AtomicInteger registeredCount = new AtomicInteger();

        private final AtomicInteger unregisteredCount = new AtomicInteger();

        public int registeredCount() {
            return registeredCount.get();
        }

        public int unregisteredCount() {
            return unregisteredCount.get();
        }

        @Override
        public void serviceChanged(final ServiceEvent serviceEvent) {
            final int type = serviceEvent.getType();
            if (type == ServiceEvent.REGISTERED) {
                registeredCount.incrementAndGet();
            } else if (type == ServiceEvent.UNREGISTERING) {
                unregisteredCount.incrementAndGet();
            }
        }

    }

}
