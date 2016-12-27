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

import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.jrummyapps.android.content.decor.AttrsDecorator;

public class FontDecorator extends AttrsDecorator<TextView> {

  @Override protected void apply(TextView view, TypedArray typedArray) {
    String typefacePath = typedArray.getString(R.styleable.FontDecorator_decorTypefaceAsset);
    if (typefacePath == null) {
      return;
    }
    Typeface typeface = TypefaceUtils.get(view.getContext(), typefacePath);
    view.setTypeface(typeface);
  }

  @Override @NonNull protected Class<TextView> clazz() {
    return TextView.class;
  }

  @Override protected int[] styleable() {
    return R.styleable.FontDecorator;
  }

}
