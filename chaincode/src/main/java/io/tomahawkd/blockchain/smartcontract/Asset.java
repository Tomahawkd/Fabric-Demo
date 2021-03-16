/*
 * SPDX-License-Identifier: Apache-2.0
 */

package io.tomahawkd.blockchain.smartcontract;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
public final class Asset {

    @Property()
    @SerializedName("uid")
    private final String uid;

    @Property()
    @SerializedName("owners")
    private final List<String> ownerMsps;

    @Property()
    @SerializedName("pending")
    private String pendingOwnerMsp;

    @Property()
    @SerializedName("status")
    private int status;

    public String getUid() {
        return uid;
    }

    public String[] getOwnerMsps() {
        return ownerMsps.toArray(new String[0]);
    }

    public String getPendingOwnerMsp() {
        return pendingOwnerMsp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public AssetStatus setStatusType(AssetStatus status) {
        this.status = status.ordinal();
        return status;
    }

    public AssetStatus getStatusType() {
        return AssetStatus.values()[this.status];
    }

    public String getCurrentOwnerMsp() {
        return ownerMsps.get(ownerMsps.size() - 1);
    }

    Asset(final String uid, final String owner) {
        this.uid = uid;
        this.ownerMsps = new ArrayList<>();
        this.ownerMsps.add(owner);
        this.pendingOwnerMsp = null;
        setStatusType(AssetStatus.APPROVED);
    }

    public AssetStatus orderAsset(final String originalOwner, final String newOwner) {
        if (getStatusType() != AssetStatus.APPROVED) return AssetStatus.NOT_AVAILABLE;
        // check original owner
        if (!getCurrentOwnerMsp().equals(originalOwner)) return AssetStatus.FORBIDDEN;
        pendingOwnerMsp = newOwner;
        return setStatusType(AssetStatus.CONFIRMING);
    }

    public AssetStatus transferAsset(final String originalOwner) {
        // you could not transfer when the status is not approved
        if (getStatusType() != AssetStatus.CONFIRMING) return AssetStatus.INVALID;
        // check caller msp
        if (!getCurrentOwnerMsp().equals(originalOwner)) return AssetStatus.FORBIDDEN;
        return setStatusType(AssetStatus.TRANSFERRING);
    }

    public AssetStatus discardAsset(final String newOwner) {
        // you must discard during the TRANSFERRING status
        if (getStatusType() != AssetStatus.TRANSFERRING &&
                getStatusType() != AssetStatus.CONFIRMING) return AssetStatus.NOT_AVAILABLE;
        // check caller msp
        if (!pendingOwnerMsp.equals(newOwner)) return AssetStatus.FORBIDDEN;
        else return setStatusType(AssetStatus.DISCARDING);
    }

    public AssetStatus confirmDiscardAsset(final String originalOwner) {
        // you must confirm it during the DISCARDING status
        if (getStatusType() != AssetStatus.DISCARDING) return AssetStatus.INVALID;
        // check caller msp
        if (!getCurrentOwnerMsp().equals(originalOwner)) return AssetStatus.FORBIDDEN;
        pendingOwnerMsp = null;
        return setStatusType(AssetStatus.APPROVED);
    }

    public AssetStatus confirmReceiveAsset(final String newOwner) {
        if (getStatusType() != AssetStatus.TRANSFERRING) return AssetStatus.INVALID;
        // check caller msp
        if (!pendingOwnerMsp.equals(newOwner)) return AssetStatus.FORBIDDEN;
        ownerMsps.add(pendingOwnerMsp);
        pendingOwnerMsp = null;
        return setStatusType(AssetStatus.APPROVED);
    }

    public AssetStatus requestDelete(final String originalOwner) {
        if (!getCurrentOwnerMsp().equals(originalOwner)) return AssetStatus.FORBIDDEN;
        if (getStatusType() != AssetStatus.APPROVED) return AssetStatus.INVALID;
        else return AssetStatus.APPROVED;
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
}
