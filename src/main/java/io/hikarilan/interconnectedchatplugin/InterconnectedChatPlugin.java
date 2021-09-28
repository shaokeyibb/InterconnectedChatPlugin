package io.hikarilan.interconnectedchatplugin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.hikarilan.interconnectedchatplugin.entities.Chat;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import okhttp3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.util.List;

public final class InterconnectedChatPlugin extends JavaPlugin implements Listener {

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();

    private BukkitTask task;

    private long query_period;
    private int last_index;
    private String remote_server_address;
    private String server_name;
    private String chat_format;

    private long lastSync = System.currentTimeMillis();

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        this.query_period = getConfig().getLong("query_period", 20);
        this.last_index = getConfig().getInt("last_index", -1);
        this.remote_server_address = getConfig().getString("remote_server_address", "http://127.0.0.1:8080");
        this.server_name = getConfig().getString("server_name", "server");
        this.chat_format = getConfig().getString("chat_format", "[%server%]%message%");

        Bukkit.getPluginManager().registerEvents(this, this);

        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {

            Request request = new Request.Builder()
                    .url(HttpUrl.parse(remote_server_address + "/getChatList").newBuilder().addQueryParameter("last_index", String.valueOf(last_index)).build())
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                int code = response.code();
                if (code != 200) {
                    getLogger().warning("failed to pull chat message from remote server,remote server return response code " + code);
                    return;
                }
                List<Chat> chatList = new Gson().fromJson(response.body().string(), new TypeToken<List<Chat>>() {
                }.getType());
                chatList.stream()
                        .filter(chat -> chat.getTime() > lastSync)
                        .filter(chat -> !chat.getServer_name().equals(server_name))
                        .forEach(chat -> {
                            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', chat_format
                                            .replaceAll("%server%", chat.getServer_name()))
                                    .replaceAll("%message%", TextComponent.toLegacyText(ComponentSerializer.parse(chat.getChat_json().toString()))));
                        });
                lastSync = System.currentTimeMillis();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 0, query_period);

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent e) {
        JsonObject json = new JsonObject();
        json.addProperty("server_name", server_name);
        json.addProperty("player_name", e.getPlayer().getName());
        json.addProperty("chat_json", ComponentSerializer.toString(TextComponent.fromLegacyText(String.format(e.getFormat(), e.getPlayer().getDisplayName(), e.getMessage()))));
        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(remote_server_address + "/postChat")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            int code = response.code();
            if (code == 200) return;
            getLogger().warning("failed to push chat message to remote server,remote server return response code " + code);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        task.cancel();
    }
}
