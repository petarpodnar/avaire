package com.avairebot.scheduler;

import com.avairebot.AvaIre;
import com.avairebot.cache.CacheType;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FetchMemeTypesJob extends Job {

    private final String apiEndpoint = "https://memegen.link/api/templates/";

    public FetchMemeTypesJob(AvaIre avaire) {
        super(avaire, 0, 7, TimeUnit.DAYS);
    }

    @Override
    public void run() {
        RequestFactory.makeGET(apiEndpoint)
            .addHeader("Cache-Control", "no-cache")
            .addHeader("Content-Type", "application/json")
            .send((Consumer<Response>) response -> {
                HashMap<String, String> memes = (HashMap<String, String>) response.toService(HashMap.class);
                HashMap<String, HashMap<String, String>> cache = new HashMap<>();

                for (Map.Entry<String, String> entry : memes.entrySet()) {
                    HashMap<String, String> meme = new HashMap<>();
                    meme.put("name", entry.getKey());
                    meme.put("url", entry.getValue());

                    cache.put(entry.getValue().substring(apiEndpoint.length(), entry.getValue().length()), meme);

                }

                avaire.getCache().getAdapter(CacheType.FILE).forever("meme.types", cache);
            });

    }
}
