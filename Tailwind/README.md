# Tailwind_Hubitat
Hubitat device drivers for Tailwind support

THIS DRIVER IS BROKEN AS OF TAILWIND V9.95.  NO INFORMATION IS AVAILABLE ON ANY FIXES AT THIS TIME (4/25)




Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
for the specific language governing permissions and limitations under the License.

Tailwind has released beta support for a local api. You can get it activated on your account by reaching out to them.  This is a beta release and there is NO security or authorization on the api, which means that anyone with access to your network can open your garage door.  As a beta release, expect breaking changes to the api, which may result in the driver malfunctioning.


Installation instructions:
1. Create a new driver for the controller using tailwindGaragedoorController.groovy
2. Create a new driver for the child device using tailwindGaragedoorChildDevice.groovy
3. Create a virtual device on Hubitat with Tailwind Garagedoor Controller as the device type
4. Set the IP and enable the doors that are physically wired to your controller


