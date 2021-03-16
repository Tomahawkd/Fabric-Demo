/*
 * SPDX-License-Identifier: Apache-2.0
 */

package io.tomahawkd.blockchain.smartcontract;

import com.google.gson.GsonBuilder;
import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.*;

public final class AssetTransferTest {

    private final List<Asset> globalAssetList = new ArrayList<>();

    private static final class MockKeyValue implements KeyValue {

        private final String key;
        private final String value;

        MockKeyValue(final String key, final String value) {
            super();
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getStringValue() {
            return this.value;
        }

        @Override
        public byte[] getValue() {
            return this.value.getBytes();
        }

    }

    private final class MockAssetResultsIterator implements QueryResultsIterator<KeyValue> {

        private final List<KeyValue> assetList;

        MockAssetResultsIterator() {
            super();

            this.assetList = new ArrayList<>();
            globalAssetList.add(AssetFactory.create("1", "Blue"));
            globalAssetList.add(AssetFactory.create("2", "Red"));
            globalAssetList.add(AssetFactory.create("3", "Orange"));
            globalAssetList.add(AssetFactory.create("4", "Green"));
            globalAssetList.add(AssetFactory.create("5", "Yellow"));
            globalAssetList.add(AssetFactory.create("6", "Black"));

            globalAssetList.forEach(e -> this.assetList.add(new MockKeyValue(e.getUid(), e.toJson())));
        }

        @Override
        public Iterator<KeyValue> iterator() {
            return this.assetList.iterator();
        }

        @Override
        public void close() throws Exception {
            // do nothing
        }

    }

    @Test
    public void invokeUnknownTransaction() {
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);

        Throwable thrown = catchThrowable(() -> contract.unknownTransaction(ctx));

        assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                .hasMessage("Undefined contract method called");
        verifyZeroInteractions(ctx);
    }

    @Nested
    class InvokeReadAssetTransaction {

        @Test
        public void whenAssetExists() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            Asset expectedAsset = AssetFactory.create("1", "Blue");
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("1")).thenReturn(expectedAsset.toJson());

            Asset asset = contract.ReadAsset(ctx, "1");

            assertThat(asset).isEqualTo(expectedAsset);
        }

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> contract.ReadAsset(ctx, "1"));

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Asset 1 does not exist");
        }
    }

    @Nested
    class InvokeCreateAssetTransaction {

        @Test
        public void whenAssetExists() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            Asset asset = AssetFactory.create("1", "Blue");
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Throwable thrown = catchThrowable(() -> contract.CreateAsset(ctx, "1"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause().hasMessage("Asset 1 already exists");
        }

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test1MSP");
            when(stub.getStringState("1")).thenReturn("");

            Asset asset = contract.CreateAsset(ctx, "1");

            assertThat(asset).isEqualTo(AssetFactory.create("1", ctx.getClientIdentity().getMSPID()));
        }
    }

    @Test
    void invokeGetAllAssetsTransaction() {
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStateByRange("", "")).thenReturn(new MockAssetResultsIterator());

        String assets = contract.GetAllAssets(ctx);
        assertThat(assets).isEqualTo(new GsonBuilder().create().toJson(globalAssetList));
    }

    @Nested
    class InvokeOrderAssetTransaction {

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test2MSP");
            when(stub.getStringState("1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> contract.OrderAsset(ctx, "1", "Test1MSP"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause().hasMessage("Asset 1 does not exist");
        }

        @Test
        public void whenNotAvailableAssetRequests() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            Asset asset = new Asset("1", "Test1MSP");
            asset.setStatusType(AssetStatus.CONFIRMING);

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test2MSP");
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Throwable thrown = catchThrowable(() -> contract.OrderAsset(ctx, "1", "Test1MSP"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Error occurs while ordering: NOT_AVAILABLE");
        }

        @Test
        public void whenAssetWithWrongOwnerRequests() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            Asset asset = new Asset("1", "Test1MSP");

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test2MSP");
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Throwable thrown = catchThrowable(() -> contract.OrderAsset(ctx, "1", "Test0MSP"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Error occurs while ordering: FORBIDDEN");
        }

        @Test
        public void whenAssetRequests() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            Asset asset = new Asset("1", "Test1MSP");

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test2MSP");
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Asset assetResult = contract.OrderAsset(ctx, "1", "Test1MSP");
            asset.setStatusType(AssetStatus.CONFIRMING);
            assertThat(assetResult).isEqualTo(asset);
        }
    }

    @Nested
    class InvokeTransferAssetTransaction {

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test1MSP");
            when(stub.getStringState("1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> contract.TransferAsset(ctx, "1"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause().hasMessage("Asset 1 does not exist");
        }

        @Test
        public void whenNotAvailableAssetRequests() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            Asset asset = new Asset("1", "Test1MSP");
            asset.orderAsset("Test1MSP", "Test2MSP");
            asset.setStatusType(AssetStatus.TRANSFERRING);

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test1MSP");
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Throwable thrown = catchThrowable(() -> contract.TransferAsset(ctx, "1"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Error occurs while ordering: INVALID");
        }

        @Test
        public void whenAssetWithWrongOwnerRequests() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            Asset asset = new Asset("1", "Test1MSP");
            asset.orderAsset("Test1MSP", "Test2MSP");

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test0MSP");
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Throwable thrown = catchThrowable(() -> contract.TransferAsset(ctx, "1"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Error occurs while ordering: FORBIDDEN");
        }

        @Test
        public void whenAssetRequests() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            Asset asset = new Asset("1", "Test1MSP");
            asset.orderAsset("Test1MSP", "Test2MSP");

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test1MSP");
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Asset assetResult = contract.TransferAsset(ctx, "1");
            asset.transferAsset("Test1MSP");
            assertThat(assetResult).isEqualTo(asset);
        }
    }

    @Nested
    class InvokeDiscardAssetTransaction {

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test2MSP");
            when(stub.getStringState("1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> contract.DiscardAsset(ctx, "1"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause().hasMessage("Asset 1 does not exist");
        }

        @Test
        public void whenNotAvailableAssetRequests() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            Asset asset = new Asset("1", "Test1MSP");
            asset.orderAsset("Test1MSP", "Test2MSP");
            asset.setStatusType(AssetStatus.DISCARDING);

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test2MSP");
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Throwable thrown = catchThrowable(() -> contract.DiscardAsset(ctx, "1"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Error occurs while ordering: NOT_AVAILABLE");
        }

        @Test
        public void whenAssetWithWrongOwnerRequests() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            Asset asset = new Asset("1", "Test1MSP");
            asset.orderAsset("Test1MSP", "Test2MSP");

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test0MSP");
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Throwable thrown = catchThrowable(() -> contract.DiscardAsset(ctx, "1"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Error occurs while ordering: FORBIDDEN");
        }

        @Test
        public void whenAssetAfterOrderRequests() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            Asset asset = new Asset("1", "Test1MSP");
            asset.orderAsset("Test1MSP", "Test2MSP");

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test2MSP");
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Asset assetResult = contract.DiscardAsset(ctx, "1");
            asset.setStatusType(AssetStatus.DISCARDING);
            assertThat(assetResult).isEqualTo(asset);
        }

        @Test
        public void whenAssetAfterTransferRequests() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            Asset asset = new Asset("1", "Test1MSP");
            asset.orderAsset("Test1MSP", "Test2MSP");
            asset.transferAsset("Test1MSP");

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test2MSP");
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Asset assetResult = contract.DiscardAsset(ctx, "1");
            asset.setStatusType(AssetStatus.DISCARDING);
            assertThat(assetResult).isEqualTo(asset);
        }
    }

    @Nested
    class InvokeConfirmDiscardAssetTransaction {

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test1MSP");
            when(stub.getStringState("1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> contract.ConfirmDiscardAsset(ctx, "1"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause().hasMessage("Asset 1 does not exist");
        }

        @Test
        public void whenNotAvailableAssetRequests() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            Asset asset = new Asset("1", "Test1MSP");
            asset.orderAsset("Test1MSP", "Test2MSP");

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test1MSP");
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Throwable thrown = catchThrowable(() -> contract.ConfirmDiscardAsset(ctx, "1"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Error occurs while ordering: INVALID");
        }

        @Test
        public void whenAssetWithWrongOwnerRequests() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            Asset asset = new Asset("1", "Test1MSP");
            asset.orderAsset("Test1MSP", "Test2MSP");
            asset.discardAsset("Test2MSP");

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test0MSP");
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Throwable thrown = catchThrowable(() -> contract.ConfirmDiscardAsset(ctx, "1"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Error occurs while ordering: FORBIDDEN");
        }

        @Test
        public void whenAssetRequests() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            Asset asset = new Asset("1", "Test1MSP");
            asset.orderAsset("Test1MSP", "Test2MSP");
            asset.discardAsset("Test2MSP");

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test1MSP");
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Asset assetResult = contract.ConfirmDiscardAsset(ctx, "1");
            asset.confirmDiscardAsset("Test1MSP");
            assertThat(assetResult).isEqualTo(asset);
        }
    }

    @Nested
    class InvokeConfirmReceiveAssetTransaction {

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test2MSP");
            when(stub.getStringState("1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> contract.ConfirmReceiveAsset(ctx, "1"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause().hasMessage("Asset 1 does not exist");
        }

        @Test
        public void whenNotAvailableAssetRequests() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            Asset asset = new Asset("1", "Test1MSP");
            asset.setStatusType(AssetStatus.CONFIRMING);

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test2MSP");
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Throwable thrown = catchThrowable(() -> contract.ConfirmReceiveAsset(ctx, "1"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Error occurs while ordering: INVALID");
        }

        @Test
        public void whenAssetWithWrongOwnerRequests() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            Asset asset = new Asset("1", "Test1MSP");
            asset.orderAsset("Test1MSP", "Test2MSP");
            asset.transferAsset("Test1MSP");

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test0MSP");
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Throwable thrown = catchThrowable(() -> contract.ConfirmReceiveAsset(ctx, "1"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Error occurs while ordering: FORBIDDEN");
        }

        @Test
        public void whenAssetRequests() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            Asset asset = new Asset("1", "Test1MSP");
            asset.orderAsset("Test1MSP", "Test2MSP");
            asset.transferAsset("Test1MSP");

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test2MSP");
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Asset assetResult = contract.ConfirmReceiveAsset(ctx, "1");
            asset.confirmReceiveAsset("Test2MSP");
            assertThat(assetResult).isEqualTo(asset);
        }
    }

    @Nested
    class DeleteAssetTransaction {

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test1MSP");
            when(stub.getStringState("1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> contract.DeleteAsset(ctx, "1"));

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Asset 1 does not exist");
        }

        @Test
        public void whenAssetExistWithoutPermission() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            Asset asset = AssetFactory.create("1", "Blue");

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test1MSP");
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Throwable thrown = catchThrowable(() -> contract.DeleteAsset(ctx, "1"));

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("MSP Test1MSP have no permission to delete this with error FORBIDDEN.");
        }

        @Test
        public void whenInProgressAssetExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            ClientIdentity identity = mock(ClientIdentity.class);
            Asset asset = new Asset("1", "Test1MSP");
            asset.setStatusType(AssetStatus.CONFIRMING);

            when(ctx.getStub()).thenReturn(stub);
            when(ctx.getClientIdentity()).thenReturn(identity);
            when(identity.getMSPID()).thenReturn("Test1MSP");
            when(stub.getStringState(asset.getUid())).thenReturn(asset.toJson());

            Throwable thrown = catchThrowable(() -> contract.DeleteAsset(ctx, "1"));

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("MSP Test1MSP have no permission to delete this with error INVALID.");
        }
    }
}
