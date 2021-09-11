/**
 *  Tailwind Garagedoor Controller
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
 *  your network can open your garage door.  
 *
 *  Thanks to derek.badge (https://github.com/Gelix/HubitatTailwind) for the ideas I borrowed
 *
 *  Changes:
 *  9/11/2021: Added polling, refactored to move all logic to the controller
 *
**/


metadata {
    definition (name: "Tailwind Garagedoor Controller", namespace: "kaimyn", author: "Chaue Shen") {
        capability "Polling"
        capability "Refresh"

        command "closeDoor", ["int"]
        command "openDoor", ["int"]
        attribute "Status", "int"
        attribute "Door 1 Status", "string"
        attribute "Door 2 Status", "string"
        attribute "Door 3 Status", "string"
    }
    
    preferences {
        input name: "ip", type: "string", title: "IP Address", required: true
        input name: "door1", type: "bool", title: "Enable Door 1", defaultValue: false
        input name: "door2", type: "bool", title: "Enable Door 2", defaultValue: false
        input name: "door3", type: "bool", title: "Enable Door 3", defaultValue: false
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
        input name: "txtEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: true
        input name: "interval", type: "integer", title: "Polling interval (seconds)", defaultValue: 2, description: "Recommended to set value between 2 and 60"
        input name: "pollEnable", type: "bool", title: "Enable status polling", defaultValue: false
    }
} 

def installed() {
    unschedule()
}

void logsOff() {
    log.info "debug logging disabled..."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
    getChildDevices.each { it.setLogs(logEnable, txtEnable) }
}

void updated() {
    addChildren()
    refresh()
    unschedule()
    initialize()
    if (logEnable) runIn(1800,logsOff)
    
}

void initialize() {
    if(pollEnable) {
        if(logEnable) log.debug "Enabling polling schedule every: ${interval} seconds"
        
        try {
            int setting = interval as int
            if(setting < 1) {
                device.updateSetting("interval", [value:2, type: "integer"])
                setting = 2
            }
            schedule("0/${setting} * * ? * * *", poll)
        } catch (Exception e) {
            log.error "Error = ${e}"
            device.updateSetting("pollEnable", [value:false, type: "bool"])
            device.updateSetting("interval", [value:"2", type: "integer"])
        }
    }
}

void addChildren() {
    def currentchild = getChildDevice("1")
    
    if(door1 && currentchild==null) {
        currentchild = addChildDevice("Tailwind Garagedoor", "1", [isComponent: true, name: "Garage Door 1", label: "Garage Door 1"])
    } else if (!door1 && currentchild!=null) {
        deleteChildDevice("1")
    }
    
    currentchild = getChildDevice("2")
    if(door2 && currentchild==null) {
        addChildDevice("Tailwind Garagedoor", "2", [isComponent: true, name: "Garage Door 2", label: "Garage Door 2"])
    } else if (!door2 && currentchild != null) {
        deleteChildDevice("2")
    }
    
    currentchild = getChildDevice("4")
    if(door3 && currentchild == null) {
        addChildDevice("Tailwind Garagedoor", "4", [isComponent: true, name: "Garage Door 3", label: "Garage Door 3"])
    } else if (!door3 && currentchild != null) {
        deleteChildDevice("4")
    }
    
    if(logEnable) getChildDevices().each { log.debug "Child device ${it.name} available" }
}


void refresh() {
    //works like poll(), but forces a send event to update status even if status has not changed
    //used in a timeout scenario and when enabling garage doors on the controller
    try {
    
        Map params = [
            uri: "http://"+ip,
            path: "/status",
        ]
        asynchttpGet("parseStatusResponse", params, [force: true])
        
    } catch(Exception e) {
        log.error "Error = ${e}"
    }
}


void poll() {
    
    try {
        Map params = [
            uri: "http://"+ip,
            path: "/status",
        ]
        asynchttpGet("parseStatusResponse", params, [force: false])
    
    } catch(Exception e) {
        log.error "Error = ${e}"
    }
}

void parseStatusResponse(resp, data) {
    if(logEnable) {
        log.debug "Method called: parseStatusResponse"
        log.debug "Polled Tailwind and got response: " + resp.getData()
        log.debug "Last status was: " + device.currentValue("Status")
    }
    
    boolean force = data.get("force")
    
    int response = resp.getData() as int
    
    def statusCodes=[
        ["closed", "closed", "closed"],       //0
        ["open", "closed", "closed"],         //1
        ["closed", "open", "closed"],         //2
        ["open", "open", "closed"],           //3
        ["closed", "closed", "open"],         //4
        ["open", "closed", "open"],           //5
        ["closed", "open", "open"],           //6
        ["open", "open", "open"]              //7
    ] 
    
    def child1 = getChildDevice("1")
    def child2 = getChildDevice("2")
    def child3 = getChildDevice("4")
    
    if(device.currentValue("Status").equals(response as String ) && !force) {
        if(logEnable) log.debug "No change"
    } else {
        sendEvent(name: "Status", value: response, displayed: false)
        if(logEnable) log.debug "Updated status to " + response
        
        if(child1!=null) { 
            child1.sendEvent(name: "door", value: statusCodes[response][0], descriptionText: "Door 1 is ${statusCodes[response][0]}")
        }
        if(child2!=null) {
            child2.sendEvent(name: "door", value: statusCodes[response][1], descriptionText: "Door 2 is ${statusCodes[response][1]}")
        }
        if(child3!=null) {
            child3.sendEvent(name: "door", value: statusCodes[response][2], descriptionText: "Door 3 is ${statusCodes[response][2]}")
        }
        
        sendEvent(name: "Door 1 Status", value: statusCodes[response][0], descriptionText: "Door 1 is ${statusCodes[response][0]}")
        sendEvent(name: "Door 2 Status", value: statusCodes[response][1], descriptionText: "Door 2 is ${statusCodes[response][1]}")
        sendEvent(name: "Door 3 Status", value: statusCodes[response][2], descriptionText: "Door 3 is ${statusCodes[response][2]}")
        
        if(txtEnable) {
            log.info "Door 1 is ${statusCodes[response][0]}"
            log.info "Door 2 is ${statusCodes[response][1]}"
            log.info "Door 3 is ${statusCodes[response][2]}"
        }
    }
}

void openDoor(doorID) {
    try {
        if(doorID != "1" && doorID != "2" && doorID != "3") throw new Exception("${doorID} is invalid. Try opening door 1, 2, or 3.")
        if(doorID == "3") doorID = 4
        openDoorInternal(doorID)        
    } catch (Exception e) {
        log.error "Error = ${e}"
    }
}

void openDoorInternal(doorID) {
    
    if(logEnable) log.debug "Method openDoor called with ID ${doorID}"

    try {
        
        Map params = [
            uri: "http://"+ip,
            path: "/cmd",
            contentType: "application/x-www-form-urlencoded",
            body: doorID
        ]
        
        if(logEnable) log.debug "Sending open request to ${params.uri} for door ${params.body}"
        
        asynchttpPost("parseCmdResponse", params, [doorID: doorID])
        
    } catch (Exception e) {
        log.error "Error = ${e}"
    }
}

void closeDoor(doorID) {
    try {
        if(doorID != "1" && doorID != "2" && doorID != "3") throw new Exception("${doorID} is invalid. Try opening door 1, 2, or 3.")
        if(doorID == "3") doorID = 4
        closeDoorInternal(doorID)        
    } catch (Exception e) {
        log.error "Error = ${e}"
    }
}      
    
void closeDoorInternal(doorID) {
    
    if(logEnable) log.debug "Method closeDoor called with ID ${doorID}"
    
    try {
    
        Map params = [
            uri: "http://"+ip,
            path: "/cmd",
            contentType: "application/x-www-form-urlencoded",
            body: "-" + doorID
        ]
  
        if(logEnable) log.debug "Sending close request to ${params.uri} for door ${params.body}"
        
        asynchttpPost("parseCmdResponse", params, [doorID: doorID])
        
    } catch (Exception e) {
        log.error "Error = ${e}"
    }
}


void parseCmdResponse(resp, data) {
    
    statusCode = resp.getData() as int
    statusCode += 4
    
    if(logEnable) log.debug "Tailwind responded with ${resp.getData()} and was interpreted as ${statusCode}"
    
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
    
    sendEvent(name: statusCodes[statusCode][0], value: statusCodes[statusCode][1])
    door = getChildDevice(data.doorID as String)
    if(door != null) door.sendEvent(name: "door", value: statusCodes[statusCode][1], descriptionText: "${statusCodes[statusCode][0]} is ${statusCodes[statusCode][1]}")
    
    if(txtEnable) log.info "${statusCodes[statusCode][0]} is ${statusCodes[statusCode][1]}"
    
    
    //wait 25 seconds and force a refresh of the status in case the door failed to open or close
    //there must be a better way of doing this
    pauseExecution(25000)
    refresh()
}
