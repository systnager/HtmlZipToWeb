package com.bohdan2505.htmlziptoweb

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bohdan2505.htmlziptoweb.databinding.FragmentSecondBinding
import com.google.android.material.snackbar.Snackbar
import java.io.FileOutputStream

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    private lateinit var currentPhotoUri: Uri
    private lateinit var contentResolver: ContentResolver
    private val FILE_CHOOSER_REQUEST_CODE = 132
    private val FILE_WRITE_REQUEST_CODE = 1001
    private lateinit var layoutJSON: String

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        contentResolver = context?.applicationContext?.contentResolver!!
        val REQUEST_CODE_PERMISSION_ACCESS_FINE_LOCATION = 100
        activity?.let {
            ActivityCompat.requestPermissions(
                it, arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_PERMISSION_ACCESS_FINE_LOCATION
            )
        }

        initializeWebView()

        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebView() {
        val pathToHtml = arguments?.getString("html_path").toString()
        val mapWebView: WebView = binding.root.findViewById(R.id.map_web_view)
        val webSettings: WebSettings = mapWebView.settings
        mapWebView.settings.javaScriptEnabled = true
        mapWebView.settings.setGeolocationEnabled(true)
        webSettings.allowFileAccess = true
        mapWebView.settings.databaseEnabled = true
        mapWebView.settings.domStorageEnabled = true
        mapWebView.addJavascriptInterface(this, "IntegratedWebJSInterface")
        webSettings.setGeolocationEnabled(true)
        webSettings.setGeolocationDatabasePath(context?.filesDir?.path)
        mapWebView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback
            ) {
                callback.invoke(origin, true, false)
            }
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileUploadCallback?.onReceiveValue(null)
                fileUploadCallback = filePathCallback
                if (fileChooserParams?.acceptTypes?.contains("*/*") == true && fileChooserParams.isCaptureEnabled) {
                } else {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "*/*"
                    val chooserIntent = Intent.createChooser(intent, "Choose File")
                    startActivityForResult(chooserIntent, FILE_CHOOSER_REQUEST_CODE)
                }

                return true
            }
        }

        mapWebView.webViewClient = WebViewClient()
        mapWebView.loadUrl("file:///$pathToHtml")
        // mapWebView.loadUrl("file:///android_asset/index.html")
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (fileUploadCallback == null) {
                super.onActivityResult(requestCode, resultCode, data)
                return
            }

            val results: Array<Uri>? = when {
                resultCode == AppCompatActivity.RESULT_OK && data?.data != null -> arrayOf(data.data!!)
                resultCode == AppCompatActivity.RESULT_OK -> arrayOf(currentPhotoUri)
                else -> null
            }

            fileUploadCallback?.onReceiveValue(results)
            fileUploadCallback = null
        }

        if (requestCode == FILE_WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                context?.contentResolver?.openFileDescriptor(uri, "w")?.use { parcelFileDescriptor ->
                    FileOutputStream(parcelFileDescriptor.fileDescriptor).use { fileOutputStream ->
                        fileOutputStream.write(layoutJSON.toByteArray())
                        fileOutputStream.close()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        (activity as? AppCompatActivity)?.supportActionBar?.show()
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        super.onResume()
    }

    //JavaScriptMethod for save text file to user filesystem without access to filesystem
    @JavascriptInterface
    fun requestDirectory(fileName: String, fileContent: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_TITLE, fileName)
        layoutJSON = fileContent
        startActivityForResult(intent, FILE_WRITE_REQUEST_CODE)
    }
}

internal class MyClient : WebChromeClient() {
    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback
    ) {
        callback.invoke(origin, true, false)
    }


}
