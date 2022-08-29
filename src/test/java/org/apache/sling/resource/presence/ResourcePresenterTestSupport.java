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

import org.apache.sling.testing.paxexam.TestSupport;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.ModifiableCompositeOption;

import static org.apache.sling.testing.paxexam.SlingOptions.slingQuickstartOakTar;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.factoryConfiguration;

public abstract class ResourcePresenterTestSupport extends TestSupport {

    protected static final String FACTORY_PID = "org.apache.sling.resource.presence.internal.ResourcePresenter";

    protected ModifiableCompositeOption baseConfiguration() {
        return composite(
            super.baseConfiguration(),
            slingQuickstart(),
            // Sling Resource Presenter
            testBundle("bundle.filename"),
            factoryConfiguration("org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended")
                .put("user.mapping", new String[]{"org.apache.sling.resource.presence=[sling-readall]"})
                .asOption(),
            // testing
            mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.hamcrest").versionAsInProject()
        );
    }

    protected Option slingQuickstart() {
        final int httpPort = findFreePort();
        final String workingDirectory = workingDirectory();
        return slingQuickstartOakTar(workingDirectory, httpPort);
    }

}
