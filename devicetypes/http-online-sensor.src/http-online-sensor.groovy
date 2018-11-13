/**
 *  HTTP Online/Offline Sensor
 *
 *  Copyright 2018 Joel Wetzel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Http Online Sensor", namespace: "joelwetzel", author: "Joel Wetzel") {
		capability "Refresh"
		capability "Sensor"
	}

	tiles {
		standardTile("sensor", "device.sensor", width: 2, height: 2, canChangeBackground: false) {
			state "inactive", label: "OFFLINE", backgroundColor:"#ee4444"
			state "active", label: "ONLINE", backgroundColor:"#44ee44"
		}
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        main "sensor"
        details(["sensor", "refresh"])
	}
}

def installed () {
	log.debug "Device Handler installed"
    updated()
}

def updated () {
	log.debug "Device Handler updated"
    
    state.tryCount = 0
    
    runEvery1Minute(refresh)
    runIn(2, refresh)
}

// parse events into attributes
def parse(description) {
//	log.debug "Execute: parse"
    
    def msg = parseLanMessage(description)
//    log.debug "HTTP Status: ${msg.status}"
    
    if (msg.status == 200) {
        state.tryCount = 0
        
        def deviceName = getLinkText(device);
        def descriptionText = "${deviceName} is ONLINE";
        log.debug descriptionText
        sendEvent(name: "sensor", value: "active", linkText: deviceName, descriptionText: descriptionText)
    }
}

// handle commands
def refresh() {
	log.debug "Execute: refresh"

	state.tryCount = state.tryCount + 1

//	log.debug "state.tryCount: ${state.tryCount}"
    
    if (state.tryCount > 3) {
//    	log.debug "***** OFFLINE *****"
        
        def deviceName = getLinkText(device);
        def descriptionText = "${deviceName} is OFFLINE";
        log.debug descriptionText
        sendEvent(name: "sensor", value: "inactive", linkText: deviceName, descriptionText: descriptionText)
    }
    
  	def command = getPingCommand()
    sendHubCommand(command)
}


def getPingCommand() {
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/",
        headers: [
            HOST: getHostAddress()
        ]
    )
    
    return result
}


// gets the address of the device
private getHostAddress() {
    def ip = getDataValue("ip")
    def port = getDataValue("port")

    if (!ip || !port) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 2) {
            ip = parts[0]
            port = parts[1]
        } else {
            log.warn "Can't figure out ip and port for device: ${device.id}"
        }

    	if (ip.startsWith("192.")) {
        	log.error "Device Network Id is human readable: " + ip + ":" + port
            log.error "Device Network Id must be HEX encoded: " + convertIPtoHex(ip) + ":" + convertPortToHex(port)
            return ip + ":" + port
        }
	}

    return convertHexToIP(ip) + ":" + convertHexToInt(port)
}

private Integer convertHexToInt(hex) {
    return Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    return [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}
