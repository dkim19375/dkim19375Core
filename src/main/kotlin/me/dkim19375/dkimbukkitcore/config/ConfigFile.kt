/*
 * MIT License
 *
 * Copyright (c) 2021 dkim19375
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.dkim19375.dkimbukkitcore.config

import me.dkim19375.dkimbukkitcore.javaplugin.CoreJavaPlugin
import me.dkim19375.dkimcore.annotation.API
import me.dkim19375.dkimcore.extension.createFileAndDirs
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.*
import java.nio.file.Files
import java.util.logging.Level
import kotlin.io.path.createDirectories
import kotlin.math.max

/**
 * @param plugin The CoreJavaPlugin extending class
 * @param fileName The name of the config file excluding file extensions.
 *
 * Please notice that the constructor does not yet create the YAML-configuration file. To create the file on the disk, use [ConfigFile.createConfig].
 **/

@API
class ConfigFile(private val plugin: CoreJavaPlugin, val fileName: String) {
    private val configFile: File

    @API
    var config: FileConfiguration
        private set
    private val pluginDataFolder: File

    init {
        var fileName = fileName
        fileName = fileName.replace('\\', '/')
        configFile = File(plugin.dataFolder, fileName)
        pluginDataFolder = plugin.dataFolder
        config = YamlConfiguration.loadConfiguration(configFile)
    }

    /**
     * This creates the configuration file. If the data folder is invalid, it will be created along with the config file.
     *
     * @return true if the file was successfully created
     */
    @API
    fun createConfig(): Boolean {
        var success = false
        if (!configFile.exists()) {
            if (!pluginDataFolder.exists() && pluginDataFolder.mkdir()) {
                success = true
            }
            try {
                if (configFile.createNewFile()) {
                    success = true
                }
            } catch (e: IOException) {
                e.printStackTrace()
                success = false
            }
        }
        return success
    }

    @API
    fun addIfDoesntExist(key: String, value: String?) {
        if (config.getString(key) == null) {
            config[key] = value
        }
    }

    /**
     * @since 1.0.0
     * @param defaults A map containing the default configuration keys and values.
     * This sets the default configuration values as the ones contained in the map.
     */
    @API
    fun addDefaults(defaults: Map<String, Any>) = config.addDefaults(defaults)

    /**
     * @since 1.0.0
     * This saves the configuration file. Saving is required every time you write to it.
     */
    @API
    fun save() = config.save(configFile)

    /**
     * @since 1.0.0
     * This reloads the configuration file, making Java acknowledge and load the new config and its values.
     */
    @API
    fun reload() {
        config = YamlConfiguration.loadConfiguration(configFile)
    }

    /**
     * @since 1.0.0
     * @return true if and only if the file or directory is
     * successfully deleted; otherwise
     * This deletes the config file.
     */
    @API
    fun deleteFile(): Boolean = configFile.delete()

    /**
     * @since 1.0.0
     * @throws IOException in case deletion is unsuccessful
     * This deletes the config file's directory and all it's contents.
     */
    @API
    fun deleteDir(): Boolean = pluginDataFolder.deleteRecursively()

    /**
     * @since 1.0.0
     * @return true if the file was successfully reset
     * This deletes and recreates the file, wiping all its contents.
     */
    @API
    fun reset() {
        deleteFile()
        configFile.createFileAndDirs()
    }

    /**
     * @since 1.0.0
     * @return true if the directory was successfully wiped
     * Wipe the config file's directory, including the file itself.
     */
    @API
    fun wipeDirectory() {
        deleteDir()
        pluginDataFolder.toPath().createDirectories()
    }

    /**
     * @since 1.0.0
     * @param name The sub directory's name.
     * @return true if and only if the file or directory is
     * successfully deleted; otherwise
     * @throws IOException If the entered string has a file extension or already exists.
     * This will create a sub-directory in the plugin's data folder, which can be accessed with [ConfigFile.pluginDataFolder]
     * If the entered name is not a valid name for a directory or the sub-directory already exists or the data folder does not exist, an IOException will be thrown.
     */
    @API
    fun createSubDirectory(name: String) {
        pluginDataFolder.toPath().createDirectories()
        val subDir = File(pluginDataFolder, name).toPath()
        subDir.createDirectories()
    }

    /**
     * @since 1.0.0
     * @param value - Check if it contains the string
     * @param ignoreDefault - Returns false if there is no set value, even if there is a default value
     * @return true or false
     * This returns true if the config contains the given value.
     */
    fun contains(value: String, ignoreDefault: Boolean = true): Boolean = config.contains(value, ignoreDefault)

    private fun getResource(): InputStream? =
        plugin.javaClass.classLoader.getResourceAsStream(fileName) ?: plugin.javaClass.getResourceAsStream(fileName)

    @API
    fun saveDefaultConfig() {
        if (!configFile.exists()) {
            saveResource()
        }
    }

    private fun saveResource() {
        val resource = getResource()
        if (resource == null) {
            File(pluginDataFolder, fileName).createFileAndDirs()
            return
        }
        pluginDataFolder.toPath().createDirectories()
        Files.copy(resource, File(pluginDataFolder, fileName).toPath())
    }
}