package sx.blah.discord.kotlin

import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.EventDispatcher
import sx.blah.discord.handle.obj.*
import sx.blah.discord.modules.ModuleLoader
import sx.blah.discord.util.Image
import java.time.LocalDateTime
import java.util.*

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
    var client: IDiscordClient? = null
    /**
     * The [DelegationEnvironment] instances which have registered for callbacks when the [client] instance is created.
     */
    private var toPropagate = mutableListOf<DelegationEnvironment<*>>()
    /**
     * The [ClientBuilder] instance.
     */
    private val builder = ClientBuilder()
    /**
     * The token used.
     */
    private var _token: String? = null
    /**
     * The email used.
      */
    private var _email: String? = null
    /**
     * The password used.
     */
    private var _password: String? = null

    /**
     * This sets the bot token for the client.
     * 
     * @param[token] The token for this bot to use.
     */
    fun token(token: String) {
        _token = token
    }

    /**
     * This sets the email for the client.
     * 
     * @param[email] The email for this bot to use.
     */
    fun email(email: String) {
        _email = email;
    }

    /**
     * This sets the password for the client.
     * 
     * @param[password] The password for this bot to use.
     */
    fun password(password: String) {
        _password = password
    }

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
            it.client = client
            it.onClientInit() 
        }
    }

    /**
     * This runs a facade or original client method if possible dynamically at runtime.
     */
    private fun <OUT> delegate(closure: DelegationEnvironment<OUT>.() -> Unit) : OUT? {
        val environment = DelegationEnvironment<OUT>(this, client)
        closure(environment)
        return environment.returnVal
    }
    
    override fun isReady(): Boolean = delegate {
        client { client!!.isReady }
        facade { client != null }
    }!!
    

    override fun login(): Unit = delegate { 
        client { client!!.login() }
        facade { 
            if (_token != null) {
                builder.withToken(_token)
            } else {
                builder.withLogin(_email, _password)
            }
            client = builder.login() 
            propagateClient()
        }
    }!!

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

    override fun changeEmail(email: String?): Unit = delegate {
        clientOnly = true
        client { client!!.changeEmail(email) }
    }!!

    override fun getToken(): String? = delegate {
        client { client!!.token }
        facade { token }
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

    override fun logout(): Unit = delegate { 
        clientOnly = true
        client { client!!.logout() }
    }!!

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

    override fun changeStatus(status: Status?): Unit = delegate {
        clientOnly = true
        client { client!!.changeStatus(status) }
    }!!

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

    override fun updatePresence(isIdle: Boolean, game: Optional<String>?): Unit = delegate { 
        clientOnly = true
        client { client!!.updatePresence(isIdle, game) }
    }!!

    override fun getGuilds(): MutableList<IGuild>? = delegate { 
        client { client!!.guilds }
        facade { mutableListOf() }
    }

    override fun changeGameStatus(game: String?): Unit = delegate { 
        clientOnly = true
        client { client!!.changeGameStatus(game) }
    }!!

    override fun changeAvatar(avatar: Image?): Unit = delegate { 
        clientOnly = true
        client { client!!.changeAvatar(avatar) }
    }!!

    override fun changePassword(password: String?): Unit = delegate { 
        clientOnly = true
        client { client!!.changePassword(password) }
    }!!

    override fun getOurUser(): IUser? = delegate { 
        client { client!!.ourUser }
        facade { null }
    }

    override fun changeUsername(username: String?): Unit = delegate { 
        clientOnly = true
        client { client!!.changeUsername(username) }
    }!!

    override fun changePresence(isIdle: Boolean): Unit = delegate { 
        clientOnly = true
        client { client!!.changePresence(isIdle) }
    }!!

    /**
     * This class is used to create an easy flow for creating facade implementations of [IDiscordClient] methods.
     * 
     * @property[source] The facade instance this is associated with.
     * @property[client] The client instance the facade is associated with.
     */
    private class DelegationEnvironment<OUT>(val source: ClientFacade, var client: IDiscordClient?) {
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
         * This registers a function as the "client-delegated" funciton.
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