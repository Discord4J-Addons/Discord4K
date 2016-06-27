package sx.blah.discord.kotlin

import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.modules.IModule

/**
 * This represents the base Discord4K module. This does nothing, it's only for show.
 */
class Discord4K : IModule {
    
    override fun getName() = "Discord4k"

    override fun enable(client: IDiscordClient?) = true

    override fun getVersion() = "1.0.0-SNAPSHOT"

    override fun getMinimumDiscord4JVersion() = "2.5.0-SNAPSHOT"

    override fun getAuthor() = "austinv11"

    override fun disable() {
        throw UnsupportedOperationException()
    }
}
