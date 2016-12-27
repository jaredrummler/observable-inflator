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

import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;

import com.jrummyapps.android.content.decor.Decorator;

/**
 * A {@link ContextWrapper} that provides a {@link ObservableLayoutInflater}.
 */
public class ObservableContextWrapper extends ContextWrapper {

  public static Builder wrap(@NonNull Context context) {
    return new Builder(context);
  }

  private final ObservableViewFactory viewFactory;
  private final Decorator[] decorators;
  private LayoutInflater inflater;

  ObservableContextWrapper(Builder builder) {
    super(builder.context);
    this.viewFactory = builder.viewFactory;
    this.decorators = builder.decorators;
  }

  @Override public Object getSystemService(String name) {
    if (LAYOUT_INFLATER_SERVICE.equals(name)) {
      if (inflater == null) {
        inflater = new ObservableLayoutInflater(this).setViewFactory(viewFactory).setDecorators(decorators);
      }
      return inflater;
    }
    return super.getSystemService(name);
  }

  public static class Builder {

    final Context context;
    Decorator[] decorators;
    ObservableViewFactory viewFactory;

    Builder(@NonNull Context context) {
      this.context = context;
    }

    /**
     * Set the decorators to be applied to newly created views.
     *
     * @param decorators
     *     The {@link Decorator decorator(s)}
     * @return This object for chaining method calls
     */
    public Builder setDecorators(@NonNull Decorator... decorators) {
      this.decorators = decorators;
      return this;
    }

    /**
     * Set the callback to be invoked when a view is newly created.
     *
     * @param viewFactory
     *     the {@link ObservableViewFactory viewFactory}
     * @return This object for chaining method calls
     */
    public Builder setViewFactory(@NonNull ObservableViewFactory viewFactory) {
      this.viewFactory = viewFactory;
      return this;
    }

    /**
     * Create the {@link ObservableContextWrapper}
     *
     * @return The context wrapper with the supplied arguments to the builder.
     */
    public ObservableContextWrapper create() {
      return new ObservableContextWrapper(this);
    }

  }

}
