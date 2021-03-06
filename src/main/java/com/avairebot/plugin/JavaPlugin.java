package com.avairebot.plugin;

import com.avairebot.AvaIre;
import com.avairebot.cache.CacheManager;
import com.avairebot.commands.CategoryHandler;
import com.avairebot.commands.CommandHandler;
import com.avairebot.config.Configuration;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.DatabaseManager;
import com.avairebot.database.migrate.Migrations;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public abstract class JavaPlugin {

    private final Set<ListenerAdapter> eventListeners = new HashSet<>();

    private Logger logger = LoggerFactory.getLogger(JavaPlugin.class);

    private AvaIre avaire;
    private PluginLoader loader;

    private Configuration config = null;

    /**
     * Initializes the plugin by setting the global avaire
     * application instance and preparing some data.
     *
     * @param avaire The global avaire application instance.
     */
    final void init(AvaIre avaire, PluginLoader loader) {
        this.avaire = avaire;
        this.loader = loader;
        this.logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    }

    /**
     * Gets a set of the event listeners that has been registered by the plugin.
     *
     * @return A set of registered event listeners.
     */
    final Set<ListenerAdapter> getEventListeners() {
        return eventListeners;
    }

    /**
     * Register the given command into the command handler, creating the
     * command container and saving it into the commands collection.
     *
     * @param command The command that should be registered into the command handler.
     */
    public final void registerCommand(Command command) {
        CommandHandler.register(command);
    }

    /**
     * Register the given category name if it doesn't already exists with the
     * given default prefix, once the category has been registered the
     * category will show up on the help command, and be available
     * to users for customization via the prefix command.
     *
     * @param name          The name of the category that should be registered, can not contain spaces or special characters.
     * @param defaultPrefix The default prefix for the category.
     * @return <code>True</code> on success, <code>False</code> if the category already exists.
     */
    public final boolean registerCategory(String name, String defaultPrefix) {
        return CategoryHandler.addCategory(avaire, name, defaultPrefix);
    }

    /**
     * Registers database migration to the migration containers, the migrations
     * will be used in {@link Migrations#up() up()}, {@link Migrations#down() down()} and {@link Migrations#rollback(int) rollback(int)}
     * <p>
     * All migrations must follow the {@link com.avairebot.contracts.database.migrations.Migration Migration contract}.
     *
     * @param migration The migration that should be registered
     * @see com.avairebot.contracts.database.migrations.Migration
     */
    public final void registerMigration(Migration migration) {
        avaire.getDatabase().getMigrations().register(migration);
    }

    /**
     * Registers a JDA event listener, the class must extend from the {@link ListenerAdapter Abstract Listener Adapter}.
     *
     * @param listener The event listener that should be registered.
     */
    public final void registerEventListener(ListenerAdapter listener) {
        eventListeners.add(listener);
    }

    /**
     * Returns the global AvaIre application instance.
     *
     * @return The global AvaIre application instance.
     */
    public final AvaIre getAvaire() {
        return avaire;
    }

    /**
     * Gets the shard manager used by the bot, giving you access to methods
     * that can interact with the application on a global scale.
     *
     * @return A list of bot shard entries.
     */
    public final ShardManager getShardManager() {
        return avaire.getShardManager();
    }

    /**
     * Gets the application cache manager, giving you
     * access to store things in files or in memory.
     *
     * @return The application cache manager.
     */
    public final CacheManager getCache() {
        return avaire.getCache();
    }

    /**
     * Gets the application database manager, giving you access to query the database, create migrations, and change the schema.
     *
     * @return The application database manager.
     */
    public final DatabaseManager getDatabase() {
        return avaire.getDatabase();
    }

    /**
     * Loads the default {@link Configuration config} into memory and prepares
     * it for the plugin, if the config has already been loaded the
     * {@link Configuration config} will just be returned.
     *
     * @return The {@link Configuration config} that.
     */
    public final Configuration getConfig() {
        if (config == null) {
            try {
                config = new Configuration(
                    this,
                    new File(
                        loader.getDataFolder(),
                        loader.getName()
                    ),
                    "config.yml"
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return config;
    }

    /**
     * Saves the {@link Configuration config} file from {@link #getConfig()}
     * to disk under the name "config.yml", if the config file already
     * exists this method will fail silently.
     */
    public final void saveDefaultConfig() {
        if (getConfig() != null) {
            getConfig().saveDefaultConfig();
        }
    }

    /**
     * Discards any data in {@link #getConfig()} and reloads from disk.
     */
    public final void reloadConfig() {
        if (getConfig() != null) {
            getConfig().reloadConfig();
        }
    }

    /**
     * Returns the plugin logger associated with the application logger. The returned
     * logger automatically tags all log messages with the plugin's name.
     *
     * @return Logger associated with this plugin.
     */
    public final Logger getLogger() {
        return logger;
    }

    /**
     * Gets the {@link PluginLoader plugin loader} used for loading this plugin, the
     * {@link PluginLoader plugin loader} can be used to access data from
     * the "plugin.yml" file for this plugin.
     *
     * @return The {@link PluginLoader plugin loader} used to load this plugin.
     */
    public final PluginLoader getPluginLoader() {
        return loader;
    }

    /**
     * Called when this plugin is enabled
     */
    public abstract void onEnable();

    /**
     * Called when this plugin is disabled
     */
    public void onDisable() {
        // This method does nothing...
    }
}
