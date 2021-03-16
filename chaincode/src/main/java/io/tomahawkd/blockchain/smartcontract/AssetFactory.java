package io.tomahawkd.blockchain.smartcontract;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class AssetFactory {

    public static Asset create(final String uid, final String owner) {
        return new Asset(uid, owner);
    }

    public static Asset fromJson(String json) throws JsonSyntaxException {
        return new GsonBuilder().create().fromJson(json, Asset.class);
    }
}
