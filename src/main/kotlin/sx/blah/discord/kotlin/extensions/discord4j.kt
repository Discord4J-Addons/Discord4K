package sx.blah.discord.kotlin.extensions

import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.Event
import sx.blah.discord.api.events.IListener

/**
 * This registers a listener for the provided event type.
 * 
 * @param[callback] The function to be executed when the event occurs.
 * 
 * @property[E] The event type.
 */
fun <E : Event> IDiscordClient.on(callback: (event: E) -> Unit) {
    this.dispatcher.registerListener(IListener<E> { event -> callback(event) })
}
