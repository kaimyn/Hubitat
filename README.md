# Tailwind_Hubitat
Hubitat device drivers for Tailwind support

Tailwind has released beta support for a local api. You can get it activated on your account by reaching out to them.  This is a beta release and there is NO security or authorization on the api, which means that anyone with access to your network can open your garage door.  I am also not a developer! This is my first driver so please use at your own risk, I don't recommend it.


Installation instructions:
1. Create a new driver for the controller using tailwindGaragedoorController.groovy
2. Create a new driver for the child device using tailwindGaragedoorChildDevice.groovy
3. Create a virtual device on Hubitat with Tailwind Garagedoor Controller as the device type
4. Set the IP and enable the doors that are physically wired to your controller

In order to use the status of the doors as a trigger for anything, you'll need to set up polling. I used Rule Machine to do it on my Hubitat.
