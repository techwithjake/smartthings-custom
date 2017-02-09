/**
 * V 1.5 of Multisensor 6 code
 *	known issues : preferences do not seem to affect configuration, secure pairing is the most tested method at the moment. when clicking the action button on *  the sensor, click two times quickly to pair secured. Still not seeing anything other than 0 for ultraviolet. Also, temp and humidity seem to be off about 6 *	degrees warmer and 7 percent humidity lower than actual, though your sensor may vary.
 
 *  Original code for gen5 Copyright 2015 SmartThings, modified for use on gen 6 by Robert Vandervoort
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
 * Edited by TechWithJake
 */
metadata {
	definition (name: "Aeon Multisensor 6", namespace: "robertvandervoort", author: "Robert Vandervoort") {
		capability "Motion Sensor"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Illuminance Measurement"
		capability "Configuration"
		capability "Sensor"
		capability "Battery"
		fingerprint deviceId: "0x2101", inClusters: "0x5E,0x86,0x72,0x59,0x85,0x73,0x71,0x84,0x80,0x30,0x31,0x70,0x7A,0xEF,0x5A,0x98,0x7A"
		}
	simulator {
		status "no motion" : "command: 9881, payload: 00300300"
		status "motion"    : "command: 9881, payload: 003003FF"
        status "no vibration" : " command: 9881, payload: 0071050000000007030000"
        status "vibration" : "command: 9881, payload: 007105000000FF07030000"
        
        for (int i = 0; i <= 100; i += 20) {
			status "temperature ${i}F": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                	scaledSensorValue: i,
                    precision: 1,
                    sensorType: 1,
                    scale: 1
				)
			).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 20) {
			status "RH ${i}%": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                	scaledSensorValue: i,
                    sensorType: 5
            	)
			).incomingMessage()
		}
		for (int i in [0, 20, 89, 100, 200, 500, 1000]) {
			status "illuminance ${i} lux": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                scaledSensorValue: i,
                sensorType: 3
                )
			).incomingMessage()
		}
		for (int i = 0; i <= 11; i += 1) {
			status "ultraviolet ${i}": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                scaledSensorValue: i,
                sensorType: 27
                )
			).incomingMessage()
		}
		for (int i in [0, 5, 10, 15, 50, 99, 100]) {
			status "battery ${i}%": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().batteryV1.batteryReport(
                batteryLevel: i
                )
			).incomingMessage()
		}
		status "low battery alert": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
				new physicalgraph.zwave.Zwave().batteryV1.batteryReport(
            	batteryLevel: 255
            	)
			).incomingMessage()
		status "wake up": "command: 8407, payload:"
	}
	tiles {
		standardTile("motion","device.motion") {
            	state "active",label:'motion',icon:"st.motion.motion.active",backgroundColor:"#53a7c0"
                state "inactive",label:'no motion',icon:"st.motion.motion.inactive",backgroundColor:"#ffffff"
			}
		valueTile("temperature","device.temperature",inactiveLabel: false) {
            	state "temperature",label:'${currentValue}°',backgroundColors:[
                	[value: 32, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 92, color: "#d04e00"],
					[value: 98, color: "#bc2323"]
				]
			}
		valueTile(
        	"humidity","device.humidity",inactiveLabel: false) {
            	state "humidity",label:'RH ${currentValue} %',unit:""
			}
		valueTile(
        	"illuminance","device.illuminance",inactiveLabel: false) {
            	state "luminosity",label:'${currentValue} ${unit}',unit:"lux"
			}
		valueTile(
        	"ultraviolet","device.ultraviolet",inactiveLabel: false) {
				state "ultraviolet",label:'UV ${currentValue} ${unit}',unit:""
			}
		standardTile(
        	"vibration","device.vibration") {
				state "active",label:'vibration',icon:"st.motion.motion.active",backgroundColor:"#ff0000"
                state "inactive",label:'calm',icon:"st.motion.motion.inactive",backgroundColor:"#00ff00"
			}
		valueTile(
			"battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		standardTile(
        	"configureAfterSecure","device.configure",inactiveLabel: false,decoration: "flat") {
				state "configure",label:'',action:"configureAfterSecure",icon:"st.secondary.configure"
			}
		main([
        	"motion","temperature","humidity","illuminance","ultraviolet"
            ])
		details([
        	"motion","temperature","humidity","illuminance","ultraviolet","battery","configureAfterSecure"
            ])
	}
    preferences {
		input "tempoffset",
			"number",
			title: "Temperature offset",
            description: "negative values reduce the monitored value positive ones add to it",
            defaultValue: 0,
            required: false,
            displayDuringSetup: false
		input "humidityoffset",
        	"number",
            title: "Humidity offset",
            description: "negative values reduce the monitored value positive ones add to it",
			defaultValue: 0,
			required: false,
            displayDuringSetup: false
		input "luminanceoffset",
          	"number",
            title: "Luminance offset",
            description: "negative values reduce the monitored value positive ones add to it",
            defaultValue: 0,
            required: false,
	        displayDuringSetup: false
		input "ultravioletoffset",
          	"number",
            title: "Ultraviolet offset",
            description: "negative values reduce the monitored value positive ones add to it",
            defaultValue: 0,
	        required: false,
			displayDuringSetup: false
		input "PIRsensitivity",
	        "number",
    	    title: "PIR motion sensitivity",
			description: "A value from 0-127 low to high sensitivity",
			defaultValue: 64,
			required: false,
			displayDuringSetup: false
		input "ReportingInterval",
        	"number",
            title: "Report data every X seconds",
            description: "A value in seconds, default is 60 / 8 minutes",
            defaultValue: 4,
            required: false,
            displayDuringSetup: false
	}
}

def parse(String description) {
	def result = null
    if (description == "updated") {
        result = null
		}
	else {
       	def cmd = zwave.parse(description, [0x31: 5, 0x30: 2, 0x84: 1])
		if (cmd) {
       		result = zwaveEvent(cmd)
		}
	}
	log.debug "Parsed '${description}' to ${result.inspect()}"
	return result
}
def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]

	if (!isConfigured()) {
		// we're still in the process of configuring a newly joined device
		log.debug("not sending wakeUpNoMoreInformation yet")
        result += response(configureAfterSecure())
	} else {
		result += response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x31: 5, 0x30: 2, 0x84: 1])
	log.debug "encapsulated: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
    log.debug "Received SecurityCommandsSupportedReport"
	response(configureAfterSecure())
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbatt = new Date().time
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
	def map = [:]
	switch (cmd.sensorType) {
		case 1:
			map.name = "temperature"
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			break;
		case 3:
			map.name = "illuminance"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "lux"
			break;
        case 5:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "%"
			break;
		case 27:
        	map.name = "ultraviolet"
            map.value = cmd.scaledSensorValue.toInteger()
            map.unit = ""
            break;
		default:
			map.descriptionText = cmd.toString()
	}
	createEvent(map)
}

def motionEvent(value) {
	def map = [name: "motion"]
	if (value == 255) {
		map.value = "active"
		map.descriptionText = "$device.displayName detected motion"
	} else {
		map.value = "inactive"
		map.descriptionText = "$device.displayName motion has stopped"
	}
	createEvent(map)
}
def vibrationEvent(value) {
	def map = [name: "vibration"]
	if (value == 255) {
		map.value = "active"
		map.descriptionText = "$device.displayName detected vibration"
	} else { 
		log.debug "-------------Vibration inactive------------------"
		map.value = "inactive"
		map.descriptionText = "$device.displayName vibration has stopped"
	}
	createEvent(map)
}
def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	motionEvent(cmd.sensorValue)
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	log.debug "Basic Set 255 triggered motion event"
	motionEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	if (cmd.notificationType == 7 && cmd.event == 8) {
		log.debug "notification type 7 event 8 triggered motion event"
		motionEvent(cmd.notificationStatus)
	} else if (cmd.notificationType == 7 && cmd.event == 0) {
		log.debug "notification type 7 event 0 motion and vibration cleared"
		motionEvent(cmd.notificationStatus)
		vibrationEvent(cmd.notificationStatus)
	} else if (cmd.notificationType == 7 && cmd.event == 3) {
		log.debug "notification type 7 event 3 triggered vibration event"
		vibrationEvent(cmd.notificationStatus)
	} else {
		createEvent(descriptionText: cmd.toString(), isStateChange: false)
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

def configureAfterSecure() {
	log.debug "configureAfterSecure()"
    //log.debug "PIRsensitivity: $PIRsensitivity, Reporting Interval: $ReportingInterval, Temp offset: $tempoffset, Humidity offset: $humidityoffset, Luminance offset: $luminanceoffset, UV offset: $ultravioletoffset"

	def PIRsens = 64
	if (PIRsensitivity) {
		PIRsens=PIRsensitivity.toInteger()
	}
	def ReportingInt = 4
	if (ReportingInterval) {
		ReportingInt=ReportingInterval.toInteger()
	}
	def tempoff = 0
	if (tempoffset) {
		tempoff=tempoffset.toInteger()
	}
	def humidityoff = 0
	if (humidityoffset) {
		humidityoff=humidityoffset.toInteger()
	}
	def luminanceoff = 0
	if (luminanceoffset) {
		luminanceoff=luminanceoffset.toInteger()
	}
	def ultravioletoff = 0
	if (ultravioletoffset) {
		ultravioletoff=ultravioletoffset.toInteger()
	}
    //log.debug "PIRsens: $PIRsens, ReportingInt: $ReportingInt, Tempoffset: $tempoff, Humidityoff: $humidityoff, Luminanceoff: $luminanceoff, UVoff: $ultravioletoff"
	def request = [
		// send temperature, humidity, illuminance, ultraviolet and battery
		zwave.configurationV1.configurationSet(parameterNumber: 0x65, size: 4, scaledConfigurationValue: 128|64|32|16|1),
		// configure frequency of reporting 
		zwave.configurationV1.configurationSet(parameterNumber: 0x6F,size: 4, scaledConfigurationValue: ReportingInt),
		// configure PIR sensitivity
		zwave.configurationV1.configurationSet(parameterNumber: 0x06,size: 1, scaledConfigurationValue: PIRsens),
		// send battery every 20 hours
		zwave.configurationV1.configurationSet(parameterNumber: 0x66, size: 4, scaledConfigurationValue: 1),
		zwave.configurationV1.configurationSet(parameterNumber: 0x70, size: 4, scaledConfigurationValue: 20*60*60),
        // send no-motion report 60 seconds after motion stops
		zwave.configurationV1.configurationSet(parameterNumber: 0x03, size: 2, scaledConfigurationValue: 60),
		// enable motion sensor
        zwave.configurationV1.configurationSet(parameterNumber: 0x04, size: 1, scaledConfigurationValue: 0),
		// send binary sensor report instead of basic set for motion
		zwave.configurationV1.configurationSet(parameterNumber: 0x05, size: 1, scaledConfigurationValue: 2),
		// Enable the function of vibration sensor
        zwave.configurationV1.configurationSet(parameterNumber: 0x07, size: 1, scaledConfigurationValue: 1),
		// disable notification-style motion events
		zwave.notificationV3.notificationSet(notificationType: 7, notificationStatus: 0),
        // configure temp offset
		zwave.configurationV1.configurationSet(parameterNumber: 0xC9, size: 2, scaledConfigurationValue: tempoff),
        // configure humidity offset
		zwave.configurationV1.configurationSet(parameterNumber: 0xCA, size: 2, scaledConfigurationValue: humidityoff),
        // configure luminance offset
		zwave.configurationV1.configurationSet(parameterNumber: 0xCB, size: 2, scaledConfigurationValue: luminanceoff),
        // configure ultraviolet offset
		zwave.configurationV1.configurationSet(parameterNumber: 0xCC, size: 2, scaledConfigurationValue: ultravioletoff), 

		zwave.batteryV1.batteryGet(),
		zwave.sensorBinaryV2.sensorBinaryGet(),
        
		// Can use the zwaveHubNodeId variable to add the hub to the device's associations:
		zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId)
    ]
	
    setConfigured()
    //log.debug request
    secureSequence(request) + ["delay 20000", zwave.wakeUpV1.wakeUpNoMoreInformation().format()]
}

def configure() {
    //["delay 30000"] + secure(zwave.securityV1.securityCommandsSupportedGet())
    log.debug "configure()"
   	def reportIntervalSecs = 4;
    	if (reportInterval) {
		reportIntervalSecs = reportInterval.toInteger()
		}
	delayBetween([
		// send temperature, humidity, illuminance, ultraviolet and battery every 8 minutes or as defined by preference
		zwave.configurationV1.configurationSet(parameterNumber: 0x65, size: 4, scaledConfigurationValue: 128|64|32|16|1).format(),
		zwave.configurationV1.configurationSet(parameterNumber: 0x6F, size: 4, scaledConfigurationValue: reportIntervalSecs).format(),

		// send battery every 20 hours
		zwave.configurationV1.configurationSet(parameterNumber: 0x66, size: 4, scaledConfigurationValue: 1).format(),
		zwave.configurationV1.configurationSet(parameterNumber: 0x70, size: 4, scaledConfigurationValue: 20*60*60).format(),

        // send no-motion report 60 seconds after motion stops
		zwave.configurationV1.configurationSet(parameterNumber: 0x03, size: 2, scaledConfigurationValue: 60).format(),

		// enable motion sensor
        zwave.configurationV1.configurationSet(parameterNumber: 0x04, size: 1, scaledConfigurationValue: 0).format(),

		// send binary sensor report instead of basic set for motion
		zwave.configurationV1.configurationSet(parameterNumber: 0x05, size: 1, scaledConfigurationValue: 2).format(),

		// configure PIR sensitivity for multisensor 6 min to max 0-127 default 64
        zwave.configurationV1.configurationSet(parameterNumber: 0x06, size: 1, scaledConfigurationValue: 64).format(),

		// Enable the function of vibration sensor
        zwave.configurationV1.configurationSet(parameterNumber: 0x07, size: 1, scaledConfigurationValue: 1).format(),

		// disable notification-style motion events
		zwave.notificationV3.notificationSet(notificationType: 7, notificationStatus: 0).format(),
        
        // configure temp offset
        zwave.configurationV1.configurationSet(parameterNumber: 0xC9, size: 2, scaledConfigurationValue: 20).format(),
        
        // configure humidity offset
        zwave.configurationV1.configurationSet(parameterNumber: 0xCA, size: 2, scaledConfigurationValue: 20).format(),
        
        // configure luminance offset
        zwave.configurationV1.configurationSet(parameterNumber: 0xCB, size: 2, scaledConfigurationValue: 0).format(),
        
        // configure ultraviolet offset
        zwave.configurationV1.configurationSet(parameterNumber: 0xCC, size: 2, scaledConfigurationValue: 5).format(),
        
		zwave.batteryV1.batteryGet(),
		zwave.sensorBinaryV2.sensorBinaryGet(),
		
        // Can use the zwaveHubNodeId variable to add the hub to the device's associations:
		zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId)
    ])	
    setConfigured()
	zwave.wakeUpV1.wakeUpNoMoreInformation().format()
}

def setConfigured() {
	device.updateDataValue("configured", "true")
}

def isConfigured() {
	Boolean configured = device.getDataValue(["configured"]) as Boolean
	
	return configured
}

private secure(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private secureSequence(commands, delay=200) {
	delayBetween(commands.collect{ secure(it) }, delay)
}

def updated() {
	log.debug "updated()"
    log.debug "PIRsensitivity: $PIRsensitivity, Reporting Interval: $ReportingInterval, Temp offset: $tempoffset, Humidity offset: $humidityoffset, Luminance offset: $luminanceoffset, UV offset: $ultravioletoffset"
    configureAfterSecure()
}
