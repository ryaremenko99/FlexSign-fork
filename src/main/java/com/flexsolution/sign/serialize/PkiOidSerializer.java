package com.flexsolution.sign.serialize;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sit.uapki.common.PkiOid;

import java.lang.reflect.Type;

/**
 * Serializer required for Gson lib in order to convert PkiOid enum to the sting representation and vice versa
 */
public class PkiOidSerializer implements JsonSerializer<PkiOid>, JsonDeserializer<PkiOid> {

    @Override
    public PkiOid deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new PkiOid(json.getAsString());
    }

    @Override
    public JsonElement serialize(PkiOid src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
