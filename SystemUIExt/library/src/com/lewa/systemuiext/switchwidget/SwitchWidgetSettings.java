package com.lewa.systemuiext.switchwidget;

import android.app.ActionBar;
import lewa.support.v7.app.ActionBarListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lewa.systemuiext.R;

import java.util.List;

import lewa.widget.DraggableListView;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ListView;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
/**
 * @author: Woody Guo <guozhenjiang@ndoo.net>
 * @description: Activity to configure the SwitchWidget, i.e., single page style or dula pages style,
 * orders of the switches, which buttons to show when in single page style
 * 
 * @author juude.song@gmail.com
 * @update: 1. load SwitchButtons from SwitchWidgetFractory
 *          2. make all buttons clickable
 *          3. optimize comments
 *
 */
public class SwitchWidgetSettings extends ActionBarListActivity
{
    private static final String TAG = "SwitchWidgetSettings";

    private DraggableListView mButtonList;
    private ButtonAdapter mButtonAdapter;

    private List<String> mSwitchButtons;

    private Context mContext;

    public static final int SWITCH_WIDGET_STYLE_SINGLE_PAGE = 1;
    public static final int SWITCH_WIDGET_STYLE_DUAL_PAGES = 2;

    public static final int MAX_WIDGET = 10;

    private SwitchButtonFactory mSwitchButtonFactory;
    private int mHeightSplitActionBar = 0;
    private View mFootView;
    private int mDefaultHeightSplitActionBar = 0;

    public static final String ACTION_PHONE_STATUSBAR_SETTINGS = "lewa.intent.action.PHONE_STATUSBAR_SETTINGS";
    private static final String COLORFUL_ACTION="com.lewa.intent.action.KILLPROCESSES_DONE";
    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SwitchButton switchButton = (SwitchButton) mButtonAdapter.getItem(position);
            if (switchButton != null)
            switchButton.onLongClick();
        }

    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.switch_widget_settings);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE
                | ActionBar.DISPLAY_HOME_AS_UP);
        mButtonList = (DraggableListView) getListView();
        mContext = this;
        mDefaultHeightSplitActionBar = getResources().getDimensionPixelSize(R.dimen.split_action_bar_height);
        mButtonList.setDropListener(mDropListener);
        mButtonList.setFixedItem(MAX_WIDGET -1);
        //addFooterViewForSplitBar();
        mButtonList.setOnItemClickListener(mOnItemClickListener);
        initSwitchButtons();

        mButtonAdapter = new ButtonAdapter(this);
        setListAdapter(mButtonAdapter);
        IntentFilter iflter = new IntentFilter();
        iflter.addAction(COLORFUL_ACTION);
        registerReceiver(mReceiver, iflter);
    }

     private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            SwitchWidgetSettings.this.finish();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem actionItem =
                menu.add(0, Menu.FIRST, 0, R.string.statusbarsettings);
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        /*ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            if (mHeightSplitActionBar == 0) {
                mHeightSplitActionBar = actionBar.getSplitHeight();
                if (mFootView != null) {
                    if (mHeightSplitActionBar != 0) {
                        mFootView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, mHeightSplitActionBar));
                    } else {
                        mFootView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, mDefaultHeightSplitActionBar));
                    }
                }
            }
        }*/
        actionItem.setOnMenuItemClickListener(btnlistener);
        return true;
    }
    //LeWa jxli: add for splitBar
    private void addFooterViewForSplitBar() {
        ListView list = mButtonList;
        ImageView imageView = new ImageView(mContext);
        imageView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, mHeightSplitActionBar));
        imageView.setImageResource(R.color.transparent);
        imageView.setVisibility(View.INVISIBLE);
        mFootView = imageView;
        list.setFooterDividersEnabled(false);
        list.addFooterView(imageView);
    }

    OnMenuItemClickListener btnlistener = new OnMenuItemClickListener(){
        @Override
        public boolean onMenuItemClick(MenuItem arg0) {
            Intent intent = new Intent(ACTION_PHONE_STATUSBAR_SETTINGS);
            mContext.startActivity(intent);
            return true;
        }

    };
    
    @Override
    public void onDestroy() {
        ((DraggableListView) mButtonList).setDropListener(null);
        setListAdapter(null);
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
        
    private DraggableListView.DropListener mDropListener = new DraggableListView.DropListener() {
        public void drop(int from, int to) {
            String button = null;
            if (from < mButtonAdapter.getCount()) {
                if(from > MAX_WIDGET) {
                    button = mSwitchButtons.remove(from - 1);
                }
                else if(from < MAX_WIDGET) {
                    button = mSwitchButtons.remove(from);
                }
                else {
                    return;
                }
            }
            if (to <= mButtonAdapter.getCount()) {
                if(to > MAX_WIDGET) {
                    mSwitchButtons.add(to -1 , button);
                }
                else {
                    mSwitchButtons.add(to, button);
                }
            }
            mButtonAdapter.notifyDataSetChanged();
            mSwitchButtonFactory.saveSwitchButtons(mSwitchButtons);
        }
    };

    private class ButtonAdapter extends BaseAdapter {
        private Context mContext;
        private Resources mResources = null;
        private LayoutInflater mInflater;

        public ButtonAdapter(Context c) {
            mContext = c;
            mResources = mContext.getResources();
            mInflater = LayoutInflater.from(mContext);
        }

        public int getCount() {
            return mSwitchButtons.size() + 1;
        }

        public Object getItem(int position) {
            if(position > MAX_WIDGET) {
                return  mSwitchButtonFactory.loadButton(mSwitchButtons.get(position -  1));
            }
            else  if ( position < MAX_WIDGET){
                return mSwitchButtonFactory.loadButton(mSwitchButtons.get(position));
            }
            else {
                return null;
            }
        }

        public long getItemId(int position) {
            return position;
        }

        public int dip2px(Context context, float dpValue) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }

        private ItemViewHolder holder;
        public View getView(int position, View convertView, ViewGroup parent) {
            SwitchButton button = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.switch_button_settings_item, null);
                View grabber  = convertView.findViewById(R.id.grabber);
                grabber.setId(lewa.R.id.grabber);//TODO: make "lewa:id/grabber" it compilable in eclipse
                holder = new ItemViewHolder();
                holder.mIcon = (ImageView) convertView.findViewById(R.id.icon);
                holder.mTextTitle = (TextView) convertView.findViewById(R.id.name);
                convertView.setTag(holder);
            }else{
                holder = (ItemViewHolder) convertView.getTag();
            }
            
            button = (SwitchButton)getItem(position);

            RelativeLayout switchItemLayout=(RelativeLayout)convertView.findViewById(R.id.switch_item_id);
            LinearLayout noticeLayout=(LinearLayout)convertView.findViewById(R.id.notice_nums_id);
            if (position == MAX_WIDGET) {
                ViewGroup.LayoutParams params = convertView.getLayoutParams();
                if (params != null && convertView != null) {
                    params.height = dip2px(convertView.getContext(),24.0f);
                    convertView.setLayoutParams(params);
                }

                switchItemLayout.setVisibility(View.GONE);
                noticeLayout.setVisibility(View.VISIBLE);
                noticeLayout.setBackgroundColor(0xffe0e0e0);
                TextView notice=(TextView) noticeLayout.getChildAt(0);
                notice.setTextColor(0xff7f7f7f);
                noticeLayout.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
                noticeLayout.setEnabled(false);
            } else {
                ViewGroup.LayoutParams params = convertView.getLayoutParams();
                if (params != null && convertView != null) {
                    params.height = dip2px(convertView.getContext(),48.0f);
                    convertView.setLayoutParams(params);
                }
                switchItemLayout.setVisibility(View.VISIBLE);
                noticeLayout.setVisibility(View.GONE);
                if (null != button) {
                    try {
                        Drawable d = mResources.getDrawable(button.mSettingsIcon);
                        holder.mIcon.setImageDrawable(d);
                        holder.mIcon.setVisibility(View.VISIBLE);
                    } catch(Exception e) {
                        holder.mIcon.setVisibility(View.GONE);
                        Log.e(TAG, "Error retrieving icon drawable", e);
                    }
                    try {
                        holder.mTextTitle.setText(mResources.getString(button.mButtonName));
                    } catch(Exception e) {
                        Log.e(TAG, "Error retrieving string", e);
                    }
                }
            }
            return convertView;
        }
    }
    
    private final class ItemViewHolder {
        public ImageView mIcon;
        public TextView mTextTitle;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {
        case android.R.id.home:
            finish();
            return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initSwitchButtons() {
        mSwitchButtonFactory = SwitchButtonFactory.getInstance(getApplicationContext());
        mSwitchButtons = mSwitchButtonFactory.loadSwitchButtons();
    }
}
