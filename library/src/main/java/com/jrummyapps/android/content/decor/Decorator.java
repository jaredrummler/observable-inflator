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

package com.jrummyapps.android.content.decor;

import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

/**
 * A class that operates on already constructed views, i.e., decorates them.
 */
public interface Decorator {

  /**
   * Decorates the given view. This method will be called for every {@link View} that is created.
   *
   * @param view
   *     The view to decorate. Never null.
   * @param attrs
   *     A read-only set of tag attributes.
   */
  void apply(@NonNull View view, @NonNull AttributeSet attrs);

}
