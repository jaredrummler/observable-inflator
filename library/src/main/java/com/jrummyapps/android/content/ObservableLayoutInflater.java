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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jrummyapps.android.content.decor.Decorator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A {@link LayoutInflater} which takes a {@link ObservableViewFactory} to operate on newly constructed views.
 */
public class ObservableLayoutInflater extends LayoutInflater {

  // ------------------------------------------------------------------------------------------------------------------
  // Much of this code was derived from:
  //   - https://github.com/chrisjenx/Calligraphy
  //   - https://github.com/chemouna/Decor
  // ------------------------------------------------------------------------------------------------------------------

  private static final String[] ANDROID_WIDGET_PREFIXES = {"android.widget.", "android.webkit."};

  private ObservableViewFactory viewFactory;
  private Decorator[] decorators;
  private boolean setPrivateFactory;

  public ObservableLayoutInflater(ContextWrapper context) {
    this(LayoutInflater.from(context.getBaseContext()), context);
  }

  public ObservableLayoutInflater(LayoutInflater original, Context context) {
    this(original, context, false);
  }

  ObservableLayoutInflater(LayoutInflater original, Context context, boolean cloned) {
    super(original, context);
    if (!cloned) {
      if (getFactory2() != null && !(getFactory2() instanceof WrapperFactory2)) {
        setFactory2(getFactory2());
      }
      if (getFactory() != null && !(getFactory() instanceof WrapperFactory)) {
        setFactory(getFactory());
      }
    }
  }

  @Override public LayoutInflater cloneInContext(Context newContext) {
    return new ObservableLayoutInflater(this, newContext, true).setViewFactory(viewFactory).setDecorators(decorators);
  }

  @Override public View inflate(int resource, ViewGroup root, boolean attachToRoot) {
    setPrivateFactoryInternal();
    return super.inflate(resource, root, attachToRoot);
  }

  @Override public void setFactory(Factory factory) {
    if (!(factory instanceof WrapperFactory)) {
      super.setFactory(new WrapperFactory(this, factory));
    } else {
      super.setFactory(factory);
    }
  }

  @Override public void setFactory2(Factory2 factory2) {
    if (!(factory2 instanceof WrapperFactory2)) {
      super.setFactory2(new WrapperFactory2(this, factory2));
    } else {
      super.setFactory2(factory2);
    }
  }

  @Override protected View onCreateView(View parent, String name, AttributeSet attrs) throws ClassNotFoundException {
    return observeAndCreateView(super.onCreateView(parent, name, attrs), attrs);
  }

  @Override protected View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
    View view = null;
    for (String prefix : ANDROID_WIDGET_PREFIXES) {
      try {
        view = createView(name, prefix, attrs);
        if (view != null) {
          break;
        }
      } catch (ClassNotFoundException ignored) {
      }
    }
    if (view == null) {
      view = super.onCreateView(name, attrs);
    }
    return observeAndCreateView(view, attrs);
  }

  /**
   * Set the callback to be invoked when a view is newly created.
   *
   * @param viewFactory
   *     the {@link ObservableViewFactory viewFactory}
   * @return This object for chaining method calls
   */
  public ObservableLayoutInflater setViewFactory(ObservableViewFactory viewFactory) {
    this.viewFactory = viewFactory;
    return this;
  }

  /**
   * Set the decorators to be applied to newly created views.
   *
   * @param decorators
   *     The {@link Decorator decorator(s)}
   * @return This object for chaining method calls
   */
  public ObservableLayoutInflater setDecorators(Decorator... decorators) {
    this.decorators = decorators;
    return this;
  }

  /**
   * Method to dispatch our view and attributes to the {@link ObservableViewFactory}. Called immediately after
   * {@link #onCreateView(String, AttributeSet)} or {@link #onCreateView(View, String, AttributeSet)}
   *
   * @param view
   *     The view being inflated
   * @param attrs
   *     The attributes for the view
   * @return Newly created view
   */
  View observeAndCreateView(View view, AttributeSet attrs) {
    if (view == null) {
      return null;
    }
    if (decorators != null) {
      for (Decorator decorator : decorators) {
        decorator.apply(view, attrs);
      }
    }
    if (viewFactory != null) {
      return viewFactory.onViewCreated(view, attrs);
    }
    return view;
  }

  /**
   * Method to inflate custom layouts that haven't been handled else where. If this fails it will fall back
   * through to the PhoneLayoutInflater method of inflating custom views where we will NOT have a hook into.
   *
   * @param parent
   *     parent view
   * @param view
   *     view if it has been inflated by this point, if this is not null this method just returns this value.
   * @param name
   *     name of the thing to inflate.
   * @param viewContext
   *     Context to inflate by if parent is null
   * @param attrs
   *     Attr for this view which we can steal fontPath from too.
   * @return view or the View we inflate in here.
   */
  View createCustomView(View parent, View view, String name, Context viewContext, AttributeSet attrs) {
    // I by no means advise anyone to do this normally, but Google has locked down access to the createView() method,
    // so we never get a callback with attributes at the end of the createViewFromTag chain (which would solve all
    // this unnecessary rubbish). We at the very least try to optimise this as much as possible. We only call for
    // customViews (As they are the ones that never go through onCreateView(...)). We also maintain the Field
    // reference and make it accessible which will make a pretty significant difference to performance on Android 4.0+.

    if (view == null && name.indexOf('.') > -1) {
      Object[] mConstructorArgs = getConstrutorArgs();
      Object lastContext = null;
      if (mConstructorArgs != null) {
        lastContext = mConstructorArgs[0];
        // The LayoutInflater actually finds out the correct context to use.
        // We just need to set it on the mConstructor for the internal method.
        mConstructorArgs[0] = viewContext;
        setConstructorArgs(mConstructorArgs);
      }
      try {
        view = createView(name, null, attrs);
      } catch (ClassNotFoundException ignored) {
      } finally {
        if (mConstructorArgs != null) {
          mConstructorArgs[0] = lastContext;
          setConstructorArgs(mConstructorArgs);
        }
      }
    }
    return view;
  }

  private Object[] getConstrutorArgs() {
    Field field = ConstructorArgsHolder.FIELD;
    if (field != null) {
      try {
        return (Object[]) field.get(this);
      } catch (IllegalAccessException ignored) {
      }
    }
    return null;
  }

  private void setConstructorArgs(Object[] mConstructorArgs) {
    Field field = ConstructorArgsHolder.FIELD;
    if (field != null) {
      try {
        field.set(this, mConstructorArgs);
      } catch (IllegalAccessException ignored) {
      }
    }
  }

  private void setPrivateFactoryInternal() {
    if (setPrivateFactory) {
      return;
    }
    if (!(getContext() instanceof Factory2)) {
      setPrivateFactory = true;
      return;
    }
    Method method = SetPrivateFactoryHolder.METHOD;
    if (method != null) {
      Factory2 factory = new PrivateWrapperFactory2(this, (Factory2) getContext());
      try {
        method.invoke(this, factory);
      } catch (Exception ignored) {
      }
    }
    setPrivateFactory = true;
  }

  private static class WrapperFactory implements Factory {

    private final ObservableLayoutInflater inflater;
    private final Factory factory;

    WrapperFactory(ObservableLayoutInflater inflater, Factory factory) {
      this.inflater = inflater;
      this.factory = factory;
    }

    @Override public View onCreateView(String name, Context context, AttributeSet attrs) {
      return inflater.observeAndCreateView(factory.onCreateView(name, context, attrs), attrs);
    }

  }

  private static class WrapperFactory2 implements Factory2 {

    final ObservableLayoutInflater inflater;
    final Factory2 factory;

    WrapperFactory2(ObservableLayoutInflater inflater, Factory2 factory) {
      this.inflater = inflater;
      this.factory = factory;
    }

    @Override public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
      return inflater.observeAndCreateView(factory.onCreateView(parent, name, context, attrs), attrs);
    }

    @Override public View onCreateView(String name, Context context, AttributeSet attrs) {
      return inflater.observeAndCreateView(factory.onCreateView(name, context, attrs), attrs);
    }

  }

  private static class PrivateWrapperFactory2 extends WrapperFactory2 {

    PrivateWrapperFactory2(ObservableLayoutInflater inflater, Factory2 factory) {
      super(inflater, factory);
    }

    @Override public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
      View view = factory.onCreateView(parent, name, context, attrs);
      return inflater.observeAndCreateView(inflater.createCustomView(parent, view, name, context, attrs), attrs);
    }

  }

  private static class ConstructorArgsHolder {
    static final Field FIELD;

    static {
      Field field;
      try {
        field = LayoutInflater.class.getDeclaredField("mConstructorArgs");
        if (!field.isAccessible()) {
          field.setAccessible(true);
        }
      } catch (NoSuchFieldException e) {
        field = null;
      }
      FIELD = field;
    }
  }

  private static class SetPrivateFactoryHolder {
    static final Method METHOD;

    static {
      Method method;
      try {
        method = LayoutInflater.class.getDeclaredMethod("setPrivateFactory", Factory2.class);
      } catch (NoSuchMethodException e) {
        method = null;
      }
      METHOD = method;
    }
  }

}
