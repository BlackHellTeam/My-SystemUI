package com.lewa.systemuiext.switchwidget;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.content.res.Resources;

import com.lewa.systemuiext.R;

public class SwitchWidget extends GridView {
    public static final boolean DEBUG = true;
    public static final boolean DEBUG_VIEW = false;
    private final static boolean DEBUG_SIZE = SystemProperties.getBoolean(
            "slidingpanellayout.debugsize", false);

    public static final String TAG = "SwitchWidget";

    private List<String> mButtons;

    private WidgetBroadcastReceiver mBroadcastReceiver = null;
    private WidgetSettingsObserver mObserver = null;
    private LayoutInflater mLayoutInflater;
    private SwitchButtonFactory mSwitchButtonFactory;

    public final static int PORTRAIT_COLUMN_COUNT = 4;
    public final static int LANDSCAPE_COLUMN_COUNT = 6;
    public final static int BUTTONS_SIZE = 12;

    private int mColumnCount;
    private boolean mPortrait;
    private int mScreenWidth;

    private SwitchAdapter mSwitchAdapter;
    private HashMap<Uri, HashSet<String>> mObserverdUrisMap = new HashMap<Uri, HashSet<String>>();
    private HashMap<String, HashSet<String>> mIntentActionsMap = new HashMap<String, HashSet<String>>();

    private static SwitchWidget sInstance;

    public static SwitchWidget getInstance() {
        return sInstance;
    }

    private Callback mCallback;

    public SwitchWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLayoutInflater = LayoutInflater.from(context);
        mSwitchButtonFactory = SwitchButtonFactory.getInstance(context);
        SwitchButton.setContext(context.getApplicationContext());
        sInstance = this;
        if (DEBUG_VIEW) {
            setBackgroundColor(Color.BLUE);
        }
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        return false;
    }

    public static Handler sHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    public static interface Callback {
        public void onSizeChanged();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (DEBUG_SIZE) {
            Log.d(TAG, "onSizeChanged : " + w + ", " + h);
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mBroadcastReceiver = new WidgetBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SwitchButtonFactory.ACTION_SWITCH_WIDGET_CHANGED);
        filter.addAction(SwitchButtonFactory.ACTION_SWITCH_WIDGET_PAGE_CHANGE);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        getContext().registerReceiver(mBroadcastReceiver, filter);
        setupSettingsObserver(sHandler);
        mSwitchAdapter = new SwitchAdapter();
        setAdapter(mSwitchAdapter);
        updateConfiguration();
        reloadButtons();
    }

    private void reloadButtons() {
        mButtons = mSwitchButtonFactory.loadAllButtonKeys();
    }

    private void updateConfiguration() {
        final int orientation = getContext().getResources().getConfiguration().orientation;
        mPortrait = orientation == Configuration.ORIENTATION_PORTRAIT;
        mColumnCount = (mPortrait ? PORTRAIT_COLUMN_COUNT
                : LANDSCAPE_COLUMN_COUNT);
        setNumColumns(mColumnCount);

        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) getLayoutParams();
        Resources resources = mContext.getResources();

        int newWidth = resources
                .getDimensionPixelSize(R.dimen.status_bar_switchwidget_width);

        if (newWidth != lp.width) {
            lp.width = newWidth;
            ((ViewGroup) getParent()).updateViewLayout(this, lp);
        }

        mScreenWidth = resources.getDisplayMetrics().widthPixels;
        if (mCallback != null) {
            mCallback.onSizeChanged();
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateConfiguration();
    }

    class ViewHolder {
        TextView mTextView;
        ViewGroup mViewContainer;
    }

    class SwitchAdapter extends BaseAdapter {
        public SwitchAdapter() {

        }

        @Override
        public int getCount() {
            return BUTTONS_SIZE;
        }

        @Override
        public Object getItem(int position) {
            int count = getCount();

            if (position == count - 1) {
                return mButtons.get(mButtons.size() - 1);
            } else if (position == count - 2) {
                return mButtons.get(mButtons.size() - 2);
            } else {
                return mButtons.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.switch_button,
                        null);
                holder = new ViewHolder();
                holder.mTextView = (TextView) convertView
                        .findViewById(R.id.switch_icon_txt);
                holder.mViewContainer = (ViewGroup) convertView
                        .findViewById(R.id.v_switch_icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String type = (String) getItem(position);
            convertView.setTag(R.id.tag_key_button_type, type);
            convertView.setOnTouchListener(mOnTouchListener);
            convertView.setOnClickListener(mClickListener);
            convertView.setOnLongClickListener(mLongClickListener);
            convertView.setPressed(false);
            convertView.clearFocus();
            SwitchButton button = mSwitchButtonFactory.loadButton(type);
            /*
             * ViewGroup.LayoutParams lp =
             * holder.mViewContainer.getLayoutParams(); Point p =
             * getItemDimen(mPortrait, mScreenWidth); lp.width = p.x; lp.height
             * = p.y; ((ViewGroup)
             * holder.mViewContainer.getParent()).updateViewLayout
             * (holder.mViewContainer, lp);
             */
            button.bindView(holder.mTextView);
            observeUri(button.getObservedUris(), type);
            registerReceiver(button.getBroadcastIntentFilter(), type);
            return convertView;
        }
    }

    public Point getItemDimen(boolean portrait, int screenWidth) {
        int width = screenWidth
                / (portrait ? PORTRAIT_COLUMN_COUNT : LANDSCAPE_COLUMN_COUNT);
        int height = (int) (0.7 * width);
        return new Point(width, height);
    }

    private void registerReceiver(IntentFilter intentFilter, String type) {
        if (intentFilter == null) {
            return;
        }
        for (int i = 0; i < intentFilter.countActions(); i++) {
            final String action = intentFilter.getAction(i);
            HashSet<String> types = mIntentActionsMap.get(action);
            if (types == null) {
                types = new HashSet<String>();
            }
            types.add(type);
            mIntentActionsMap.put(action, types);
            mBroadcastReceiver.register(action);
        }
    }

    private void observeUri(List<Uri> observedUris, String type) {
        if (observedUris == null) {
            return;
        }
        for (Uri uri : observedUris) {
            HashSet<String> types = mObserverdUrisMap.get(uri);
            if (types == null) {
                types = new HashSet<String>();
            }
            types.add(type);
            mObserverdUrisMap.put(uri, types);
            mObserver.observe(uri);
        }
    }

    private OnTouchListener mOnTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent ev) {
            switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (view != null && (view.getParent() != null))
                    ((ViewGroup) view.getParent()).setPressed(true);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (view != null && (view.getParent() != null))
                    ((ViewGroup) view.getParent()).setPressed(false);
                break;
            }
            return false;
        }

    };

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String type = (String) v.getTag(R.id.tag_key_button_type);
            mSwitchButtonFactory.loadButton(type).toggleState();
        }
    };

    private View.OnLongClickListener mLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            String type = (String) v.getTag(R.id.tag_key_button_type);
            return mSwitchButtonFactory.loadButton(type).onLongClick();
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mObserver.unobserve();
        mBroadcastReceiver.unregister();
    }

    public void setupSettingsObserver(Handler handler) {
        if (mObserver == null) {
            mObserver = new WidgetSettingsObserver(handler);
        }
    }

    private class WidgetBroadcastReceiver extends BroadcastReceiver {
        private HashSet<String> mActions = new HashSet<String>();

        public void register(String action) {
            if (!mActions.contains(action)) {
                getContext().registerReceiver(this, new IntentFilter(action));
                mActions.add(action);
            }
        }

        public void unregister() {
            getContext().unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    SwitchButtonFactory.ACTION_SWITCH_WIDGET_CHANGED)
                    || intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                reloadButtons();
                mSwitchAdapter.notifyDataSetChanged();
            } else if (intent.getAction().equals(
                    SwitchButtonFactory.ACTION_SWITCH_WIDGET_PAGE_CHANGE)) {
                mSwitchAdapter.notifyDataSetChanged();
            } else {
                HashSet<String> types = mIntentActionsMap.get(intent
                        .getAction());
                for (String type : types) {
                    if (mButtons.get(0).equals(type)) {
                        mSwitchAdapter.notifyDataSetChanged();// there's some
                                                              // problem with
                                                              // the first item
                    } else {
                        mSwitchButtonFactory.loadButton(type).onReceive(
                                context, intent);
                    }
                }
            }
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "width : " + MeasureSpec.getSize(widthMeasureSpec));
        Log.d(TAG, "height : " + MeasureSpec.getSize(heightMeasureSpec));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private class WidgetSettingsObserver extends ContentObserver {

        private HashSet<Uri> mUris = new HashSet<Uri>();

        public WidgetSettingsObserver(Handler handler) {
            super(handler);
        }

        public void observe(Uri uri) {
            ContentResolver resolver = getContext().getContentResolver();
            if (mButtons.get(0).equals(SwitchButtonFactory.KEY_SOUND_BUTTONS)) {
                resolver.registerContentObserver(uri, false, this);
            }
            if (!mUris.contains(uri)) {
                mUris.add(uri);
                resolver.registerContentObserver(uri, false, this);
            }
        }

        public void unobserve() {
            ContentResolver resolver = getContext().getContentResolver();
            resolver.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.d(TAG, "uri : " + uri);
            Log.d(TAG, "types : " + mObserverdUrisMap.get(uri));
            HashSet<String> types = mObserverdUrisMap.get(uri);
            // pr61188 add begin
            // avoid FC due to null
            if (types != null && types.size() > 0) {
                // pr61188 lewa end
                for (String type : types) {
                    if (mButtons.get(0).equals(type)) {
                        mSwitchAdapter.notifyDataSetChanged();// there's some
                                                              // problem with
                                                              // the
                                                              // first item
                    } else {
                        mSwitchButtonFactory.loadButton(type).onChangeUri(uri);
                    }
                }
            }
        }
    }
}
