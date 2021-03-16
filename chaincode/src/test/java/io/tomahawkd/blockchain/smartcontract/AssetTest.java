/*
 * SPDX-License-Identifier: Apache-2.0
 */

package io.tomahawkd.blockchain.smartcontract;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public final class AssetTest {

    @Test
    public void isReflexive() {
        Asset asset = new Asset("1", "Blue");

        assertThat(asset).isEqualTo(asset);
    }

    @Test
    public void isSymmetric() {
        Asset assetA = new Asset("1", "Blue");
        Asset assetB = new Asset("1", "Blue");

        assertThat(assetA).isEqualTo(assetB);
        assertThat(assetB).isEqualTo(assetA);
    }

    @Test
    public void isTransitive() {
        Asset assetA = new Asset("1", "Blue");
        Asset assetB = new Asset("1", "Blue");
        Asset assetC = new Asset("1", "Blue");

        assertThat(assetA).isEqualTo(assetB);
        assertThat(assetB).isEqualTo(assetC);
        assertThat(assetA).isEqualTo(assetC);
    }

    @Test
    public void handlesInequality() {
        Asset assetA = new Asset("1", "Blue");
        Asset assetB = new Asset("2", "Red");

        assertThat(assetA).isNotEqualTo(assetB);
    }

    @Test
    public void handlesOtherObjects() {
        Asset assetA = new Asset("1", "Blue");
        String assetB = "not a asset";

        assertThat(assetA).isNotEqualTo(assetB);
    }

    @Test
    public void handlesNull() {
        Asset asset = new Asset("1", "Blue");

        assertThat(asset).isNotEqualTo(null);
    }

    @Test
    public void handlesOrder() {
        Asset asset = new Asset("1", "Blue");
        assertThat(asset.getStatusType()).isEqualTo(AssetStatus.APPROVED);

        // check status
        Arrays.stream(AssetStatus.values()).filter(v -> v != AssetStatus.APPROVED).forEach(v -> {
            asset.setStatusType(v);
            assertThat(asset.orderAsset("Blue", "Red")).isEqualTo(AssetStatus.NOT_AVAILABLE);
        });

        asset.setStatusType(AssetStatus.APPROVED);
        // check user
        assertThat(asset.orderAsset("Red", "Blue")).isEqualTo(AssetStatus.FORBIDDEN);


        assertThat(asset.orderAsset("Blue", "Red")).isEqualTo(AssetStatus.CONFIRMING);
        assertThat(asset.getPendingOwnerMsp()).isEqualTo("Red");
    }

    @Test
    public void handlesTransfer() {
        Asset asset = new Asset("1", "Blue");
        assertThat(asset.orderAsset("Blue", "Red")).isEqualTo(AssetStatus.CONFIRMING);

        // check status
        Arrays.stream(AssetStatus.values()).filter(v -> v != AssetStatus.CONFIRMING).forEach(v -> {
            asset.setStatusType(v);
            assertThat(asset.transferAsset("Blue")).isEqualTo(AssetStatus.INVALID);
        });

        asset.setStatusType(AssetStatus.CONFIRMING);
        // check user
        assertThat(asset.transferAsset("Red")).isEqualTo(AssetStatus.FORBIDDEN);


        assertThat(asset.transferAsset("Blue")).isEqualTo(AssetStatus.TRANSFERRING);
        assertThat(asset.getPendingOwnerMsp()).isEqualTo("Red");
    }

    @Test
    public void handlesDiscard() {
        Asset asset = new Asset("1", "Blue");
        assertThat(asset.orderAsset("Blue", "Red")).isEqualTo(AssetStatus.CONFIRMING);

        // check status
        Arrays.stream(AssetStatus.values()).filter(v -> v != AssetStatus.TRANSFERRING)
                .filter(v -> v != AssetStatus.CONFIRMING).forEach(v -> {
            asset.setStatusType(v);
            assertThat(asset.discardAsset("Red")).isEqualTo(AssetStatus.NOT_AVAILABLE);
        });

        asset.setStatusType(AssetStatus.CONFIRMING);
        // check user
        assertThat(asset.discardAsset("Orange")).isEqualTo(AssetStatus.FORBIDDEN);

        assertThat(asset.discardAsset("Red")).isEqualTo(AssetStatus.DISCARDING);
        assertThat(asset.getPendingOwnerMsp()).isEqualTo("Red");

        asset.setStatusType(AssetStatus.TRANSFERRING);
        // check user
        assertThat(asset.discardAsset("Orange")).isEqualTo(AssetStatus.FORBIDDEN);

        assertThat(asset.discardAsset("Red")).isEqualTo(AssetStatus.DISCARDING);
        assertThat(asset.getPendingOwnerMsp()).isEqualTo("Red");
    }

    @Test
    public void handlesConfirmDiscard() {
        Asset asset = new Asset("1", "Blue");
        assertThat(asset.orderAsset("Blue", "Red")).isEqualTo(AssetStatus.CONFIRMING);
        assertThat(asset.transferAsset("Blue")).isEqualTo(AssetStatus.TRANSFERRING);
        assertThat(asset.discardAsset("Red")).isEqualTo(AssetStatus.DISCARDING);

        // check status
        Arrays.stream(AssetStatus.values()).filter(v -> v != AssetStatus.DISCARDING).forEach(v -> {
            asset.setStatusType(v);
            assertThat(asset.confirmDiscardAsset("Blue")).isEqualTo(AssetStatus.INVALID);
        });

        asset.setStatusType(AssetStatus.DISCARDING);
        // check user
        assertThat(asset.confirmDiscardAsset("Red")).isEqualTo(AssetStatus.FORBIDDEN);

        assertThat(asset.confirmDiscardAsset("Blue")).isEqualTo(AssetStatus.APPROVED);
        assertThat(asset.getPendingOwnerMsp()).isEqualTo(null);
    }

    @Test
    public void handlesConfirmReceive() {
        Asset asset = new Asset("1", "Blue");
        assertThat(asset.orderAsset("Blue", "Red")).isEqualTo(AssetStatus.CONFIRMING);
        assertThat(asset.transferAsset("Blue")).isEqualTo(AssetStatus.TRANSFERRING);

        // check status
        Arrays.stream(AssetStatus.values()).filter(v -> v != AssetStatus.TRANSFERRING).forEach(v -> {
            asset.setStatusType(v);
            assertThat(asset.confirmReceiveAsset("Red")).isEqualTo(AssetStatus.INVALID);
        });

        asset.setStatusType(AssetStatus.TRANSFERRING);
        // check user
        assertThat(asset.confirmReceiveAsset("Orange")).isEqualTo(AssetStatus.FORBIDDEN);

        assertThat(asset.confirmReceiveAsset("Red")).isEqualTo(AssetStatus.APPROVED);
        assertThat(asset.getCurrentOwnerMsp()).isEqualTo("Red");
        assertThat(asset.getPendingOwnerMsp()).isEqualTo(null);
    }

}
