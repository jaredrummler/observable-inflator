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

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * A base class for a decorator that transform certain View subtypes with certain attributes. Useful when you want to
 * extend standard layout inflation to add your own attributes to system widgets. If a view with type {@code View&lt;
 * ? extends T&gt;} is inflated and it has one of the attributes returned in {@link AttrsDecorator#styleable()} ()}
 * method then {@link AttrsDecorator#apply(View, TypedArray)} will be invoked for that view.
 *
 * @param <T>
 *     The type or parent type of View that this decorator applies to.
 */
public abstract class AttrsDecorator<T extends View> implements Decorator {

  @Override public void apply(@NonNull View view, @NonNull AttributeSet attributeSet) {
    if (!clazz().isAssignableFrom(view.getClass())) {
      return;
    }

    TypedArray values = obtainAttributes(view.getContext(), attributeSet);
    if (values == null || values.length() == 0) {
      return;
    }

    try {
      for (int i = 0; i < values.length(); i++) {
        TypedValue buf = new TypedValue();
        if (values.hasValue(i) && values.getValue(i, buf)) {
          //noinspection unchecked
          apply((T) view, values);
          break;
        }
      }
    } finally {
      values.recycle();
    }
  }

  /**
   * This method will be called if a View of type T was inflated and it had one of the attributes specified by
   * {@link AttrsDecorator#styleable()} set.
   *
   * @param view
   *     The view object that is being decorated.
   * @param typedArray
   *     A {@link TypedArray} for attributes.
   */
  protected abstract void apply(@NonNull T view, @NonNull TypedArray typedArray);

  /**
   * The class for the given view
   *
   * @return The class/typetoken for T
   */
  @NonNull protected abstract Class<T> clazz();

  /**
   * Attributes supported by this decorator.
   *
   * @return a non-null array of android attr resource ids.
   */
  protected abstract int[] styleable();

  /**
   * The default style specified by <var>defStyleAttr</var>
   *
   * @return An attribute in the current theme that contains a reference to a style resource that supplies defaults
   * values for the TypedArray. Can be 0 to not look for defaults.
   */
  protected int defStyleAttr() {
    return 0;
  }

  /**
   * The style resource specified in the AttributeSet (named "style").
   *
   * @return A resource identifier of a style resource that supplies default values for the TypedArray, used only if
   * defStyleAttr is 0 or can not be found in the theme.  Can be 0 to not look for defaults.
   */
  protected int defStyleRes() {
    return 0;
  }

  /**
   * Get the attributes
   *
   * @param context
   *     the context held by the view
   * @param attributeSet
   *     A read-only set of tag attributes.
   * @return a TypedArray holding an array of the attribute values.
   */
  protected TypedArray obtainAttributes(Context context, AttributeSet attributeSet) {
    return context.getTheme().obtainStyledAttributes(attributeSet, styleable(), defStyleAttr(), defStyleRes());
  }

}