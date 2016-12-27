/*
 * Copyright (C) 2016 JRummy Apps Inc.
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

package com.jrummyapps.observableinflator.demo;

import android.content.Context;
import android.graphics.Typeface;
import android.util.LruCache;

/**
 * Utility methods for getting a typeface from assets or from a file.
 */
public class TypefaceUtils {

  private static final LruCache<String, Typeface> CACHE = new LruCache<>(15);

  /**
   * Get a typeface from assets
   *
   * @param path
   *     the filename of the font in the assets directory.
   * @return The typeface
   */
  public static Typeface get(Context context, String path) {
    synchronized (CACHE) {
      Typeface typeface = CACHE.get(path);
      if (typeface == null) {
        try {
          typeface = Typeface.createFromAsset(context.getAssets(), path);
        } catch (Throwable t) {
          return null;
        }
        CACHE.put(path, typeface);
      }
      return typeface;
    }
  }

  private TypefaceUtils() {
    throw new AssertionError("no instances");
  }

}
