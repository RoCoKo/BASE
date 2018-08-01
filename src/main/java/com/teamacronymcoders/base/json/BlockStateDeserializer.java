package com.teamacronymcoders.base.json;

import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.lang.reflect.Type;
import java.util.Map;

public class BlockStateDeserializer implements JsonDeserializer<IBlockState> {
    @Override
    public IBlockState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.has("block")) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(jsonObject.get("block").getAsString()));
            if (block != null) {
                if (jsonObject.has("properties")) {
                    BlockStateContainer blockStateContainer = block.getBlockState();
                    IBlockState blockState = block.getDefaultState();
                    JsonObject properties = jsonObject.getAsJsonObject("properties");
                    for (Map.Entry<String, JsonElement> entry : properties.entrySet()) {
                        IProperty<?> property = blockStateContainer.getProperty(entry.getKey());
                        if (property != null) {
                            blockState = setValueHelper(blockState, property, entry.getValue().getAsString());
                        } else {
                            throw new JsonParseException("Failed to find property: " + entry.getKey());
                        }
                    }
                    return blockState;
                } else {
                    return block.getDefaultState();
                }
            }
        }

        throw new JsonParseException("Failed to deserialize BlockState for: " + json.toString());
    }

    private static <T extends Comparable<T>> IBlockState setValueHelper(final IBlockState blockState, final IProperty<T> property, final String stringValue) throws JsonParseException {
        return property.parseValue(stringValue).toJavaUtil()
                .map(propertyValue -> blockState.withProperty(property, propertyValue))
                .orElseThrow(() -> new JsonParseException("Failed to find value " + stringValue + " for property " + property.getName()));
    }
}
