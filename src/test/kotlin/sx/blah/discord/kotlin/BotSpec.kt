package sx.blah.discord.kotlin

import org.jetbrains.spek.api.Spek
import sx.blah.discord.handle.impl.events.ReadyEvent
import java.io.File

val TOKEN: String
    get() {
        val credentials = File("./credentials.txt")
        if (credentials.exists())
            return credentials.readLines()[0]
        else
            return ""  
    }  

class BotSpec : Spek({
    describe("a simple bot", {
        bot {
            on<ReadyEvent> { println("Logged in as ${ourUser!!.name}") }
            given("a token") {
                token(TOKEN)
                it("should login") {
                    login()
                    dispatcher!!.waitFor<ReadyEvent> { true }
                }
            }
        }
    })
})


