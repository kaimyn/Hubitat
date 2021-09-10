
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
 *  your network can open your garage door.  I am also not an experience developer! This is my first ever driver so please 
 *  use at your own risk, I don't recommend it.
 *
**/


metadata {
    definition (name: "Tailwind Garagedoor Controller", namespace: "kaimyn", author: "Chaue Shen") {
        capability "Polling"
        capability "Refresh"

        attribute "Status", "text"
        attribute "Door 1 Status", "text"
        attribute "Door 2 Status", "text"
        attribute "Door 3 Status", "text"
    }
    
    preferences {
        input name: "ip", type: "text", title: "IP Address", required: true
        input name: "door1", type: "bool", title: "Enable Door 1"
        input name: "door2", type: "bool", title: "Enable Door 2"
        input name: "door3", type: "bool", title: "Enable Door 3"
    }
} 


void updated() {
    //log.info "Update"
    addChildren()
    updateChildren()
    refresh()
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
    
    getChildDevices().each { log.info "Child device: " + it.name }
}

void updateChildren() {
    getChildDevices().each { 
        it.setIP(ip)
        it.setDoorID(it.deviceNetworkId)
    }
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
    //log.info "Polled Tailwind and got response: " + resp.getData()
    //log.info "Last status was: " + device.currentValue("Status")
    
    boolean force = data.get("force")
    
    def child1 = getChildDevice("1")
    def child2 = getChildDevice("2")
    def child3 = getChildDevice("4")
    
    def child1Status
    def child2Status
    def child3Status
    
    if(device.currentValue("Status").equals(resp.getData()) && !force) {
        log.info "No change"
    } else {
        sendEvent(name: "Status", value: resp.getData(), displayed: false)
        log.info "Updated status to " + resp.getData()
        int response = resp.getData() as int

        //bitwise comparison where 0 means closed
        //feels cleaner than the switch used in the child devices but no idea if it's better
        door1Status = response&1
        door2Status = response&2
        door3Status = response&4
        
        child1Status = "closed"
        child2Status = "closed"
        child3Status = "closed"
        
        if(door1Status>0) child1Status = "open"
        if(door2Status>0) child2Status = "open"
        if(door3Status>0) child3Status = "open"

        if(child1!=null) { 
            child1.updateStatus(child1Status)
        }
        if(child2!=null) {
            child2.updateStatus(child2Status)
        }
        if(child3!=null) {
            child3.updateStatus(child3Status)
        }
        
        sendEvent(name: "Door 1 Status", value: child1Status, displayed: true)
        sendEvent(name: "Door 2 Status", value: child2Status, displayed: true)
        sendEvent(name: "Door 3 Status", value: child3Status, displayed: true)
    }
}
