package thelm.jaopca.gtceu.compat.gtceu;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.MaterialProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GTCEuMaterialData(String name, boolean ore, boolean plate, boolean gear, boolean rod, boolean dense) {

	public static final Codec<GTCEuMaterialData> CODEC = RecordCodecBuilder.create(instance->instance.group(
			Codec.STRING.fieldOf("name").forGetter(GTCEuMaterialData::name),
			Codec.BOOL.fieldOf("ore").forGetter(GTCEuMaterialData::ore),
			Codec.BOOL.fieldOf("plate").forGetter(GTCEuMaterialData::plate),
			Codec.BOOL.fieldOf("gear").forGetter(GTCEuMaterialData::gear),
			Codec.BOOL.fieldOf("rod").forGetter(GTCEuMaterialData::rod),
			Codec.BOOL.fieldOf("dense").forGetter(GTCEuMaterialData::dense)
			).apply(instance, GTCEuMaterialData::new));

	public static GTCEuMaterialData fromGTCEuMaterial(Material material) {
		return new GTCEuMaterialData(
				material.getName(),
				material.hasProperty(PropertyKey.ORE),
				material.hasFlag(MaterialFlags.GENERATE_PLATE),
				material.hasFlag(MaterialFlags.GENERATE_GEAR),
				material.hasFlag(MaterialFlags.GENERATE_ROD),
				material.hasFlag(MaterialFlags.GENERATE_DENSE));
	}
}
