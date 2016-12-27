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

package com.jrummyapps.android.content;

import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Callback that is invoked when a view is newly created.
 */
public interface ObservableViewFactory {

  /**
   * <p>Perform operations on a newly created view.</p>
   *
   * <p>Called immediately after
   * {@link LayoutInflater#onCreateView(String, AttributeSet)} or
   * {@link LayoutInflater#onCreateView(View, String, AttributeSet)}
   * has returned a non-null view.</p>
   *
   * @param view
   *     The newly created view.
   * @param attrs
   *     A read-only set of tag attributes.
   * @return The view.
   */
  @NonNull View onViewCreated(@NonNull View view, @NonNull AttributeSet attrs);

}
