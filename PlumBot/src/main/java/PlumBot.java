/*
 * Wymaga biblioteki:
 * - JDA (https://github.com/DV8FromTheWorld/JDA)
 * - Rome (https://rometools.github.io/rome/)
 * - SLF4J Simple (do logowania)
 */

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.*;

import javax.security.auth.login.LoginException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

public class PlumBot extends ListenerAdapter {

    private static final String TOKEN = "MTM5NTM4Mjc4ODc2ODkyMzY0OA.GDw78h.bTTFmBaGBmAm1ggzwbe3v04R5sMBBdxoXgdU5Q";
    private static final String EARLY_PLAYLIST = "https://www.youtube.com/feeds/videos.xml?playlist_id=UUMOHVQuaye22jLJptftYUssDA";
    private static final String PUBLIC_PLAYLIST = "https://www.youtube.com/feeds/videos.xml?playlist_id=UUHVQuaye22jLJptftYUssDA";

    private final Set<String> seenEarly = new HashSet<>();
    private final Set<String> seenPublic = new HashSet<>();
    private TextChannel notificationChannel;

    public static void main(String[] args) throws LoginException {
        JDABuilder.createDefault(TOKEN)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new PlumBot())
                .build();
    }

    @Override
    public void onReady(net.dv8tion.jda.api.events.session.ReadyEvent event) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> checkFeeds(event.getJDA()), 0, 5, TimeUnit.MINUTES);
        System.out.println("Bot gotowy");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String content = event.getMessage().getContentRaw();
        if (content.equalsIgnoreCase("!ustawkanal")) {
            notificationChannel = event.getChannel().asTextChannel();
            notificationChannel.sendMessage("✅ Kanał powiadomień został ustawiony.").queue();
        }
    }

    private void checkFeeds(JDA jda) {
        if (notificationChannel == null) return;

        try {
            // Early access
            SyndFeed earlyFeed = new SyndFeedInput().build(new XmlReader(new URL(EARLY_PLAYLIST)));
            for (SyndEntry entry : earlyFeed.getEntries()) {
                String videoId = extractVideoId(entry.getLink());
                if (seenEarly.add(videoId)) {
                    if (seenPublic.contains(videoId)) continue;
                    notificationChannel.sendMessage("🌟 **Nowy film dla wspierających!**\n" + entry.getTitle() + "\n" + entry.getLink()).queue();
                }
            }

            // Public
            SyndFeed publicFeed = new SyndFeedInput().build(new XmlReader(new URL(PUBLIC_PLAYLIST)));
            for (SyndEntry entry : publicFeed.getEntries()) {
                String videoId = extractVideoId(entry.getLink());
                if (seenPublic.add(videoId)) {
                    if (seenEarly.contains(videoId)) continue;
                    notificationChannel.sendMessage("🎬 **Nowy publiczny film!**\n" + entry.getTitle() + "\n" + entry.getLink()).queue();
                }
            }

        } catch (Exception e) {
            System.err.println("Błąd przy sprawdzaniu kanałów: " + e.getMessage());
        }
    }

    private String extractVideoId(String link) {
        int index = link.indexOf("?v=");
        return (index != -1) ? link.substring(index + 3) : link;
    }
}
