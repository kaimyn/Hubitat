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
 *  Changes:
 *  9/11/2021: Added polling, refactored to move all logic to the controller
 *
**/



metadata {
    definition (name: "Tailwind Garagedoor", namespace: "kaimyn", author: "Chaue Shen") {
        capability "GarageDoorControl"
        capability "DoorControl"
        
    }
} 

void setDoorID(doorID) {
    updateDataValue("doorID", doorID as String)
}

void close() {
    getParent().closeDoor(getDataValue("doorID"))
}

void open() {
    getParent().openDoor(getDataValue("doorID"))
}
