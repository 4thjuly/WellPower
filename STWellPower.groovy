/**
 *  Well Power Monitor
 *
 *  Copyright 2020 Ian Ellison-Taylor
 *
 */

metadata {
	definition (name: "Well Monitor", namespace: "4thjuly", author: "Ian Ellison-Taylor") {
        capability "Polling"
        capability "Refresh"
        capability "Sensor"
        capability "Power Meter"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
        valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
            state "power", label:'Level\n${currentValue}'
        }
        valueTile("ison", "device.ison", decoration: "flat", width: 2, height: 2) {
            state "ison", label:'${currentValue}'
        }
        valueTile("ontime", "device.ontime", decoration: "flat", width: 2, height: 2) {
            state "ontime", label:'On\n ${currentValue}s'
        }
        valueTile("offtime", "device.offtime", decoration: "flat", width: 2, height: 2) {
            state "offtime", label:'Off\n ${currentValue}s'
        }
        standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
			state "icon", action:"Refresh.refresh", icon:"st.secondary.refresh", defaultState: true
		}

        main("ison");
        details(["ison", "ontime", "offtime", "power", "refresh"])
    }
    
    preferences {
        input("token", "text", title: "Access Token")
        input("deviceId", "text", title: "Device ID")
    }
}

def initialize() {
    log.debug "Initialize: ${new Date()}"
}

def installed() {
    log.debug "Installed: ${new Date()}"
	updated();
}

def updated() {
    log.debug "Updated: ${new Date()}"
    unschedule()
    runEvery1Minute(refresh)
}

def refresh() {
    log.debug "Refresh: ${new Date()}"
    getDeviceStatus();
}

def parse(String description) {
	log.debug "Parsing '${description}'"
}

def poll() {
    log.debug "Poll: ${new Date()}"
    getDeviceStatus();
}

private getDeviceStatus(key) {
	try {
        httpGet("https://api.particle.io/v1/devices/${deviceId}/IsOn?access_token=${token}") { response -> 
     		log.debug "getDeviceStatus: $response.data.name $response.data.result"           
            if (response.data.result == true) { 
                sendEvent(name: 'ison', value: 'On')
            } else {
                sendEvent(name: 'ison', value: 'Off')
            }
        }
        httpGet("https://api.particle.io/v1/devices/${deviceId}/OnTimeDelta?access_token=${token}") { response -> 
     		log.debug "getDeviceStatus: $response.data.name $response.data.result"           
            sendEvent(name: 'ontime', value: response.data.result)
        }
        httpGet("https://api.particle.io/v1/devices/${deviceId}/OffTimeDelta?access_token=${token}") { response -> 
     		log.debug "getDeviceStatus: $response.data.name $response.data.result"           
            sendEvent(name: 'offtime', value: response.data.result)
        }
        httpGet("https://api.particle.io/v1/devices/${deviceId}/PowerLevel?access_token=${token}") { response -> 
     		log.debug "getDeviceStatus: $response.data.name $response.data.result"           
            sendEvent(name: 'power', value: response.data.result)
        }
    } catch (exc) {
    	log.debug "Exception: $exc"
    }
}
