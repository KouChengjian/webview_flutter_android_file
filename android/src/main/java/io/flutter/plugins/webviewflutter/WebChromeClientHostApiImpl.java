// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.webviewflutter;
import static android.app.Activity.RESULT_OK;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import io.flutter.plugins.webviewflutter.GeneratedAndroidWebView.WebChromeClientHostApi;
import java.util.Objects;

/**
 * Host api implementation for {@link WebChromeClient}.
 *
 * <p>Handles creating {@link WebChromeClient}s that intercommunicate with a paired Dart object.
 */
public class WebChromeClientHostApiImpl implements WebChromeClientHostApi {
  private final InstanceManager instanceManager;
  private final WebChromeClientCreator webChromeClientCreator;
  private final WebChromeClientFlutterApiImpl flutterApi;
  private Context context;


  private static ValueCallback<Uri[]> mUploadMessageArray;
  private final static int FILECHOOSER_RESULTCODE = 1;
  private static Uri fileUri;
  private static Uri videoUri;
  public ResultHandler resultHandler = new ResultHandler();

  /**
   * Implementation of {@link WebChromeClient} that passes arguments of callback methods to Dart.
   */
  public static class WebChromeClientImpl extends SecureWebChromeClient {
    private final WebChromeClientFlutterApiImpl flutterApi;
    private boolean returnValueForOnShowFileChooser = false;
    private boolean returnValueForOnConsoleMessage = false;

    private boolean returnValueForOnJsAlert = false;
    private boolean returnValueForOnJsConfirm = false;
    private boolean returnValueForOnJsPrompt = false;

    /**
     * Creates a {@link WebChromeClient} that passes arguments of callbacks methods to Dart.
     *
     * @param flutterApi handles sending messages to Dart
     */
    public WebChromeClientImpl(@NonNull WebChromeClientFlutterApiImpl flutterApi) {
      this.flutterApi = flutterApi;
    }

    @Override
    public void onProgressChanged(@NonNull WebView view, int progress) {
      flutterApi.onProgressChanged(this, view, (long) progress, reply -> {});
    }

    @Override
    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
      flutterApi.onShowCustomView(this, view, callback, reply -> {});
    }

    @Override
    public void onHideCustomView() {
      flutterApi.onHideCustomView(this, reply -> {});
    }

    public void onGeolocationPermissionsShowPrompt(
        @NonNull String origin, @NonNull GeolocationPermissions.Callback callback) {
      flutterApi.onGeolocationPermissionsShowPrompt(this, origin, callback, reply -> {});
    }

    @Override
    public void onGeolocationPermissionsHidePrompt() {
      flutterApi.onGeolocationPermissionsHidePrompt(this, reply -> {});
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("LambdaLast")
    @Override
    public boolean onShowFileChooser(
        @NonNull WebView webView,
        @NonNull ValueCallback<Uri[]> filePathCallback,
        @NonNull FileChooserParams fileChooserParams) {
      final boolean currentReturnValueForOnShowFileChooser = returnValueForOnShowFileChooser;
      flutterApi.onShowFileChooser(
          this,
          webView,
          fileChooserParams,
          reply -> {
            // The returned list of file paths can only be passed to `filePathCallback` if the
            // `onShowFileChooser` method returned true.
            if (currentReturnValueForOnShowFileChooser) {
              final Uri[] filePaths = new Uri[reply.size()];
              for (int i = 0; i < reply.size(); i++) {
                filePaths[i] = Uri.parse(reply.get(i));
              }
              filePathCallback.onReceiveValue(filePaths);
            }
          });
      return currentReturnValueForOnShowFileChooser;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onPermissionRequest(@NonNull PermissionRequest request) {
      flutterApi.onPermissionRequest(this, request, reply -> {});
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
      flutterApi.onConsoleMessage(this, consoleMessage, reply -> {});
      return returnValueForOnConsoleMessage;
    }

    /** Sets return value for {@link #onShowFileChooser}. */
    public void setReturnValueForOnShowFileChooser(boolean value) {
      returnValueForOnShowFileChooser = value;
    }

    /** Sets return value for {@link #onConsoleMessage}. */
    public void setReturnValueForOnConsoleMessage(boolean value) {
      returnValueForOnConsoleMessage = value;
    }

    public void setReturnValueForOnJsAlert(boolean value) {
      returnValueForOnJsAlert = value;
    }

    public void setReturnValueForOnJsConfirm(boolean value) {
      returnValueForOnJsConfirm = value;
    }

    public void setReturnValueForOnJsPrompt(boolean value) {
      returnValueForOnJsPrompt = value;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
      if (returnValueForOnJsAlert) {
        flutterApi.onJsAlert(
            this,
            url,
            message,
            reply -> {
              result.confirm();
            });
        return true;
      } else {
        return false;
      }
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
      if (returnValueForOnJsConfirm) {
        flutterApi.onJsConfirm(
            this,
            url,
            message,
            reply -> {
              if (reply) {
                result.confirm();
              } else {
                result.cancel();
              }
            });
        return true;
      } else {
        return false;
      }
    }

    @Override
    public boolean onJsPrompt(
        WebView view, String url, String message, String defaultValue, JsPromptResult result) {
      if (returnValueForOnJsPrompt) {
        flutterApi.onJsPrompt(
            this,
            url,
            message,
            defaultValue,
            reply -> {
              @Nullable String inputMessage = reply;
              if (inputMessage != null) {
                result.confirm(inputMessage);
              } else {
                result.cancel();
              }
            });
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * Implementation of {@link WebChromeClient} that only allows secure urls when opening a new
   * window.
   */
  public static class SecureWebChromeClient extends WebChromeClient {
    @Nullable WebViewClient webViewClient;

    @Override
    public boolean onCreateWindow(
        @NonNull final WebView view,
        boolean isDialog,
        boolean isUserGesture,
        @NonNull Message resultMsg) {
      return onCreateWindow(view, resultMsg, new WebView(view.getContext()));
    }

    /**
     * Verifies that a url opened by `Window.open` has a secure url.
     *
     * @param view the WebView from which the request for a new window originated.
     * @param resultMsg the message to send when once a new WebView has been created. resultMsg.obj
     *     is a {@link WebView.WebViewTransport} object. This should be used to transport the new
     *     WebView, by calling WebView.WebViewTransport.setWebView(WebView)
     * @param onCreateWindowWebView the temporary WebView used to verify the url is secure
     * @return this method should return true if the host application will create a new window, in
     *     which case resultMsg should be sent to its target. Otherwise, this method should return
     *     false. Returning false from this method but also sending resultMsg will result in
     *     undefined behavior
     */
    @VisibleForTesting
    boolean onCreateWindow(
        @NonNull final WebView view,
        @NonNull Message resultMsg,
        @Nullable WebView onCreateWindowWebView) {
      // WebChromeClient requires a WebViewClient because of a bug fix that makes
      // calls to WebViewClient.requestLoading/WebViewClient.urlLoading when a new
      // window is opened. This is to make sure a url opened by `Window.open` has
      // a secure url.
      if (webViewClient == null) {
        return false;
      }

      final WebViewClient windowWebViewClient =
          new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(
                @NonNull WebView windowWebView, @NonNull WebResourceRequest request) {
              if (!webViewClient.shouldOverrideUrlLoading(view, request)) {
                view.loadUrl(request.getUrl().toString());
              }
              return true;
            }

            // Legacy codepath for < N.
            @Override
            @SuppressWarnings({"deprecation", "RedundantSuppression"})
            public boolean shouldOverrideUrlLoading(WebView windowWebView, String url) {
              if (!webViewClient.shouldOverrideUrlLoading(view, url)) {
                view.loadUrl(url);
              }
              return true;
            }
          };

      if (onCreateWindowWebView == null) {
        onCreateWindowWebView = new WebView(view.getContext());
      }
      onCreateWindowWebView.setWebViewClient(windowWebViewClient);

      final WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
      transport.setWebView(onCreateWindowWebView);
      resultMsg.sendToTarget();

      return true;
    }

    /**
     * Set the {@link WebViewClient} that calls to {@link WebChromeClient#onCreateWindow} are passed
     * to.
     *
     * @param webViewClient the forwarding {@link WebViewClient}
     */
    public void setWebViewClient(@NonNull WebViewClient webViewClient) {
      this.webViewClient = webViewClient;
    }

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
      if (mUploadMessageArray != null) {
        mUploadMessageArray.onReceiveValue(null);
      }
      mUploadMessageArray = filePathCallback;

      final String[] acceptTypes = getSafeAcceptedTypes(fileChooserParams);
      List<Intent> intentList = new ArrayList<Intent>();
      fileUri = null;
      videoUri = null;
      if (acceptsImages(acceptTypes)) {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputFilename(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        intentList.add(takePhotoIntent);
      }
      if (acceptsVideo(acceptTypes)) {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        videoUri = getOutputFilename(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
        intentList.add(takeVideoIntent);
      }
      Intent contentSelectionIntent;
      if (Build.VERSION.SDK_INT >= 21) {
        final boolean allowMultiple = fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE;
        contentSelectionIntent = fileChooserParams.createIntent();
        contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple);
      } else {
        contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("*/*");
      }
      Intent[] intentArray = intentList.toArray(new Intent[intentList.size()]);

      Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
      chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
      chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
      WebViewFlutterPlugin.activity.startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
      return true;
    }
  }

  /** Handles creating {@link WebChromeClient}s for a {@link WebChromeClientHostApiImpl}. */
  public static class WebChromeClientCreator {
    /**
     * Creates a {@link WebChromeClientHostApiImpl.WebChromeClientImpl}.
     *
     * @param flutterApi handles sending messages to Dart
     * @return the created {@link WebChromeClientHostApiImpl.WebChromeClientImpl}
     */
    @NonNull
    public WebChromeClientImpl createWebChromeClient(
        @NonNull WebChromeClientFlutterApiImpl flutterApi) {
      return new WebChromeClientImpl(flutterApi);
    }
  }

  /**
   * Creates a host API that handles creating {@link WebChromeClient}s.
   *
   * @param instanceManager maintains instances stored to communicate with Dart objects
   * @param webChromeClientCreator handles creating {@link WebChromeClient}s
   * @param flutterApi handles sending messages to Dart
   */
  public WebChromeClientHostApiImpl(
      @NonNull InstanceManager instanceManager,
      @NonNull WebChromeClientCreator webChromeClientCreator,
      @NonNull WebChromeClientFlutterApiImpl flutterApi) {
    this.instanceManager = instanceManager;
    this.webChromeClientCreator = webChromeClientCreator;
    this.flutterApi = flutterApi;
  }

  @Override
  public void create(@NonNull Long instanceId) {
    final WebChromeClient webChromeClient =
        webChromeClientCreator.createWebChromeClient(flutterApi);
    instanceManager.addDartCreatedInstance(webChromeClient, instanceId);
  }

  @Override
  public void setSynchronousReturnValueForOnShowFileChooser(
      @NonNull Long instanceId, @NonNull Boolean value) {
    final WebChromeClientImpl webChromeClient =
        Objects.requireNonNull(instanceManager.getInstance(instanceId));
    webChromeClient.setReturnValueForOnShowFileChooser(value);
  }

  @Override
  public void setSynchronousReturnValueForOnConsoleMessage(
      @NonNull Long instanceId, @NonNull Boolean value) {
    final WebChromeClientImpl webChromeClient =
        Objects.requireNonNull(instanceManager.getInstance(instanceId));
    webChromeClient.setReturnValueForOnConsoleMessage(value);
  }

  @Override
  public void setSynchronousReturnValueForOnJsAlert(
      @NonNull Long instanceId, @NonNull Boolean value) {
    final WebChromeClientImpl webChromeClient =
        Objects.requireNonNull(instanceManager.getInstance(instanceId));
    webChromeClient.setReturnValueForOnJsAlert(value);
  }

  @Override
  public void setSynchronousReturnValueForOnJsConfirm(
      @NonNull Long instanceId, @NonNull Boolean value) {
    final WebChromeClientImpl webChromeClient =
        Objects.requireNonNull(instanceManager.getInstance(instanceId));
    webChromeClient.setReturnValueForOnJsConfirm(value);
  }

  @Override
  public void setSynchronousReturnValueForOnJsPrompt(
      @NonNull Long instanceId, @NonNull Boolean value) {
    final WebChromeClientImpl webChromeClient =
        Objects.requireNonNull(instanceManager.getInstance(instanceId));
    webChromeClient.setReturnValueForOnJsPrompt(value);
  }



  private static String[] getSafeAcceptedTypes(WebChromeClient.FileChooserParams params) {

    // the getAcceptTypes() is available only in api 21+
    // for lower level, we ignore it
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      return params.getAcceptTypes();
    }

    final String[] EMPTY = {};
    return EMPTY;
  }

  private static Boolean acceptsImages(String[] types) {
    return isArrayEmpty(types) || arrayContainsString(types, "image");
  }

  private static Boolean isArrayEmpty(String[] arr) {
    // when our array returned from getAcceptTypes() has no values set from the
    // webview
    // i.e. <input type="file" />, without any "accept" attr
    // will be an array with one empty string element, afaik
    return arr.length == 0 || (arr.length == 1 && arr[0].length() == 0);
  }

  private static Boolean arrayContainsString(String[] array, String pattern) {
    for (String content : array) {
      if (content.contains(pattern)) {
        return true;
      }
    }
    return false;
  }

  private static Boolean acceptsVideo(String[] types) {
    return isArrayEmpty(types) || arrayContainsString(types, "video");
  }

  private static Uri getOutputFilename(String intentType) {
    String prefix = "";
    String suffix = "";

    if (intentType == MediaStore.ACTION_IMAGE_CAPTURE) {
      prefix = "image-";
      suffix = ".jpg";
    } else if (intentType == MediaStore.ACTION_VIDEO_CAPTURE) {
      prefix = "video-";
      suffix = ".mp4";
    }

    String packageName = WebViewFlutterPlugin.activity.getApplicationContext().getPackageName();
    File capturedFile = null;
    try {
      capturedFile = createCapturedFile(prefix, suffix);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return FileProvider.getUriForFile(WebViewFlutterPlugin.activity.getApplicationContext(), packageName + ".fileprovider", capturedFile);
  }

  private static File createCapturedFile(String prefix, String suffix) throws IOException {
    @SuppressLint("SimpleDateFormat")
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = prefix + "_" + timeStamp;
    File storageDir = WebViewFlutterPlugin.activity.getApplicationContext().getExternalFilesDir(null);
    return File.createTempFile(imageFileName, suffix, storageDir);
  }

  private static long getFileSize(Uri fileUri) {
    @SuppressLint("Recycle") Cursor returnCursor = WebViewFlutterPlugin.activity.getApplicationContext().getContentResolver()
            .query(fileUri, null, null, null, null);
    returnCursor.moveToFirst();
    int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
    return returnCursor.getLong(sizeIndex);
  }

  private static Uri[] getSelectedFiles(Intent data) {
    // we have one files selected
    if (data.getData() != null) {
      String dataString = data.getDataString();
      if (dataString != null) {
        return new Uri[]{Uri.parse(dataString)};
      }
    }
    // we have multiple files selected
    if (data.getClipData() != null) {
      final int numSelectedFiles = data.getClipData().getItemCount();
      Uri[] result = new Uri[numSelectedFiles];
      for (int i = 0; i < numSelectedFiles; i++) {
        result[i] = data.getClipData().getItemAt(i).getUri();
      }
      return result;
    }
    return null;
  }

  static class ResultHandler {
    public boolean handleResult(int requestCode, int resultCode, Intent intent) {
      boolean handled = false;
      if (Build.VERSION.SDK_INT >= 21) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
          Uri[] results = null;
          if (resultCode == RESULT_OK) {
            if (fileUri != null && getFileSize(fileUri) > 0) {
              results = new Uri[]{fileUri};
            } else if (videoUri != null && getFileSize(videoUri) > 0) {
              results = new Uri[]{videoUri};
            } else if (intent != null) {
              results = getSelectedFiles(intent);
            }
          }
          if (mUploadMessageArray != null) {
            mUploadMessageArray.onReceiveValue(results);
            mUploadMessageArray = null;
          }
          handled = true;
        }
      } else {
//        if (requestCode == FILECHOOSER_RESULTCODE) {
//          Uri result = null;
//          if (resultCode == RESULT_OK && intent != null) {
//            result = intent.getData();
//          }
//          if (mUploadMessage != null) {
//            mUploadMessage.onReceiveValue(result);
//            mUploadMessage = null;
//          }
//          handled = true;
//        }
      }
      return handled;
    }
  }
}
