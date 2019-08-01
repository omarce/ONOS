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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;


import com.google.common.collect.Lists;
import org.onlab.packet.ChassisId;
import org.onlab.packet.MacAddress;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.SparseAnnotations;
import org.onosproject.net.device.DefaultDeviceDescription;
import org.onosproject.net.device.DefaultPortDescription;
import org.onosproject.net.device.DeviceDescription;
import org.onosproject.net.device.DeviceDescriptionDiscovery;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.device.PortDescription;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ListIterator;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import org.onosproject.net.driver.DriverHandler;
import org.onosproject.protocol.rest.RestSBController;
import org.onosproject.protocol.rest.RestSBDevice;
import java.io.InputStream;
import javax.ws.rs.core.MediaType;
import org.onosproject.net.Link;
import org.onosproject.net.behaviour.LinkDiscovery;
import org.onosproject.net.link.DefaultLinkDescription;
import org.onosproject.net.link.LinkDescription;
import org.onosproject.net.ConnectPoint;


public class LinkDiscoverySrestImpl extends AbstractHandlerBehaviour implements LinkDiscovery {

    private final Logger log = getLogger(getClass());

    @Override
    public Set<LinkDescription> getLinks() {

	
	DriverHandler handler=handler();
	RestSBController controller=handler.get(RestSBController.class);
	DeviceId deviceId=handler.data().deviceId();
	InputStream is=controller.get(deviceId,"/Links.json",MediaType.valueOf(MediaType.APPLICATION_JSON));
	ObjectMapper mapper=new ObjectMapper();
	List<HashMap<String,String>> p;
	HashMap<String,String> map;
	ConnectPoint src,dst;
	HashSet<LinkDescription> links=new HashSet<LinkDescription>();
	DefaultLinkDescription ld;
	
	try{
	    p=mapper.readValue(is,new TypeReference<List<HashMap<String,String>>>() {});
	    ListIterator li=p.listIterator();
	
	    while( li.hasNext() ){
		map=(HashMap<String,String>)li.next();
		src=ConnectPoint.deviceConnectPoint(map.get("src"));
		dst=ConnectPoint.deviceConnectPoint(map.get("dst"));
		ld=new DefaultLinkDescription(src,dst,Link.Type.DIRECT);
		links.add(ld);
	    }
	} catch (Exception e) {
            log.error("Exception occurred because of {}, trace: {}", e, e.getStackTrace());
        }

       
	return links;
    }


}
