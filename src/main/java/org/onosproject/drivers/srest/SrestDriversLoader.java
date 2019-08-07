/*
 * Copyright 2016-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.drivers.srest;

import org.osgi.service.component.annotations.Component;
import org.onosproject.net.driver.AbstractDriverLoader;
import org.onosproject.net.config.ConfigFactory;
import org.onosproject.net.config.NetworkConfigRegistry;
import org.onosproject.net.DeviceId;
import org.onosproject.net.config.basics.SubjectFactories;
import static org.slf4j.LoggerFactory.getLogger;
import org.slf4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.onlab.util.SharedExecutors;
import org.onlab.util.SharedScheduledExecutorService;
import org.onlab.util.SharedScheduledExecutors;

import org.onosproject.net.config.NetworkConfigEvent;
import org.onosproject.net.config.NetworkConfigListener;
import org.onosproject.net.config.NetworkConfigRegistry;
import java.util.concurrent.ExecutorService;
import static org.onosproject.net.config.NetworkConfigEvent.Type.CONFIG_ADDED;
import static org.onosproject.net.config.NetworkConfigEvent.Type.CONFIG_REMOVED;
import static org.onosproject.net.config.NetworkConfigEvent.Type.CONFIG_UPDATED;

@Component(immediate = true)
public class SrestDriversLoader extends AbstractDriverLoader {

    private final Logger log = getLogger(getClass());

    private NetworkConfigListener configListener=new SrestNetworkConfigListener();
    
    protected final ConfigFactory factory =
	new ConfigFactory<DeviceId, SrestDeviceConfig>(
                    SubjectFactories.DEVICE_SUBJECT_FACTORY,
                    SrestDeviceConfig.class, SrestDeviceConfig.CONFIG_KEY) {
                @Override
                public SrestDeviceConfig createConfig() {
                   return new SrestDeviceConfig();
                }
            };
    
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
   protected NetworkConfigRegistry netCfgService;

    public SrestDriversLoader() {
        super("/srest-drivers.xml");
    }

    @Activate
    @Override
    protected void activate() {
        super.activate();
        netCfgService.registerConfigFactory(factory);
	netCfgService.addListener(configListener);
	log.info("Srest drivers loaded Activated");
		
    }

    

    

    // code borrowed from TBC

    private class SrestNetworkConfigListener implements NetworkConfigListener {
        @Override
        public void event(NetworkConfigEvent event) {
            if (!isRelevant(event)) {
                log.warn("Irrelevant network configuration event: {}", event);
                return;
            }

            ExecutorService bg = SharedExecutors.getSingleThreadExecutor();
            if (event.type() == CONFIG_REMOVED) {
                log.info("Config {} event for rest device provider for {}",
			 event.type(), event.prevConfig().get().subject());
                DeviceId did = (DeviceId) event.subject();
            } else {
                //CONFIG_ADDED or CONFIG_UPDATED
                log.info("Config {} event for rest device provider for {}",
			 event.type(), event.config().get().subject());
                SrestDeviceConfig cfg = (SrestDeviceConfig) event.config().get();
		bg.execute(() -> cfg.addOrUpdate(event));
            }
        }

        @Override
        public boolean isRelevant(NetworkConfigEvent event) {
            return event.configClass().equals(SrestDeviceConfig.class) &&
		(event.type() == CONFIG_ADDED ||
		 event.type() == CONFIG_UPDATED ||
		 event.type() == CONFIG_REMOVED);
        }
    }

}
