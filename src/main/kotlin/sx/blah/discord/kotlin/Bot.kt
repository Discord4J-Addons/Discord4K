package sx.blah.discord.kotlin

import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.EventDispatcher
import sx.blah.discord.handle.obj.*
import sx.blah.discord.modules.ModuleLoader
import sx.blah.discord.util.Image
import java.time.LocalDateTime
import java.util.*

/**
 * This allows for a quick and easy setup for creating a bot.
 * 
 * @param[closure] See [ClientFacade] for information regarding functions available.
 */
fun bot(closure: ClientFacade.() -> Unit) {
    closure(ClientFacade())
}

/**
 * This represents a future instance of [IDiscordClient] with additional extension methods for better flow within the
 * [bot] function.
 */
// To those of you reading the source, I know it's messy
class ClientFacade : IDiscordClient {

    /**
     * The real [IDiscordClient] instance (if it exists).
     */
    var client: Lazy<IDiscordClient> = lazy { builder.login() }
    /**
     * The [DelegationEnvironment] instances which have registered for callbacks when the [client] instance is created.
     */
    private var toPropagate = mutableListOf<DelegationEnvironment<*>>()
    /**
     * The [ClientBuilder] instance.
     */
    private val builder = ClientBuilder()
    /**
     * The email used.
      */
    @Deprecated("Use tokens instead.", replaceWith = ReplaceWith("token"), level = DeprecationLevel.WARNING)
    var email: String? = null
    /**
     * The password used.
     */
    @Deprecated("Use tokens instead.", replaceWith = ReplaceWith("token"), level = DeprecationLevel.WARNING)
    var password: String? = null

    /**
     * @see[ClientBuilder.withTimeout]
     */
    fun timeout(timeout: Long) {
        builder.withTimeout(timeout)
    }

    /**
     * @see[ClientBuilder.withPingTimeout]
     */
    fun pingTimeout(timeout: Int) {
        builder.withPingTimeout(timeout)
    }

    /**
     * Sets the client as a daemon.
     * @see[ClientBuilder.setDaemon]
     */
    fun asDaemon() {
        builder.setDaemon(true)
    }

    /**
     * @see[ClientBuilder.withReconnects]
     */
    fun withReconnects() {
        builder.withReconnects()
    }
    
    /**
     * Propagates the client change event to all registered [DelegationEnvironment]s.
     */
    private fun propagateClient() {
        toPropagate.forEach {
            it.client = client.value
            it.onClientInit() 
        }
    }

    /**
     * This runs a facade or original client method if possible dynamically at runtime.
     * **NOTE:** This was designed for internal use.
     *
     * @param[closure] The execution delegation init script.
     */
    fun <OUT> delegate(closure: DelegationEnvironment<OUT>.() -> Unit) : OUT? {
        val environment = DelegationEnvironment<OUT>(this, if (client.isInitialized()) client.value else null)
        closure(environment)
        return environment.returnVal
    }
    
    override fun isReady(): Boolean = delegate {
        client { client!!.isReady }
        facade { client != null }
    }!!
    

    override fun login(): Unit {
        val facadeClient = this
        delegate<Unit> {
            client { client!!.login() }
            facade {
                if (token != null) {
                    builder.withToken(token)
                } else {
                    builder.withLogin(email, password)
                }
                facadeClient.client.value //Initializes the client
                propagateClient()
            }
        }
    }

    override fun isBot(): Boolean = delegate {
        client { client!!.isBot }
        facade { true }
    }!!

    override fun getChannelByID(channelID: String?): IChannel? = delegate {
        client { client!!.getChannelByID(channelID) }
        facade { null }
    }

    override fun getApplicationIconURL(): String? = delegate {
        client { client!!.applicationIconURL }
        facade { null }
    }

    override fun changeEmail(email: String?): Unit {
        delegate<Unit> {
            clientOnly = true
            client { client!!.changeEmail(email) }
        }
    }

    /**
     * Gets the token used by this bot.
     */
    override fun getToken(): String? = delegate {
        client { client!!.token }
        facade { token }
    }

    /**
     * Sets the token used by this bot.
     * @param[newToken] The new token.
     */
    fun setToken(newToken: String?): Unit? = delegate { 
        facadeOnly = true
        facade { 
            token = newToken 
            return@facade null
        }
    }

    override fun getApplications(): MutableList<IApplication>? = delegate {
        client { client!!.applications }
        facade { mutableListOf() }
    }

    override fun createGuild(name: String?, region: IRegion?, icon: Optional<Image>?): IGuild? {
        if (icon!!.isPresent) {
            return createGuild(name, region, icon.get())
        } else {
            return createGuild(name, region)
        }
    }

    override fun createGuild(name: String?, region: IRegion?): IGuild? = delegate { 
        clientOnly = true
        client { client!!.createGuild(name, region) }
    }

    override fun createGuild(name: String?, region: IRegion?, icon: Image?): IGuild? = delegate { 
        clientOnly = true
        client { client!!.createGuild(name, region, icon) }
    }

    override fun getLaunchTime(): LocalDateTime? = delegate { 
        client { client!!.launchTime }
        facade { LocalDateTime.now() }
    }

    override fun getDescription(): String? = delegate { 
        client { client!!.description }
        facade { null }
    }

    override fun createApplication(name: String?): IApplication? = delegate { 
        clientOnly = true
        client { client!!.createApplication(name) }
    }

    override fun getDispatcher(): EventDispatcher? = delegate { 
        client { client!!.dispatcher }
        facade { null }
    }

    override fun logout(): Unit {
        delegate<Unit> {
            clientOnly = true
            client { client!!.logout() }
        }
    }

    override fun getModuleLoader(): ModuleLoader? = delegate { 
        client { client!!.moduleLoader }
        facade { null }
    }

    override fun getGuildByID(guildID: String?): IGuild? = delegate { 
        client { client!!.getGuildByID(guildID) }
        facade { null }
    }

    override fun getUserByID(userID: String?): IUser? = delegate { 
        client { client!!.getUserByID(userID) }
        facade { null }
    }

    override fun getRegions(): MutableList<IRegion>? = delegate { 
        client { client!!.regions }
        facade { mutableListOf() }
    }

    override fun getOrCreatePMChannel(user: IUser?): IPrivateChannel? = delegate { 
        client { client!!.getOrCreatePMChannel(user) }
        facade { null }
    }

    override fun getApplicationClientID(): String? = delegate { 
        client { client!!.applicationClientID }
        facade { null }
    }

    override fun getConnectedVoiceChannels(): MutableList<IVoiceChannel>? = delegate { 
        client { client!!.connectedVoiceChannels }
        facade { mutableListOf() }
    }

    override fun getVoiceChannels(): MutableCollection<IVoiceChannel>? = delegate { 
        client { client!!.voiceChannels }
        facade { mutableListOf() }
    }

    override fun getRegionByID(regionID: String?): IRegion? = delegate { 
        client { client!!.getRegionByID(regionID) }
        facade { null }
    }

    override fun getInviteForCode(code: String?): IInvite? = delegate { 
        client { client!!.getInviteForCode(code) }
        facade { null }
    }

    override fun getResponseTime(): Long = delegate { 
        client { client!!.responseTime }
        facade { 0 }
    }!!

    override fun changeStatus(status: Status?): Unit {
        delegate<Unit> {
            clientOnly = true
            client { client!!.changeStatus(status) }
        }
    }

    override fun getVoiceChannelByID(id: String?): IVoiceChannel? = delegate { 
        client { client!!.getVoiceChannelByID(id) }
        facade { null }
    }

    override fun getChannels(includePrivate: Boolean): MutableCollection<IChannel>? = delegate { 
        client { client!!.getChannels(includePrivate) }
        facade { mutableListOf() }
    }

    override fun getApplicationName(): String? = delegate { 
        client { client!!.applicationName }
        facade { null }
    }

    override fun updatePresence(isIdle: Boolean, game: Optional<String>?): Unit {
        delegate<Unit> {
            clientOnly = true
            client { client!!.updatePresence(isIdle, game) }
        }
    }

    override fun getGuilds(): MutableList<IGuild>? = delegate { 
        client { client!!.guilds }
        facade { mutableListOf() }
    }

    override fun changeGameStatus(game: String?): Unit {
        delegate<Unit> {
            clientOnly = true
            client { client!!.changeGameStatus(game) }
        }?: return
    }

    override fun changeAvatar(avatar: Image?): Unit {
        delegate<Unit> {
            clientOnly = true
            client { client!!.changeAvatar(avatar) }
        }
    }
    

    override fun changePassword(password: String?): Unit {
        delegate<Unit> {
            clientOnly = true
            client { client!!.changePassword(password) }
        }
    }

    override fun getOurUser(): IUser? = delegate { 
        client { client!!.ourUser }
        facade { null }
    }

    override fun changeUsername(username: String?): Unit {
        delegate<Unit> {
            clientOnly = true
            client { client!!.changeUsername(username) }
        }
    }

    override fun changePresence(isIdle: Boolean): Unit {
        delegate<Unit> {
            clientOnly = true
            client { client!!.changePresence(isIdle) }
        }
    }

    /**
     * This class is used to create an easy flow for creating facade implementations of [IDiscordClient] methods.
     * 
     * @property[source] The facade instance this is associated with.
     * @property[client] The client instance the facade is associated with.
     */
    class DelegationEnvironment<OUT>(val source: ClientFacade, var client: IDiscordClient?) {
        private var _returnVal: OUT? = null
        private var _facade : () -> OUT? = { null }
        private var _client : () -> OUT? = { null }
        /**
         * If true, only the function provided by [facade] will be used.
         */
        var facadeOnly = false
        /**
         * If true, only the function provided by [client] will be used, and if the client is not ready yet, it will
         * register for a callback when the client is ready.
         */
        var clientOnly = false
        /**
         * This lazily calculates the result of the function this environment represents.
         */
        var returnVal: OUT?
            set(value) {
                _returnVal = value
            }
            get() {
                if (facadeOnly) {
                   return _facade()
                } else if (clientOnly && client == null) {
                    source.toPropagate.add(this)
                    return null
                } else if (client == null) {
                    return _facade()
                }
                
                return _client()
            }

        /**
         * This registers a function as the "facade" function.
         */
        fun facade(function: () -> OUT?): Unit { 
            _facade = function 
        }

        /**
         * This registers a function as the "client-delegated" function.
         */
        fun client(function: () -> OUT?): Unit {
            _client = function
        }

        /**
         * This is called when this environment is called back when a new client instance is ready.
         * Note: This is only applicable if [clientOnly] is `true`.
         */
        fun onClientInit() {
            _client()
        }
    }
}
