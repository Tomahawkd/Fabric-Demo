/*
 * SPDX-License-Identifier: Apache-2.0
 */

package io.tomahawkd.blockchain.application.utils;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Asset {

    @SerializedName("uid")
    private final String uid;

    @SerializedName("owners")
    private final List<String> ownerMsps = new ArrayList<>();

    @SerializedName("pending")
    private final String pendingOwnerMsp;

    @SerializedName("status")
    private final int status;

    public String getUid() {
        return uid;
    }

    public List<String> getOwnerMsps() {
        return ownerMsps;
    }

    public String getPendingOwnerMsp() {
        return pendingOwnerMsp;
    }

    public AssetStatus getStatus() {
        return AssetStatus.values()[this.status];
    }

    public String getCurrentOwnerMsp() {
        return ownerMsps.get(ownerMsps.size() - 1);
    }

    public Asset(final String uid, final String owner) {
        this(uid, owner, "", AssetStatus.APPROVED);
    }

    // test only
    public Asset(String uid, String owner, String pendingOwnerMsp, AssetStatus status) {
        this.uid = uid;
        this.ownerMsps.add(owner);
        this.pendingOwnerMsp = pendingOwnerMsp;
        this.status = status.ordinal();
    }

    public Asset(String uid, ArrayList<String> ownerMsps, String pendingOwnerMsp, String status) {
        this.uid = uid;
        this.ownerMsps.addAll(ownerMsps);
        this.pendingOwnerMsp = pendingOwnerMsp;
        this.status = AssetStatus.valueOf(status).ordinal();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Asset other = (Asset) obj;

        return uid.equals(other.uid) &&
                Objects.deepEquals(getOwnerMsps(), other.getOwnerMsps()) &&
                status == other.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, ownerMsps.hashCode(), status);
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this, Asset.class);
    }

    public String toJson() {
        return toString();
    }

    public static Asset fromJson(String json) throws JsonSyntaxException {
        return new GsonBuilder().create().fromJson(json, Asset.class);
    }

    public static Asset createErrorAsset() {
        return new Asset("", "", "", AssetStatus.INVALID);
    }
}
