package com.apparati.apparati.content.client;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;

import java.util.Map;

/**
 * Utility class for handling dynamic texture application on Geckolib models.
 */
public class ApparatiTextureHelper {

    /**
     * Resolves a material string (e.g., "minecraft:stone:1") to a BlockState.
     * Supports formats: "modid:name", "modid:name:meta", and legacy short names (e.g. "gold").
     */
    public static IBlockState getBlockStateFromMaterial(String material) {
        if (material == null) return net.minecraft.init.Blocks.IRON_BLOCK.getDefaultState();

        Block block = null;
        int meta = 0;
        String lookupName = material;

        // Check for metadata in format "modid:name:meta"
        if (material.chars().filter(ch -> ch == ':').count() >= 2) {
            int lastColon = material.lastIndexOf(':');
            try {
                String possibleMeta = material.substring(lastColon + 1);
                if (possibleMeta.matches("\\d+")) {
                    meta = Integer.parseInt(possibleMeta);
                    lookupName = material.substring(0, lastColon);
                }
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }

        // Try explicit registry name lookup
        if (lookupName.contains(":")) {
            block = Block.getBlockFromName(lookupName);
        }
        
        // Try standard block naming convention (e.g. "gold" -> "minecraft:gold_block")
        if (block == null) {
            block = Block.getBlockFromName("minecraft:" + lookupName + "_block");
        }
        
        // Try direct naming (e.g. "obsidian" -> "minecraft:obsidian")
        if (block == null) {
            block = Block.getBlockFromName("minecraft:" + lookupName);
        }

        // Fallback for legacy short names that don't match the above patterns
        if (block == null) {
            if (lookupName.equals("gold")) block = net.minecraft.init.Blocks.GOLD_BLOCK;
            else if (lookupName.equals("iron")) block = net.minecraft.init.Blocks.IRON_BLOCK;
            else if (lookupName.equals("redstone")) block = net.minecraft.init.Blocks.REDSTONE_BLOCK;
            else if (lookupName.equals("lapis")) block = net.minecraft.init.Blocks.LAPIS_BLOCK;
            else if (lookupName.equals("diamond")) block = net.minecraft.init.Blocks.DIAMOND_BLOCK;
            else if (lookupName.equals("emerald")) block = net.minecraft.init.Blocks.EMERALD_BLOCK;
            else if (lookupName.equals("glass")) block = net.minecraft.init.Blocks.GLASS;
            else if (lookupName.equals("stone")) block = net.minecraft.init.Blocks.STONE;
        }

        // Default to Iron Block if resolution fails
        if (block == null) block = net.minecraft.init.Blocks.IRON_BLOCK;

        // Retrieve the specific state from metadata
        try {
            return block.getStateFromMeta(meta);
        } catch (Exception e) {
            return block.getDefaultState();
        }
    }

    /**
     * Applies a TextureAtlasSprite to a GeoBone's geometry by modifying UV coordinates.
     * This method only applies to the specific bone's cubes and does not recurse to children.
     * Original UVs are saved to the provided map to allow restoration later.
     */
    public static void applySpriteToBoneGeometry(GeoBone bone, TextureAtlasSprite sprite, Map<GeoVertex, float[]> originalUVs) {
        // Iterate over all cubes in the bone
        for (GeoCube cube : bone.childCubes) {
            for (GeoQuad quad : cube.quads) {
                for (GeoVertex vertex : quad.vertices) {
                    // Save original UVs if not already saved to avoid permanent modification
                    if (!originalUVs.containsKey(vertex)) {
                        originalUVs.put(vertex, new float[]{vertex.textureU, vertex.textureV});
                    }

                    float u = vertex.textureU;
                    float v = vertex.textureV;
                    
                    // Scale UVs to tile the 16x16 block texture across the 64x64 model texture space.
                    // Factor of 4 comes from 64 / 16.
                    float normalizedU = u * 4.0f;
                    float normalizedV = v * 4.0f;
                    
                    // Keep only the fractional part to ensure tiling
                    normalizedU = normalizedU - (float)Math.floor(normalizedU);
                    normalizedV = normalizedV - (float)Math.floor(normalizedV);
                    
                    // Map the normalized (0-1) UVs to the sprite's location on the texture atlas
                    vertex.textureU = sprite.getInterpolatedU(normalizedU * 16.0);
                    vertex.textureV = sprite.getInterpolatedV(normalizedV * 16.0);
                }
            }
        }
    }

    /**
     * Restores the original UV coordinates from the map and clears it.
     * This prevents destructive edits to the cached model.
     */
    public static void restoreUVs(Map<GeoVertex, float[]> originalUVs) {
        for (Map.Entry<GeoVertex, float[]> entry : originalUVs.entrySet()) {
            GeoVertex vertex = entry.getKey();
            float[] uvs = entry.getValue();
            vertex.textureU = uvs[0];
            vertex.textureV = uvs[1];
        }
        originalUVs.clear();
    }
}
