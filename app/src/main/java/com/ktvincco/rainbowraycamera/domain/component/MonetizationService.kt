package com.ktvincco.rainbowraycamera.domain.component


import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.ktvincco.rainbowraycamera.AppSettings
import com.ktvincco.rainbowraycamera.data.DataSaver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MonetizationService (
    private val mainActivity: Activity,
    private val dataSaver: DataSaver
) {


    // Settings


    companion object {
        const val LOG_TAG = "MonetizationService"
        const val loadAfterShowTimeoutMs: Long = 500
        const val loadAfterErrorTimeoutMs: Long = 80000
    }
    private val isDisableAdAfterFullVersionPurchase = true


    // Variables


    // Components
    private val appSettings = AppSettings()
    // AD
    private var isAdReady = false
    private var rewardedAd: RewardedAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var onAdShowedCallback: (
        isSuccess: Boolean, adShowException: String) -> Unit = { _, _ ->}
    // Purchases
    private var isBillingReady = false
    private lateinit var billingClient: BillingClient
    private var productsDetails: ProductDetailsResult? = null
    private var purchaseCallback: () -> Unit = {}


    // ADmob Rewarded AD


    private fun reloadAdmobRewardedAdAfterShow() {
        // Wait and show AD
        Handler(Looper.getMainLooper()).postDelayed(
            { loadAdmobRewardedAd() }, loadAfterShowTimeoutMs
        )
    }


    private fun reloadAdmobRewardedAdAfterError() {
        // Wait and show AD
        Handler(Looper.getMainLooper()).postDelayed(
            { loadAdmobRewardedAd() }, loadAfterErrorTimeoutMs
        )
    }


    @SuppressLint("VisibleForTests")
    private fun loadAdmobRewardedAd() {

        // Reset variables
        rewardedAd = null

        // Get AD Unit ID
        val adUnitId = if (!appSettings.getIsEnableAdTestMode()) {
            appSettings.getAdmobRewardedAdUnitId() }
            else { appSettings.getTestAdmobRewardedAdUnitId() }

        // Request and load new AD
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(mainActivity, adUnitId,
            adRequest, object : RewardedAdLoadCallback() {

                // Success load
                override fun onAdLoaded(ad: RewardedAd) {

                    // Assign variables
                    rewardedAd = ad
                    // Log
                    Log.i(LOG_TAG, "onAdLoaded")
                    // Set state
                    isAdReady = true

                    // Assign show result callbacks
                    rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                        // Error show
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            super.onAdFailedToShowFullScreenContent(adError)
                            // Log
                            Log.e(
                                LOG_TAG, "onAdFailedToShowFullScreenContent: " +
                                    adError.message)
                            // Return result
                            onAdShowedCallback(false, "UnexpectedError")
                            // Load next AD
                            reloadAdmobRewardedAdAfterShow()
                        }

                        // Success show
                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            // Log
                            Log.i(LOG_TAG, "onAdShowedFullScreenContent")
                            // Set state
                            isAdReady = false
                            // Return result
                            onAdShowedCallback(true, "")
                            // Load next AD
                            reloadAdmobRewardedAdAfterShow()
                        }

                        // Show ended
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            // Log
                            Log.i(LOG_TAG, "onAdDismissedFullScreenContent")
                        }
                    }
                }

                // Failed to load AD
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // Log
                    Log.e(LOG_TAG, "onAdFailedToLoad R : " + adError.message)
                    // Load next AD
                    reloadAdmobRewardedAdAfterError()
                }
        })
    }


    // ADmob Rewarded Interstitial AD


    private fun reloadAdmobRewardedInterstitialAfterShow() {
        // Wait and load AD
        Handler(Looper.getMainLooper()).postDelayed(
            { loadAdmobRewardedInterstitialAd() }, loadAfterShowTimeoutMs
        )
    }

    private fun reloadAdmobRewardedInterstitialAfterError() {
        // Wait and load AD
        Handler(Looper.getMainLooper()).postDelayed(
            { loadAdmobRewardedInterstitialAd() }, loadAfterErrorTimeoutMs
        )
    }

    @SuppressLint("VisibleForTests")
    private fun loadAdmobRewardedInterstitialAd() {
        // Reset variables
        rewardedInterstitialAd = null

        // Get AD Unit ID
        val adUnitId = if (!appSettings.getIsEnableAdTestMode()) {
            appSettings.getAdmobRewardedInterstitialAdUnitId() }
            else { appSettings.getTestAdmobRewardedInterstitialAdUnitId() }

        // Request and load new AD
        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(mainActivity, adUnitId, adRequest,
            object : RewardedInterstitialAdLoadCallback() {
            // Success load
            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                // Assign variables
                rewardedInterstitialAd = ad
                // Log
                Log.i(LOG_TAG, "onAdLoaded")
                // Set state
                isAdReady = true

                // Assign show result callbacks
                rewardedInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    // Error show
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        super.onAdFailedToShowFullScreenContent(adError)
                        // Log
                        Log.e(
                            LOG_TAG, "onAdFailedToShowFullScreenContent: " +
                                    adError.message
                        )
                        // Return result
                        onAdShowedCallback(false, "UnexpectedError")
                        // Load next AD
                        reloadAdmobRewardedInterstitialAfterShow()
                    }

                    // Success show
                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        // Log
                        Log.i(LOG_TAG, "onAdShowedFullScreenContent")
                        // Set state
                        isAdReady = false
                        // Return result
                        onAdShowedCallback(true, "")
                        // Load next AD
                        reloadAdmobRewardedInterstitialAfterShow()
                    }

                    // Show ended
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        // Log
                        Log.i(LOG_TAG, "onAdDismissedFullScreenContent")
                    }
                }
            }

            // Failed to load AD
            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Log
                Log.e(LOG_TAG, "onAdFailedToLoad RI: " + adError.message)
                // Load next AD
                reloadAdmobRewardedInterstitialAfterError()
            }
        })
    }


    // Advertisement


    private fun initializeAdvertisementClient() {

        // Log
        Log.i(LOG_TAG, "initializeAdvertisementClient")

        // Initialize admob
        MobileAds.initialize(mainActivity) {
            // After initialization, load AD
            val adUnit = appSettings.getRewardedAdUnit()
            if (adUnit == "AdmobRewarded") { loadAdmobRewardedAd() }
            if (adUnit == "AdmobRewardedInterstitial") { loadAdmobRewardedInterstitialAd() }
        }
    }


    // Billing


    // Process purchases state and state changes
    private fun processPurchaseState(productId: String, purchaseState: Int) {

        when (productId) {

            "functionality_package_full" -> {
                if (purchaseState == PurchaseState.PURCHASED) {
                    dataSaver.saveBooleanByKey(
                        "is_functionality_package_full_purchased", true)
                    purchaseCallback()
                }

                if (purchaseState == PurchaseState.UNSPECIFIED_STATE) {
                    dataSaver.saveBooleanByKey(
                        "is_functionality_package_full_purchased", false)
                    purchaseCallback()
                }
            }

            else -> {

            }
        }
    }


    // Receive product updates from store API and process in handlePurchase
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode ==
                BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.skus.size > 0) {
                    processPurchaseState(purchase.skus[0], purchase.purchaseState)
                }
            }
        }
    }


    // Receive product updates from store API and process in handlePurchase
    private val purchasesResponseListener = PurchasesResponseListener()
    { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            for (purchase in purchases) {
                if (purchase.skus.size > 0) {
                    processPurchaseState(purchase.skus[0], purchase.purchaseState)
                }
            }
        }
    }


    // Request all purchases for user and process states
    private fun updatePurchases() {
        CoroutineScope(Dispatchers.Default).launch {

            // Make request
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)

            // Request users purchases from server
            billingClient.queryPurchasesAsync(params.build(), purchasesResponseListener)
        }
    }


    private fun makePurchase(productId: String) {

        // Select product from list
        val targetProduct = productsDetails!!.productDetailsList!!.firstOrNull {
            it.productId == productId } ?: return

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(targetProduct).build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList).build()

        // Launch the billing flow (process result in purchasesUpdatedListener)
        billingClient.launchBillingFlow(mainActivity, billingFlowParams)
    }


    suspend fun loadProductsInfo() {

        // Create request about products info
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("functionality_package_full")
                .setProductType(BillingClient.ProductType.INAPP).build()
        )
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        // Request products info
        productsDetails = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params.build())
        }

        // Set state
        isBillingReady = true

        // Update purchases
        updatePurchases()
    }


    private fun initializeBillingClient() {

        // Log
        Log.i(LOG_TAG, "initializeBillingClient")

        // Create billing client
        billingClient = BillingClient.newBuilder(mainActivity)
            .setListener(purchasesUpdatedListener).enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts()
                    .enablePrepaidPlans().build()).build()

        // Take connection to Google Play Services
        billingClient.startConnection(object : BillingClientStateListener {

            // When connection is taken
            override fun onBillingSetupFinished(billingResult: BillingResult) {

                // When connection is OK
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    // Load products
                    CoroutineScope(Dispatchers.Default).launch {
                        loadProductsInfo()
                    }
                }

                // When connection is not OK
                if (billingResult.responseCode !=  BillingClient.BillingResponseCode.OK) {
                    // Log
                    Log.e(LOG_TAG, "EXC: BillingClient req. responseCode != OK")
                }
            }

            // When connection error
            override fun onBillingServiceDisconnected() {
                // Log
                Log.e(LOG_TAG, "onBillingServiceDisconnected")
            }
        })

    }


    // Private (General)


    private fun initializeSystems() {

        // Billing
        initializeBillingClient()

        // Admob
        if (!(isDisableAdAfterFullVersionPurchase &&
            dataSaver.loadBooleanByKey("is_functionality_package_full_purchased") == true)) {
            initializeAdvertisementClient()
        }
    }


    // Public


    fun setupService() {
        Log.i(LOG_TAG, "Setup service")
        initializeSystems()
    }


    fun showRewardedAd(onAdShowed: (isSuccess: Boolean, adShowException: String) -> Unit) {

        // Illegal state EXC
        if (!isAdReady) { onAdShowed(false, "AdNotReady"); return }

        // When AD unavailable
        if (rewardedAd == null && rewardedInterstitialAd == null) {
            onAdShowed(false, "AdUnavailable"); return }

        // Assign callback
        onAdShowedCallback = onAdShowed

        // Show AD
        val adUnit = appSettings.getRewardedAdUnit()
        if (adUnit == "AdmobRewarded") {
            rewardedAd?.show(mainActivity) {} }
        if (adUnit == "AdmobRewardedInterstitial") {
            rewardedInterstitialAd?.show(mainActivity) {} }
    }


    fun getIsFullVersionActive(): Boolean {
        return dataSaver.loadBooleanByKey("is_functionality_package_full_purchased") ?: false
    }


    fun getFullVersionPrice(): String {
        val targetProduct = productsDetails!!.productDetailsList!!.firstOrNull {
            it.productId == "functionality_package_full" } ?: return "Loading"
        return targetProduct.oneTimePurchaseOfferDetails?.formattedPrice ?: "..."
    }


    fun buyFullVersion(newPurchaseCallback: () -> Unit) {
        // Check illegal state EXC
        if (!isBillingReady || dataSaver.loadBooleanByKey(
                "is_functionality_package_full_purchased") == true) { return }

        // Assign
        purchaseCallback = newPurchaseCallback

        // Buy request
        makePurchase("functionality_package_full")
    }

}