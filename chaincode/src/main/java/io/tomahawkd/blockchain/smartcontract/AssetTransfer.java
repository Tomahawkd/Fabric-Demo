/*
 * SPDX-License-Identifier: Apache-2.0
 */

package io.tomahawkd.blockchain.smartcontract;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.GsonBuilder;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

@Contract(
        name = "basic",
        info = @Info(
                title = "Trace system",
                description = "A trace system that traces the goods",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "developer@platform.com",
                        name = "Trace",
                        url = "https://trace.platform.com")))
@Default
public final class AssetTransfer implements ContractInterface {

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String LoadDemo(final Context ctx) {
        Asset asset1 = new Asset("1", "SupplierMSP");
        Asset asset2 = new Asset("2", "SupplierMSP");
        Asset asset3 = new Asset("3", "SupplierMSP");
        Asset asset4 = new Asset("4", "SupplierMSP");
        Asset asset5 = new Asset("5", "SupplierMSP");
        Asset asset6 = new Asset("6", "SupplierMSP");
        Asset asset7 = new Asset("7", "SupplierMSP");
        Asset asset8 = new Asset("8", "SupplierMSP");
        Asset asset9 = new Asset("9", "SupplierMSP");

        asset3.orderAsset("SupplierMSP", "SellerMSP");

        asset4.orderAsset("SupplierMSP", "SellerMSP");
        asset4.transferAsset("SupplierMSP");

        asset5.orderAsset("SupplierMSP", "SellerMSP");
        asset5.transferAsset("SupplierMSP");
        asset5.discardAsset("SellerMSP");

        asset6.orderAsset("SupplierMSP", "SellerMSP");
        asset6.transferAsset("SupplierMSP");
        asset6.confirmReceiveAsset("SellerMSP");

        asset7.orderAsset("SupplierMSP", "SellerMSP");
        asset7.transferAsset("SupplierMSP");
        asset7.confirmReceiveAsset("SellerMSP");
        asset7.orderAsset("SellerMSP", "PlatformMSP");

        asset8.orderAsset("SupplierMSP", "SellerMSP");
        asset8.transferAsset("SupplierMSP");
        asset8.confirmReceiveAsset("SellerMSP");
        asset8.orderAsset("SellerMSP", "PlatformMSP");
        asset8.transferAsset("SellerMSP");

        asset9.orderAsset("SupplierMSP", "SellerMSP");
        asset9.transferAsset("SupplierMSP");
        asset9.confirmReceiveAsset("SellerMSP");
        asset9.orderAsset("SellerMSP", "PlatformMSP");
        asset9.transferAsset("SellerMSP");
        asset9.confirmReceiveAsset("PlatformMSP");

        ctx.getStub().putStringState(asset1.getUid(), asset1.toJson());
        ctx.getStub().putStringState(asset2.getUid(), asset2.toJson());
        ctx.getStub().putStringState(asset3.getUid(), asset3.toJson());
        ctx.getStub().putStringState(asset4.getUid(), asset4.toJson());
        ctx.getStub().putStringState(asset5.getUid(), asset5.toJson());
        ctx.getStub().putStringState(asset6.getUid(), asset6.toJson());
        ctx.getStub().putStringState(asset7.getUid(), asset7.toJson());
        ctx.getStub().putStringState(asset8.getUid(), asset8.toJson());
        ctx.getStub().putStringState(asset9.getUid(), asset9.toJson());

        return "OK";
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String CreateAsset(final Context ctx, final String uid) {
        ChaincodeStub stub = ctx.getStub();

        if (AssetExists(ctx, uid)) {
            String errorMessage = String.format("Asset %s already exists", uid);
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Asset asset = AssetFactory.create(uid, ctx.getClientIdentity().getMSPID());
        stub.putStringState(uid, asset.toJson());

        return asset.toJson();
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String ReadAsset(final Context ctx, final String uid) {
        ChaincodeStub stub = ctx.getStub();
        String json = stub.getStringState(uid);

        if (json == null || json.isEmpty()) {
            String errorMessage = String.format("Asset %s does not exist", uid);
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        return AssetFactory.fromJson(json).toJson();
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String OrderAsset(final Context ctx, final String uid, final String originalOwner) {
        ChaincodeStub stub = ctx.getStub();
        String json = stub.getStringState(uid);

        if (json == null || json.isEmpty()) {
            String errorMessage = String.format("Asset %s does not exist", uid);
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Asset asset = AssetFactory.fromJson(json);
        AssetStatus result = asset.orderAsset(originalOwner, ctx.getClientIdentity().getMSPID());
        if (result == AssetStatus.FORBIDDEN ||
                result == AssetStatus.INVALID ||
                result == AssetStatus.NOT_AVAILABLE) {
            String errorMessage = String.format("Error occurs while ordering: %s", result.toString());
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
        stub.putStringState(uid, asset.toJson());

        return asset.toJson();
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String TransferAsset(final Context ctx, final String uid) {
        ChaincodeStub stub = ctx.getStub();
        String json = stub.getStringState(uid);

        if (json == null || json.isEmpty()) {
            String errorMessage = String.format("Asset %s does not exist", uid);
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Asset asset = AssetFactory.fromJson(json);
        AssetStatus result = asset.transferAsset(ctx.getClientIdentity().getMSPID());
        if (result == AssetStatus.FORBIDDEN ||
                result == AssetStatus.INVALID ||
                result == AssetStatus.NOT_AVAILABLE) {
            String errorMessage = String.format("Error occurs while ordering: %s", result.toString());
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
        stub.putStringState(uid, asset.toJson());

        return asset.toJson();
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String DiscardAsset(final Context ctx, final String uid) {
        ChaincodeStub stub = ctx.getStub();
        String json = stub.getStringState(uid);

        if (json == null || json.isEmpty()) {
            String errorMessage = String.format("Asset %s does not exist", uid);
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Asset asset = AssetFactory.fromJson(json);
        AssetStatus result = asset.discardAsset(ctx.getClientIdentity().getMSPID());
        if (result == AssetStatus.FORBIDDEN ||
                result == AssetStatus.INVALID ||
                result == AssetStatus.NOT_AVAILABLE) {
            String errorMessage = String.format("Error occurs while ordering: %s", result.toString());
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
        stub.putStringState(uid, asset.toJson());

        return asset.toJson();
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String ConfirmDiscardAsset(final Context ctx, final String uid) {
        ChaincodeStub stub = ctx.getStub();
        String json = stub.getStringState(uid);

        if (json == null || json.isEmpty()) {
            String errorMessage = String.format("Asset %s does not exist", uid);
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Asset asset = AssetFactory.fromJson(json);
        AssetStatus result = asset.confirmDiscardAsset(ctx.getClientIdentity().getMSPID());
        if (result == AssetStatus.FORBIDDEN ||
                result == AssetStatus.INVALID ||
                result == AssetStatus.NOT_AVAILABLE) {
            String errorMessage = String.format("Error occurs while ordering: %s", result.toString());
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
        stub.putStringState(uid, asset.toJson());

        return asset.toJson();
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String ConfirmReceiveAsset(final Context ctx, final String uid) {
        ChaincodeStub stub = ctx.getStub();
        String json = stub.getStringState(uid);

        if (json == null || json.isEmpty()) {
            String errorMessage = String.format("Asset %s does not exist", uid);
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Asset asset = AssetFactory.fromJson(json);
        AssetStatus result = asset.confirmReceiveAsset(ctx.getClientIdentity().getMSPID());
        if (result == AssetStatus.FORBIDDEN ||
                result == AssetStatus.INVALID ||
                result == AssetStatus.NOT_AVAILABLE) {
            String errorMessage = String.format("Error occurs while ordering: %s", result.toString());
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
        stub.putStringState(uid, asset.toJson());

        return asset.toJson();
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void DeleteAsset(final Context ctx, final String uid) {
        ChaincodeStub stub = ctx.getStub();
        String json = stub.getStringState(uid);

        if (json == null || json.isEmpty()) {
            String errorMessage = String.format("Asset %s does not exist", uid);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Asset asset = AssetFactory.fromJson(json);
        String id = ctx.getClientIdentity().getMSPID();
        AssetStatus result = asset.requestDelete(id);
        if (result == AssetStatus.APPROVED) {
            stub.delState(uid);
        } else {
            String err = String.format("MSP %s have no permission to delete this with error %s.", id, result.toString());
            System.out.println(err);
            throw new ChaincodeException(err);
        }
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AssetExists(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetID);

        return (assetJSON != null && !assetJSON.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllAssets(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Asset> queryResults = new ArrayList<>();

        // To retrieve all assets from the ledger use getStateByRange with empty startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
        // As another example, if you use startKey = 'asset0', endKey = 'asset9' ,
        // then getStateByRange will retrieve asset with keys between asset0 (inclusive) and asset9 (exclusive) in lexical order.
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            Asset asset = AssetFactory.fromJson(result.getStringValue());
            queryResults.add(asset);
            System.out.println(asset.toString());
        }

        return new GsonBuilder().create().toJson(queryResults);
    }
}
