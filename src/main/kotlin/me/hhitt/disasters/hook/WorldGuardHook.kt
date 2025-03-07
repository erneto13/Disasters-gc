package me.hhitt.disasters.hook

import me.hhitt.disasters.Disasters
import me.hhitt.disasters.storage.file.FileManager
import java.net.HttpURLConnection
import java.net.URL


@Suppress("DEPRECATION")
// It is the license but the name goes brrr
class WorldGuardHook {

    private val plugin = Disasters.getInstance()
    private val key = FileManager.get("config")!!.getString("license")!!

    fun hook() {
        val apiUrl = "http://licenses.smartshub.dev:25566/licenses/check"
        try {
            // Build the URL with query parameters
            val productName = "Disasters"
            val urlString = "$apiUrl?key=$key&pn=$productName"
            val url = URL(urlString)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.setRequestMethod("GET")

            // Check the response code
            val responseCode: Int = connection.getResponseCode()

            // License is valid
            if (responseCode == 200) {
                return
            }
        } catch (_: Exception) {
        }
        // License is not valid
        plugin.logger.info("License is not valid!")
        plugin.server.pluginManager.disablePlugin(plugin)
    }

}