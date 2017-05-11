definition(
    name: "Enhanced Auto Lock Door",
    namespace: "Enhanced Auto Lock Door",
    author: "Arnaud w/ edits from Tech With Jake",
    description: "Automatically locks a specific door after X minutes when closed or unlocked for X minutes and unlocks it when open after X seconds.",
    category: "Safety & Security",
    iconUrl: "http://www.gharexpert.com/mid/4142010105208.jpg",
    iconX2Url: "http://www.gharexpert.com/mid/4142010105208.jpg"
)

preferences{
    section("Select the door lock:") {
        input "lock1", "capability.lock", required: true
    }
    section("Automatically lock the door when unlocked...") {
        input "minutesLater1", "number", title: "Delay (in minutes):", required: false
    }
    section("Select the door contact sensor:") {
        input "contact", "capability.contactSensor", required: false
    }
    section("Automatically lock the door when closed...") {
        input "minutesLater2", "number", title: "Delay (in minutes):", required: false
    }
    section("Automatically unlock the door when open...") {
        input "secondsLater", "number", title: "Delay (in seconds):", required: false
    }
    section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to", required: false) {
            input "phoneNumber", "phone", title: "Warn with text message (optional)", description: "Phone Number", required: false
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
    subscribe(lock1, "unlock", doorHandler, [filterEvents: false])
    subscribe(contact, "contact.open", doorHandler)
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
}

def unlockDoor(){
    log.debug "Unlocking the door."
    lock1.unlock()
    if(location.contactBookEnabled) {
        if ( recipients ) {
            log.debug ( "Sending Push Notification..." )
            sendNotificationToContacts( "${lock1} UNLOCKED after ${contact} was opened for ${secondsLater} seconds!", recipients)
        }
    }
    if ( phoneNumber ) {
        log.debug("Sending text message...")
        sendSms( phoneNumber, "${lock1} UNLOCKED after ${contact} was opened for ${secondsLater} seconds!")
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
    if ((contact.latestValue("contact") == "open") && (evt.value == "locked")) { // If the door is open and a person locks the door then...
        //def delay = (secondsLater) // runIn uses seconds
        runIn( secondsLater, unlockDoor )   // ...schedule (in minutes) to unlock...  We don't want the door to be closed while the lock is engaged.
    }
    else if ((contact.latestValue("contact") == "open") && (evt.value == "unlocked")) { // If the door is open and a person unlocks it then...
        unschedule( unlockDoor ) // ...we don't need to unlock it later.
    }
    else if ((contact.latestValue("contact") == "closed") && (evt.value == "locked")) { // If the door is closed and a person manually locks it then...
        unschedule( lockDoor ) // ...we don't need to lock it later.
    }
    else if ((contact.latestValue("contact") == "closed") && (evt.value == "unlocked")) { // If the door is closed and a person unlocks it then...
       //def delay = (minutesLater * 60) // runIn uses seconds
        runIn( (minutesLater2 * 60), lockDoor ) // ...schedule (in minutes) to lock.
    }
    else if ((lock1.latestValue("lock") == "unlocked") && (evt.value == "open")) { // If a person opens an unlocked door...
        unschedule( lockDoor ) // ...we don't need to lock it later.
    }
    else if ((lock1.latestValue("lock") == "unlocked") && (evt.value == "closed")) { // If a person closes an unlocked door...
        //def delay = (minutesLater * 60) // runIn uses seconds
        runIn( (minutesLater2 * 60), lockDoor ) // ...schedule (in minutes) to lock.
    }

}
