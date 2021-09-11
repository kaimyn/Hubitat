/**
 *  Tailwind Garagedoor Child Device
 *
 *  Copyright 2021 Chaue Shen
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
 *  Tailwind has released beta support for a local api. You can get it activated on your account by reaching out to them.  
 *  This is a beta release and there is NO security or authorization on the api, which means that anyone with access to 
 *  your network can open your garage door.  I am also not an experience developer! This is my first ever driver so please 
 *  use at your own risk, I don't recommend it.
 *
**/



metadata {
    definition (name: "Tailwind Garagedoor", namespace: "Chaue", author: "Chaue Shen") {
        capability "GarageDoorControl"
        capability "DoorControl"
        capability "ContactSensor"
        
    }
} 

void setLogs(logEnable, txtEnable) {
    updateDataValue("logEnable", logEnable as String)
    updateDataValue("txtEnable", txtEnable as String)
    
    if(logEnable) {
        log.info "logEnable " + logEnable
        log.info "txtEnable " + txtEnable
    }
}


void setIP(address) {
    
    updateDataValue("ip", address)
    
    if(getDataValue("logEnable") as boolean) log.info "Setting IP: " + getDataValue("ip")
}

void setDoorID(id) {
    updateDataValue("doorID", id) 
}

void updateStatus(status) {
    //update the status of the door
    //this event will fire even if the status has not changed either in the event of a refresh or if a different door has changed state
    //I'm not sure if sending events in this case has other implications across the system
    
    sendEvent(name: "door", value: status, descriptionText: "Door ${device.deviceNetworkId} is ${status}")
    sendEvent(name: "contact", value: status)
    
    if(getDataValue("txtEnable") as boolean) log.info "Door ${device.deviceNetworkId} is ${status}"
}

void close() {
    
    try {
    
        Map params = [
            uri: "http://"+getDataValue("ip"),
            path: "/cmd",
            contentType: "application/x-www-form-urlencoded",
            body: "-" + getDataValue("doorID")
        ]
  
        if(getDataValue("logEnable") as boolean) log.info "Sending close request to ${params.uri} for door ${params.body}"
        
        asynchttpPost("parseCmdResponse", params)
        
    } catch (Exception e) {
        log.error "Error = ${e}"
    }
}

void open() {
    
    try {
    
        Map params = [
            uri: "http://"+getDataValue("ip"),
            path: "/cmd",
            contentType: "application/x-www-form-urlencoded",
            body: getDataValue("doorID")
        ]
        
        if(getDataValue("logEnable") as boolean) log.info "Sending open request to ${params.uri} for door ${params.body}"
        
        asynchttpPost("parseCmdResponse", params, cmd)
        
    } catch (Exception e) {
        log.error "Error = ${e}"
    }
}

void parseCmdResponse(resp, data) {
    
    statusCode = resp.getData() as int
    statusCode += 4
    
    if(getDataValue("logEnable") as boolean) log.info "Tailwind responded with ${resp.getData()} and was interpreted as ${statusCode}"
    
    //thanks to derek.badge (https://github.com/Gelix/HubitatTailwind) for the idea
    def statusCodes = [
        ["Door 3 Status", "closing"],    //-4 + 4 -> 0
        ["Unused", "unused"],            //No mapping -> 1
        ["Door 2 Status", "closing"],    //-2 + 4 -> 2
        ["Door 1 Status", "closing"],    //-1 + 4 -> 3
        ["Unused", "unused"],            //No mapping -> 4
        ["Door 1 Status", "opening"],    //1 + 4 -> 5
        ["Door 2 Status", "opening"],    //2 + 4 -> 6
        ["Unused", "unused"],            //No mapping -> 7
        ["Door 3 Status", "opening"]    //4 + 4 -> 8
    ]
    
    getParent().sendEvent(name: statusCodes[statusCode][0], value: statusCodes[statusCode][1])
    sendEvent(name: "door", value: statusCodes[statusCode][1], descriptionText: "Door ${statusCodes[statusCode][0]} is ${statusCodes[statusCode][1]}")
    
    if(getDataValue("txtEnable") as boolean) log.info "Door ${statusCodes[statusCode][0]} is ${statusCodes[statusCode][1]}"
    
    
    //wait 15 seconds and force a refresh of the status in case the door failed to open or close
    //there must be a better way of doing this
    pauseExecution(15000)
    getParent().refresh()
}
