package org.samo_lego.simplevillagers.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.samo_lego.config2brigadier.IBrigadierConfigurator;
import org.samo_lego.config2brigadier.annotation.BrigadierDescription;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import static org.samo_lego.simplevillagers.SimpleVillagers.LOGGER;
import static org.samo_lego.simplevillagers.SimpleVillagers.MOD_ID;
import static org.samo_lego.simplevillagers.SimpleVillagers.getConfigFile;

public class Config implements IBrigadierConfigurator {
    private static final Gson GSON;

    @SerializedName("// How many ticks between each golem spawn. Defaults to 4 minutes.")
    public final String _comment_golemTimer = "";
    @BrigadierDescription(defaultOption = "4800")
    @SerializedName("golem_timer")
    public int golemTimer = 4800;


    @SerializedName("// How many ticks does golem 'live'. Defaults to 12 seconds.")
    public final String _comment_golemDyingTicks = "";
    @BrigadierDescription(defaultOption = "240")
    @SerializedName("golem_death_ticks")
    public int golemDyingTicks = 240;


    @SerializedName("// How many ticks must pass between each breeding cycle. Defaults to 5 minutes.")
    public final String _comment_breedingTimer = "";
    @BrigadierDescription(defaultOption = "6000")
    @SerializedName("breeding_timer")
    public int breedingTimer = 6000;


    @SerializedName("// Age of new baby villagers. Default is 20 minutes to grow up (like in vanilla).")
    public final String _comment_baby_age = "";
    @BrigadierDescription(defaultOption = "-24000")
    @SerializedName("baby_age")
    public int babyAge = -24000;


    /**
     * Loads config file.
     *
     * @param file file to load the language file from.
     * @return TaterzenLanguage object
     */
    public static Config loadConfigFile(File file) {
        Config config = null;
        if (file.exists()) {
            try (var fileReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            )) {
                config = GSON.fromJson(fileReader, Config.class);
            } catch (IOException e) {
                throw new RuntimeException(MOD_ID + " Problem occurred when trying to load config: ", e);
            }
        }
        if(config == null)
            config = new Config();

        config.saveConfigFile(file);

        return config;
    }

    /**
     * Saves the config to the given file.
     *
     * @param file file to save config to
     */
    public void saveConfigFile(File file) {
        try (var writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            LOGGER.error("Problem occurred when saving config: " + e.getMessage());
        }
    }

    /**
     * Changes values of current object with reflection,
     * in order to keep the same object.
     * (that still allows in-game editing)
     */
    public void reload() {
        Config newConfig = loadConfigFile(getConfigFile());

        this.reload(newConfig);
        this.save();
    }

    @Override
    public void save() {
        this.saveConfigFile(getConfigFile());
    }

    static {
        GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();
    }
}