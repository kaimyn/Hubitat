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
**/



metadata {
    definition (name: "Tailwind Garagedoor", namespace: "Chaue", author: "Chaue Shen") {
        capability "GarageDoorControl"
        capability "DoorControl"
        capability "ContactSensor"
        
    }

} 

void setIP(address) {
    updateDataValue("ip", address)
    log.info "Setting IP: " + getDataValue("ip")
}

void setDoorID(id) {
    updateDataValue("doorID", id) 
}

void updateStatus(status) {
    //update the status of the door
    //this event will fire even if the status has not changed either in the event of a refresh or if a different door has changed state
    //I'm not sure if sending events in this case has other implications across the system
    
    sendEvent(name: "door", value: status, displayed: true)
    sendEvent(name: "contact", value: status, displayed: true)
    log.info "Sent event for door: " + getDataValue("doorID") + ", " + status
}

void close() {
    
    try {
    
        Map params = [
            uri: "http://"+getDataValue("ip"),
            path: "/cmd",
            contentType: "application/x-www-form-urlencoded",
            body: "-" + getDataValue("doorID")
        ]
  
        asynchttpPost("parseCmdResponse", params, cmd)
        log.info "Closing IP: " + params.get("uri") + params.get("path")
        
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
        
        asynchttpPost("parseCmdResponse", params, cmd)
        log.info "Opening IP: " + params.get("uri") + params.get("path")
        
    } catch (Exception e) {
        log.error "Error = ${e}"
    }
}

void parseCmdResponse(resp, data) {
    switch(resp.getData() as int) {
        case 1:
            log.info "Garage door 1 is opening"
            getParent().sendEvent(name: "Door 1 Status", value: "opening", displayed: true)
            sendEvent(name: "door", value: "opening", displayed: true)
            break
        case 2:
            log.info "Garage door 2 is opening"
            getParent().sendEvent(name: "Door 2 Status", value: "opening", displayed: true)
            sendEvent(name: "door", value: "opening", displayed: true)        
            break
        case 4:
            log.info "Garage door 3 is opening"
            getParent().sendEvent(name: "Door 3 Status", value: "opening", displayed: true)
            sendEvent(name: "door", value: "opening", displayed: true)        
            break
        case -1:
            log.info "Garage door 1 is closing"
            getParent().sendEvent(name: "Door 1 Status", value: "closing", displayed: true)
            sendEvent(name: "door", value: "closing", displayed: true)        
            break
        case -2:
            log.info "Garage door 2 is closing"
            getParent().sendEvent(name: "Door 2 Status", value: "closing", displayed: true)
            sendEvent(name: "door", value: "closing", displayed: true)        
            break
        case -4:
            log.info "Garage door 3 is closing"
            getParent().sendEvent(name: "Door 3 Status", value: "closing", displayed: true)
            sendEvent(name: "door", value: "closing", displayed: true)        
            break
    }
    
    //wait 15 seconds and force a refresh of the status in case the door failed to open or close
    //there must be a better way of doing this
    pauseExecution(15000)
    getParent().refresh()
}
