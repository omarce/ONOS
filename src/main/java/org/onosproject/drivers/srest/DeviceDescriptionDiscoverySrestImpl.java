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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import org.onosproject.net.driver.DriverHandler;
import org.onosproject.protocol.rest.RestSBController;
import org.onosproject.protocol.rest.RestSBDevice;
import java.io.InputStream;
import javax.ws.rs.core.MediaType;


public class DeviceDescriptionDiscoverySrestImpl extends AbstractHandlerBehaviour
        implements DeviceDescriptionDiscovery {
    
    private final Logger log = getLogger(getClass());


    @Override
    public DeviceDescription discoverDeviceDetails() {
	log.info("SREST:discoverDeviceDetails");


	// get handler
	DriverHandler handler=handler();
	RestSBController controller=handler.get(RestSBController.class);
	DeviceId deviceId=handler.data().deviceId();
	InputStream is=controller.get(deviceId,"/DeviceDescription.json",MediaType.valueOf(MediaType.APPLICATION_JSON));

	HashMap<String,String> map=new HashMap<String,String>();
	ObjectMapper mapper=new ObjectMapper();
	HashMap<String,String> def=new HashMap<String,String>();
	def.put("manufacturer","Unknown");
 	def.put("hw_version","Unknown");
	def.put("sw_version","Unknown");
	def.put("serial_number","Unknown");
      
	try{
	    map=mapper.readValue(is,map.getClass());
	} catch (Exception e) {
            log.error("Exception occurred because of {}, trace: {}", e, e.getStackTrace());
            return null;
        }

	def.putAll(map);
	


	DefaultDeviceDescription desc=
	    new DefaultDeviceDescription(deviceId.uri(),
					 Device.Type.SWITCH,
					 def.get("manufacturer"),
					 def.get("hw_version"),
					 def.get("sw_version"),
					 def.get("serial_number"),
					 new ChassisId());
					 
	

	return desc;

    }

    @Override
    public List<PortDescription> discoverPortDetails() {
	log.info("SREST:discoverPortDetails");
	
        List<PortDescription> ports = Lists.newArrayList();
	DriverHandler handler=handler();
	RestSBController controller=handler.get(RestSBController.class);
	DeviceId deviceId=handler.data().deviceId();
	InputStream is=controller.get(deviceId,"/PortsDescription.json",MediaType.valueOf(MediaType.APPLICATION_JSON));
	ObjectMapper mapper=new ObjectMapper();
	List<HashMap<String,String>> p;
	HashMap<String,String> map;
	
	try{
	    p=mapper.readValue(is,new TypeReference<List<HashMap<String,String>>>() {});
	} catch (Exception e) {
            log.error("Exception occurred because of {}, trace: {}", e, e.getStackTrace());
            return null;
        }
	ListIterator li=p.listIterator();
	PortDescription portD;
	
	try{	
	    while( li.hasNext() ){
		map=(HashMap<String,String>)li.next();
		portD = DefaultPortDescription.builder()
		    .withPortNumber(PortNumber.portNumber(map.get("port_number")))
		    .isEnabled(true)
		    .type(Port.Type.FIBER)
		    .portSpeed(1000)
		    .build();
		ports.add(portD);
		
	    }
	} catch (Exception e) {
            log.error("Exception occurred because of {}, trace: {}", e, e.getStackTrace());
        }

       
	return ports;
    }


}
