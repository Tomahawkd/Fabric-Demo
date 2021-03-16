package io.tomahawkd.blockchain.application.user;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.tomahawkd.blockchain.application.utils.Asset;

import java.util.List;

public enum TransactionHelper {

    INSTANCE;

    public void LoadDemo() throws Exception {
        ConnectionManager.INSTANCE.submit("LoadDemo");
    }

    public Asset CreateAsset(final String uid) throws Exception {
        return Asset.fromJson(ConnectionManager.INSTANCE.submit("CreateAsset", uid));
    }

    public Asset ReadAsset(final String uid) throws Exception {
        return Asset.fromJson(ConnectionManager.INSTANCE.evaluate("ReadAsset", uid));
    }

    public Asset OrderAsset(final String uid, final String originalOwner) throws Exception {
        return Asset.fromJson(ConnectionManager.INSTANCE.submit("OrderAsset", uid, originalOwner));
    }

    public Asset TransferAsset(final String uid) throws Exception {
        return Asset.fromJson(ConnectionManager.INSTANCE.submit("TransferAsset", uid));
    }

    public Asset DiscardAsset(final String uid) throws Exception {
        return Asset.fromJson(ConnectionManager.INSTANCE.submit("DiscardAsset", uid));
    }

    public Asset ConfirmDiscardAsset(final String uid) throws Exception {
        return Asset.fromJson(ConnectionManager.INSTANCE.submit("ConfirmDiscardAsset", uid));
    }

    public Asset ConfirmReceiveAsset(final String uid) throws Exception {
        return Asset.fromJson(ConnectionManager.INSTANCE.submit("ConfirmReceiveAsset", uid));
    }

    public void DeleteAsset(final String uid) throws Exception {
        ConnectionManager.INSTANCE.submit("DeleteAsset", uid);
    }

    public List<Asset> GetAllAssets() throws Exception {
        return new GsonBuilder().create().fromJson(
                ConnectionManager.INSTANCE.evaluate("GetAllAssets"),
                new TypeToken<List<Asset>>() {}.getType()
        );
    }
}
