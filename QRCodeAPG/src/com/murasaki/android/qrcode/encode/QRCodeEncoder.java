/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.murasaki.android.qrcode.encode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.murasaki.android.qrcode.Contents;
import com.murasaki.android.qrcode.Intents;
import com.murasaki.android.apg.R;
import com.google.zxing.client.result.AddressBookParsedResult;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.common.BitMatrix;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * This class does the work of decoding the user's request and extracting all the data
 * to be encoded in a barcode.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
final class QRCodeEncoder {

  private static final String TAG = QRCodeEncoder.class.getSimpleName();

  private static final int WHITE = 0xFFFFFFFF;
  private static final int BLACK = 0xFF000000;

  private final Activity activity;
  private String contents;
  private String displayContents;
  private String title;
  private BarcodeFormat format;
  private final int dimension;

  QRCodeEncoder(Activity activity, Intent intent, int dimension) {
    this.activity = activity;
    if (intent == null) {
      throw new IllegalArgumentException("No valid data to encode.");
    }

    String action = intent.getAction();
    if (action.equals(Intents.Encode.ACTION)) {
      if (!encodeContentsFromZXingIntent(intent)) {
        throw new IllegalArgumentException("No valid data to encode.");
      }
    }
    this.dimension = dimension;
  }

  public String getContents() {
    return contents;
  }

  public String getDisplayContents() {
    return displayContents;
  }

  public String getTitle() {
    return title;
  }

  // It would be nice if the string encoding lived in the core ZXing library,
  // but we use platform specific code like PhoneNumberUtils, so it can't.
  private boolean encodeContentsFromZXingIntent(Intent intent) {
     // Default to QR_CODE if no format given.
    String formatString = intent.getStringExtra(Intents.Encode.FORMAT);
    try {
      format = BarcodeFormat.valueOf(formatString);
    } catch (IllegalArgumentException iae) {
      // Ignore it then
      format = null;
    }
    if (format == null || BarcodeFormat.QR_CODE.equals(format)) {
      String type = intent.getStringExtra(Intents.Encode.TYPE);
      if (type == null || type.length() == 0) {
        return false;
      }
      this.format = BarcodeFormat.QR_CODE;
      encodeQRCodeContents(intent, type);
    } else {
      String data = intent.getStringExtra(Intents.Encode.DATA);
      if (data != null && data.length() > 0) {
        contents = data;
        displayContents = data;
        title = activity.getString(R.string.contents_text);
      }
    }
    return contents != null && contents.length() > 0;
  }
  
  private boolean encodeContentsFromShareIntentPlainText(Intent intent) {
    // Notice: Google Maps shares both URL and details in one text, bummer!
    contents = intent.getStringExtra(Intent.EXTRA_TEXT);
    // We only support non-empty and non-blank texts.
    // Trim text to avoid URL breaking.
    if (contents == null) {
      return false;
    }
    contents = contents.trim();
    if (contents.length() == 0) {
      return false;
    }
    // We only do QR code.
    format = BarcodeFormat.QR_CODE;
    // TODO: Consider using the Intent.EXTRA_SUBJECT or Intent.EXTRA_TITLE
    displayContents = contents;
    title = activity.getString(R.string.contents_text);
    return true;
  }

  private void encodeQRCodeContents(Intent intent, String type) 
  {
    String data = intent.getStringExtra(Intents.Encode.DATA);
    if (data != null && data.length() > 0) 
    {
       contents = data;
       displayContents = data;
       title = activity.getString(R.string.contents_text);
    }
  }

  Bitmap encodeAsBitmap() throws WriterException {
    Hashtable<EncodeHintType,Object> hints = null;
    String encoding = guessAppropriateEncoding(contents);
    if (encoding != null) {
      hints = new Hashtable<EncodeHintType,Object>(2);
      hints.put(EncodeHintType.CHARACTER_SET, encoding);
    }
    MultiFormatWriter writer = new MultiFormatWriter();    
    BitMatrix result = writer.encode(contents, format, dimension, dimension, hints);
    int width = result.getWidth();
    int height = result.getHeight();
    int[] pixels = new int[width * height];
    // All are 0, or black, by default
    for (int y = 0; y < height; y++) {
      int offset = y * width;
      for (int x = 0; x < width; x++) {
        pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
      }
    }

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
    return bitmap;
  }

  private static String guessAppropriateEncoding(CharSequence contents) {
    // Very crude at the moment
    for (int i = 0; i < contents.length(); i++) {
      if (contents.charAt(i) > 0xFF) {
        return "UTF-8";
      }
    }
    return null;
  }


}
