package sx.blah.discord.kotlin.extensions

import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.Event
import sx.blah.discord.api.events.EventDispatcher
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.obj.*
import sx.blah.discord.handle.obj.*
import sx.blah.discord.kotlin.ClientFacade
import sx.blah.discord.util.RequestBuffer

/**
 * This wraps a all requests to use the [RequestBuffer].
 * 
 * @param[closure] The requests to execute. This **CANNOT** catch [sx.blah.discord.util.RateLimitException].
 * 
 * @see[RequestBuffer.request]
 */
fun buffer(closure: () -> Unit) {
    RequestBuffer.request { closure() }
}

/**
 * This wraps a all requests to use the [RequestBuffer].
 *
 * @param[closure] The requests to execute. This **CANNOT** catch [sx.blah.discord.util.RateLimitException].
 * @return The future representing the result of the requests on success.
 *
 * @see[RequestBuffer.request]
 */
fun <T> buffer(closure: () -> T): RequestBuffer.RequestFuture<T> {
    return RequestBuffer.request(RequestBuffer.IRequest<T> { closure() })
}

/**
 * This attempts to locate the specified type of [IDiscordObject] by its id.
 * 
 * @param[id] The id for the object.
 * @return The object, or null if not found.
 */
inline fun <reified OBJECT : IDiscordObject<OBJECT>> IDiscordClient.find(id: String): OBJECT? {
    when (OBJECT::class) {
        IMessage::class, Message::class -> {
            guilds.forEach { 
                it.channels.forEach {
                    val message = it.getMessageByID(id)
                    if (message != null) 
                        return@find it.getMessageByID(id) as? OBJECT 
                } 
            }
        }
        IUser::class, User::class -> {
            return getUserByID(id) as? OBJECT
        }
        IGuild::class, Guild::class -> {
            return getGuildByID(id) as? OBJECT
        }
        IVoiceChannel::class, VoiceChannel::class -> {
            return getVoiceChannelByID(id) as? OBJECT
        }
        IPrivateChannel::class, PrivateChannel::class -> {
            val user = getUserByID(id)
            return if (user != null && user != ourUser) getOrCreatePMChannel(user) as? OBJECT else null
        }
        IChannel::class, Channel::class -> {
            return getChannelByID(id) as? OBJECT
        }
        IRegion::class, Region::class -> {
            return getRegionByID(id) as? OBJECT
        }
        IRole::class, Role::class -> {
            guilds.forEach { 
                val role = it.getRoleByID(id)
                if (role != null)
                    return@find role as? OBJECT
            }
        }
        else -> throw RuntimeException("Cannot locate object of type ${OBJECT::class.simpleName}")
    }
    
    return null
}

/**
 * This registers a listener for the provided event type.
 * 
 * @param[callback] The function to be executed when the event occurs.
 * 
 * @property[E] The event type.
 */
fun <E : Event> IDiscordClient.on(callback: AnonymousListener<E>.(event: E) -> Unit) {
    if (this is ClientFacade) {
        this.delegate<Unit> { 
            clientOnly = true
            client { client!!.on(callback) }
        }
    } else {
        val listener = AnonymousListener(callback)
        listener.register(this.dispatcher)
    }
}

/**
 * This causes the current thread to wait until an event is received.
 */
fun <E : Event> IDiscordClient.waitFor() {
    this.waitFor<E> { true }
}

/**
 * This causes the current thread to wait until an event is received.
 *
 * @param[callback] This is called when the event is received. When it returns true, the current thread is wake.
 */
fun <E : Event> IDiscordClient.waitFor(callback: (event: E) -> Boolean) {
    this.waitFor(callback, 0)
}

/**
 * This causes the current thread to wait until an event is received.
 * 
 * @param[callback] This is called when the event is received. When it returns true, the current thread is wake.
 * @param[timeout] The timeout for the thread to wake if the event isn't received.
 */
fun <E : Event> IDiscordClient.waitFor(callback: (event: E) -> Boolean, timeout: Long) {
    if (this is ClientFacade) {
        this.delegate<Unit> {
            clientOnly = true
            client { client!!.waitFor(callback, timeout) }
        }
    } else {
        this.dispatcher.waitFor<E>({ callback(it) }, timeout)
    }
}

/**
 * This represents an anonymous [IListener] implementation environment for the callback.
 * 
 * @param[callback] The function to be executed when the event occurs.
 */
class AnonymousListener<E : Event>(val callback: AnonymousListener<E>.(event: E) -> Unit): IListener<E> {
    
    private var dispatcher: EventDispatcher? = null

    /**
     * Registers this listener to the provided [EventDispatcher].
     * 
     * @param[dispatcher] The dispatcher to register to.
     */
    fun register(dispatcher: EventDispatcher) {
        dispatcher.registerListener(this)
        this.dispatcher = dispatcher
    }

    /**
     * Unregisters this listener to the last used [EventDispatcher].
     */
    fun unregister() {
        unregister(dispatcher)
    }

    /**
     * Unregisters this listener from the provided [EventDispatcher].
     * 
     * @param[dispatcher] The dispatcher to unregister from.
     */
    fun unregister(dispatcher: EventDispatcher?) {
        dispatcher?.unregisterListener(this)
    }
    
    override fun handle(event: E) {
        callback(this, event)
    }
}
