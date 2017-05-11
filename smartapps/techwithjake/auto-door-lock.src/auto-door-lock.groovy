definition(
    name: "Auto Lock Door",
    namespace: "techwithjake",
    author: "Tech With Jake",
    description: "Automatically locks a specific door after X minutes when closed.",
    category: "My Apps",
    iconUrl: "http://www.gharexpert.com/mid/4142010105208.jpg",
    iconX2Url: "http://www.gharexpert.com/mid/4142010105208.jpg"
)

preferences{
    section("Select the door lock:") {
        input "lock1", "capability.lock", required: true
    }
    section("Automatically lock the door when unlocked...") {
        input "minutesLater1", "number", title: "Delay (in minutes):", required: true
    }
    section("Select the door contact sensor:") {
        input "contact", "capability.contactSensor", required: false
    }
    section("Automatically lock the door when closed...") {
        input "minutesLater2", "number", title: "Delay (in minutes) > 0:", required: false
    }
    section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to", required: false) {
            input "phoneNumber", "phone", title: "Warn with text message (optional)", description: "Phone Number", required: false
            input "pushNotification", "bool", title: "Push notification", required: false, defaultValue: "false"
        }
    }
}

def installed(){
    initialize()
}

def updated(){
    unsubscribe()
    unschedule()
    initialize()
}

def initialize(){
    log.debug "Settings: ${settings}"
    subscribe(lock1, "lock", doorHandler, [filterEvents: false])
    subscribe(contact, "contact.closed", doorHandler)
}

def lockDoor(){
    log.debug "Locking the door."
    lock1.lock()
    if(location.contactBookEnabled) {
        if ( recipients ) {
            log.debug ( "Sending Push Notification..." )
            sendNotificationToContacts( "${lock1} LOCKED after ${contact} was closed for ${minutesLater2} minutes or it was unlocked for ${minutesLater1} minutes!", recipients)
        }
    }
    if (phoneNumber) {
        log.debug("Sending text message...")
        sendSms( phoneNumber, "${lock1} LOCKED after ${contact} was closed for ${minutesLater2} minutes or it was unlocked for ${minutesLater1} minutes!")
    }
    if (pushNotification) {
        log.debug("Sending push notification...")
        sendPush("${lock1} LOCKED after ${contact} was closed for ${minutesLater2} minutes or it was unlocked for ${minutesLater1} minutes!")
      }
}

def doorHandler(evt){
    if ((lock1.latestValue("lock") == "unlocked") && (evt.value == "unlocked")) { // If a person unlocks a locked door...
      //def delay = (minutesLater * 60) // runIn uses seconds
        runIn( (minutesLater1 * 60), lockDoor ) // ...schedule (in minutes) to lock.
    }
    else if ((lock1.latestValue("lock") == "locked") && (evt.value == "locked")) { // If a person manually locks it then...
        unschedule( lockDoor ) // ...we don't need to lock it later.
    }
    if ((contact.latestValue("contact") == "closed") && (evt.value == "locked")) { // If the door is closed and a person manually locks it then...
        unschedule( lockDoor ) // ...we don't need to lock it later.
    }
    else if ((contact.latestValue("contact") == "closed") && (evt.value == "unlocked")) { // If the door is closed and a person unlocks it then...
       //def delay = (minutesLater * 60) // runIn uses seconds
        runIn( (minutesLater2 * 60), lockDoor ) // ...schedule (in minutes) to lock.
    }
    else if ((lock1.latestValue("lock") == "unlocked") && (evt.value == "closed")) { // If a person closes an unlocked door...
        //def delay = (minutesLater * 60) // runIn uses seconds
        runIn( (minutesLater2 * 60), lockDoor ) // ...schedule (in minutes) to lock.
    }
}
