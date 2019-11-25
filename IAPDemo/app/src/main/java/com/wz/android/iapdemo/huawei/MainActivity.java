package com.wz.android.iapdemo.huawei;

import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.wz.android.iapdemo.huawei.CipherUtil;
import com.wz.android.iapdemo.huawei.Key;
import com.wz.android.iapdemo.huawei.R;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.support.api.client.Status;
import com.huawei.hms.support.api.entity.iap.ConsumePurchaseReq;
import com.huawei.hms.support.api.entity.iap.GetBuyIntentReq;
import com.huawei.hms.support.api.entity.iap.OrderStatusCode;
import com.huawei.hms.support.api.entity.iap.SkuDetail;
import com.huawei.hms.support.api.entity.iap.SkuDetailReq;
import com.huawei.hms.support.api.iap.BuyResultInfo;
import com.huawei.hms.support.api.iap.ConsumePurchaseResult;
import com.huawei.hms.support.api.iap.GetBuyIntentResult;
import com.huawei.hms.support.api.iap.SkuDetailResult;
import com.huawei.hms.support.api.iap.json.Iap;
import com.huawei.hms.support.api.iap.json.IapApiException;
import com.huawei.hms.support.api.iap.json.IapClient;

import com.huawei.hms.support.api.iap.json.Iap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    ListView listView;
    public static final String TAG = "IAPDemo";

    public static final int PRODUCT_TYPE_CONSUMABLE = 0;

    public static final int REQ_CODE_BUY = 4002;


    private String item_desc = "DESC";
    private String item_price = "PRICE";
    private String item_skuid = "SKUID";
    private String item_image = "IMAGE";
    private List<HashMap<String, Object>> products = new ArrayList<HashMap<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initProduct();
    }

    /**
     * * Initialize products information and show the products
    */

    private void initProduct() {
        listView = (ListView)findViewById(R.id.itemlist);
        // obtain in-app product details configured in AppGallery Connect, and then show the products
        IapClient iapClient = Iap.getIapClient(MainActivity.this);


        Task<SkuDetailResult> task = iapClient.getSkuDetail(createSkuDetailReq());
        task.addOnSuccessListener(new OnSuccessListener<SkuDetailResult>() {
            @Override
            public void onSuccess(SkuDetailResult result) {
                if (result != null && !result.getSkuList().isEmpty()) {
                    showProduct(result.getSkuList());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static SkuDetailReq createSkuDetailReq() {
        SkuDetailReq skuDetailRequest = new SkuDetailReq();
        skuDetailRequest.priceType = PRODUCT_TYPE_CONSUMABLE;
        ArrayList<String> skuList = new ArrayList<>();
        skuList.add("pd1");
        skuList.add("pd2");
        skuDetailRequest.skuIds = skuList;
        return skuDetailRequest;
    }

    /**
     * to show the products
     * @param skuDetailList Product list
     */
    private void showProduct(List<SkuDetail> skuDetailList) {
        for (SkuDetail skuDetail : skuDetailList) {
            HashMap<String, Object> item1 = new HashMap<String, Object>();
            item1.put(item_desc, skuDetail.productDesc);
            item1.put(item_price, skuDetail.price);
            item1.put(item_skuid, skuDetail.productId);
            item1.put(item_image, R.drawable.blue_ball);
            products.add(item1);
        }
        SimpleAdapter simAdapter = new SimpleAdapter(
                MainActivity.this, products, R.layout.item_layout,
                new String[]{item_image, item_desc, item_price}, new int[]{
                R.id.item_image, R.id.item_desc, R.id.item_price});
        listView.setAdapter(simAdapter);
        simAdapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                String product = (String) products.get(pos).get(item_skuid);
                getBuyIntent(MainActivity.this, product, PRODUCT_TYPE_CONSUMABLE);
            }
        });

    }
    /**
     * create orders for in-app products in the PMS
     * @param activity indicates the activity object that initiates a request.
     * @param skuId ID list of products to be queried. Each product ID must exist and be unique in the current app.
     * @param type  In-app product type.
     */
    private void getBuyIntent(final Activity activity, String skuId, int type) {
        Log.d(TAG, "call getBuyIntent");
        IapClient mClient = Iap.getIapClient(activity);
        Task<GetBuyIntentResult> task = mClient.getBuyIntent(createGetBuyIntentReq(type, skuId));
        task.addOnSuccessListener(new OnSuccessListener<GetBuyIntentResult>() {
            @Override
            public void onSuccess(GetBuyIntentResult result) {
                Log.d(TAG, "getBuyIntent, onSuccess");
                if (result == null) {
                    Log.d(TAG, "result is null");
                    return;
                }
                Status status = result.getStatus();
                if (status == null) {
                    Log.d(TAG, "status is null");
                    return;
                }
                // you should pull up the page to complete the payment process
                if (status.hasResolution()) {
                    try {
                        status.startResolutionForResult(activity, REQ_CODE_BUY);
                    } catch (IntentSender.SendIntentException exp) {
                        Log.e(TAG, exp.getMessage());
                    }
                } else {
                    Log.e(TAG, "intent is null");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException)e;
                    int returnCode = apiException.getStatusCode();
                    Log.d(TAG, "getBuyIntent, returnCode: " + returnCode);
                    // handle error scenarios
                } else {
                    // Other external errors
                    Log.e(TAG, e.getMessage());
                }

            }
        });
    }

    /**
     * Create a GetBuyIntentReq request
     * @param type In-app product type.
     * @param skuId ID of the in-app product to be paid.
     *              The in-app product ID is the product ID you set during in-app product configuration in AppGallery Connect.
     * @return GetBuyIntentReq
     */
    private GetBuyIntentReq createGetBuyIntentReq(int type, String skuId) {
        GetBuyIntentReq request = new GetBuyIntentReq();
        request.productId = skuId;
        request.priceType = type;
        request.developerPayload = "test";
        return request;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_BUY) {
            if (data != null) {
                BuyResultInfo buyResultInfo = Iap.getIapClient(this).getBuyResultInfoFromIntent(data);
                if (buyResultInfo.getReturnCode() == OrderStatusCode.ORDER_STATE_CANCEL) {
                    // user cancels payment
                    Toast.makeText(this, "user cancel", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (buyResultInfo.getReturnCode() == OrderStatusCode.ORDER_ITEM_ALREADY_OWNED) {
                    // item already owned
                    Toast.makeText(this, "you have owned the product", Toast.LENGTH_SHORT).show();
                    // you can check if the user has purchased the product and decide whether to provide goods
                    // if the purchase is a consumable product, consuming the purchase and deliver product
                    return;
                }
                if (buyResultInfo.getReturnCode() == OrderStatusCode.ORDER_STATE_SUCCESS) {
                    // verify signature of payment results
                    boolean success = CipherUtil.doCheck(buyResultInfo.getInAppPurchaseData(), buyResultInfo.getInAppDataSignature(), Key.getPublicKey());
                    if (success) {
                        // call the consumption interface to consume it after delivering the product to your user
                        consumePurchase(this, buyResultInfo.getInAppPurchaseData(), buyResultInfo.getInAppDataSignature());
                    } else {
                        Toast.makeText(this, "Pay successful,sign failed", Toast.LENGTH_SHORT).show();
                    }
                    return;
                } else {
                    Toast.makeText(this, "Pay failed", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Log.i(TAG, "data is null");
            }
            return;
        }

    }

    /**
     * consume the unconsumed purchase with type 0
     * @param inAppPurchaseData JSON string that contains purchase order details.
     * @param inAppSignature signature of inAppPurchaseData
     */
    private void consumePurchase(final Context context, String inAppPurchaseData, String inAppSignature) {
        Log.i(TAG, "call consumePurchase");
        IapClient mClient = Iap.getIapClient(context);
        // verify signature of inAppPurchaseDataList
        boolean success = CipherUtil.doCheck(inAppPurchaseData, inAppSignature, Key.getPublicKey());
        if (success) {
            Log.i(TAG, "verify success");
            Task<ConsumePurchaseResult> task = mClient.consumePurchase(createConsumePurchaseReq(inAppPurchaseData));
            task.addOnSuccessListener(new OnSuccessListener<ConsumePurchaseResult>() {
                @Override
                public void onSuccess(ConsumePurchaseResult result) {
                    // Consume success
                    Log.i(TAG, "consumePurchase success");
                    Toast.makeText(context, "Pay success, and the product has been delivered", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (e instanceof IapApiException) {
                        IapApiException apiException = (IapApiException)e;
                        int returnCode = apiException.getStatusCode();
                        Log.i(TAG, "consumePurchase fail,returnCode: " + returnCode);
                    } else {
                        // Other external errors
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(context, context.getString(R.string.external_error), Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    /**
     * Create a ConsumePurchaseReq request
     * @param purchaseData which is generated by the Huawei payment server during product payment and returned to the app through InAppPurchaseData.
     *                     The app transfers this parameter for the Huawei payment server to update the order status and then deliver the in-app product.
     * @return ConsumePurchaseReq
     */
    private ConsumePurchaseReq createConsumePurchaseReq(String purchaseData) {
        ConsumePurchaseReq consumePurchaseRequest = new ConsumePurchaseReq();
        String purchaseToken = "";
        try {
            JSONObject jsonObject = new JSONObject(purchaseData);
            purchaseToken = jsonObject.optString("purchaseToken");
        } catch (JSONException e) {

        }
        consumePurchaseRequest.purchaseToken = purchaseToken;
        return consumePurchaseRequest;
    }



}
