package thelm.jaopca.gtceu.compat.gtceu;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import thelm.jaopca.api.data.IDataModule;
import thelm.jaopca.api.data.JAOPCADataModule;
import thelm.jaopca.utils.ApiImpl;

@JAOPCADataModule(modDependencies = "gtceu@[1.4,)")
public class GTCEuDataModule implements IDataModule {

	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public String getName() {
		return "gtceu";
	}

	@Override
	public void register() {
		Path cacheDir = FMLPaths.CONFIGDIR.get().resolve("jaopca").resolve("gtceu");
		Path itemTagCacheFile = cacheDir.resolve("gtceu_item_tags.json");
		Path materialCacheFile = cacheDir.resolve("gtceu_materials.json");
		try {
			InputStream is = Files.exists(itemTagCacheFile) ? Files.newInputStream(itemTagCacheFile) :
				getClass().getResourceAsStream("/data/jaopcagtceu/defaultcache/gtceu_item_tags.json");
			Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			JsonElement json = JsonParser.parseReader(reader);
			is.close();
			List<String> tags = Codec.STRING.listOf().parse(JsonOps.INSTANCE, json).result().orElse(List.of());
			for(String tag : tags) {
				ApiImpl.INSTANCE.registerDefinedItemTag(new ResourceLocation(tag));
			}
		}
		catch(Exception e) {
			LOGGER.error("Cannot read GTCEu item tags", e);
		}
		try {
			InputStream is = Files.exists(materialCacheFile) ? Files.newInputStream(materialCacheFile) :
				getClass().getResourceAsStream("/data/jaopcagtceu/defaultcache/gtceu_materials.json");
			Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			JsonElement json = JsonParser.parseReader(reader);
			is.close();
			List<GTCEuMaterialData> materials = GTCEuMaterialData.CODEC.listOf().parse(JsonOps.INSTANCE, json).result().orElse(List.of());
			for(GTCEuMaterialData material : materials) {
				String name = material.name();
				GTCEuCompatModule.BLACKLIST.add(name);
				if(material.ore()) {
					GTCEuModule.BLACKLIST.add(name);
				}
				if(material.plate()) {
					GTCEuCompatModule.PLATE_BLACKLIST.add(name);
				}
				if(material.gear()) {
					GTCEuCompatModule.GEAR_BLACKLIST.add(name);
				}
				if(material.rod()) {
					GTCEuCompatModule.ROD_BLACKLIST.add(name);
				}
				if(material.dense()) {
					GTCEuCompatModule.DENSE_BLACKLIST.add(name);
				}
			}
		}
		catch(Exception e) {
			LOGGER.error("Cannot read GTCEu materials", e);
		}
	}

	public static void generateCache() {
		Path cacheDir = FMLPaths.CONFIGDIR.get().resolve("jaopca").resolve("gtceu");
		Path itemTagCacheFile = cacheDir.resolve("gtceu_item_tags.json");
		Path materialCacheFile = cacheDir.resolve("gtceu_materials.json");
		if(!Files.exists(cacheDir) || !Files.isDirectory(cacheDir)) {
			try {
				if(Files.exists(cacheDir) && !Files.isDirectory(cacheDir)) {
					LOGGER.warn("Cache directory {} is a file, deleting", cacheDir);
					Files.delete(cacheDir);
				}
				Files.createDirectory(cacheDir);
			}
			catch(Exception e) {
				LOGGER.error("Could not create cache directory {}", cacheDir, e);
				return;
			}
		}
		try {
			if(!Files.exists(itemTagCacheFile)) {
				List<String> itemTags = Stream.concat(
						ChemicalHelper.UNIFICATION_ENTRY_ITEM.entrySet().stream().
						filter(entry->!entry.getValue().isEmpty()).
						map(entry->entry.getKey()).
						filter(entry->entry.material != null).
						flatMap(entry->Arrays.stream(entry.tagPrefix.getAllItemTags(entry.material))),
						GTToolType.getTypes().values().stream().
						flatMap(type->type.itemTags.stream())).
						map(tag->tag.location().toString()).
						distinct().sorted().toList();
				JsonElement itemTagJson = Codec.STRING.listOf().encodeStart(JsonOps.INSTANCE, itemTags).result().get();
				Files.writeString(itemTagCacheFile, itemTagJson.toString(), StandardCharsets.UTF_8);
			}
		}
		catch(Exception e) {
			LOGGER.error("Could not create cache file {}", itemTagCacheFile, e);
		}
		try {
			if(!Files.exists(materialCacheFile)) {
				List<GTCEuMaterialData> materials = GTCEuAPI.materialManager.getRegisteredMaterials().stream().
						sorted().map(GTCEuMaterialData::fromGTCEuMaterial).toList();
				JsonElement materialJson = GTCEuMaterialData.CODEC.listOf().encodeStart(JsonOps.INSTANCE, materials).result().get();
				Files.writeString(materialCacheFile, materialJson.toString(), StandardCharsets.UTF_8);
			}
		}
		catch(Exception e) {
			LOGGER.error("Could not create cache file {}", materialCacheFile, e);
		}
	}
}
