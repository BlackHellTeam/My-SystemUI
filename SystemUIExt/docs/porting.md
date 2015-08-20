


# SystemUI Porting Guide　　juuda.com

**功能对照表**
`说明：左侧表示功能名，右侧表示用到的文件`

STORAGE_NOTIFICATION_PRIORITY_NORMAL :   StorageNotification.java

LEWA_NAVIGATION_BAR :  KeyButtonView.java  PhoneStatusBar.java  NavigationBarView.java

LEWA_INTERCEPT_TOUCH_EVENT : StatusBarWindowView

LEWA_BATTERY_STYLE : QuickSettings.java PhoneStatusBar.java  PhoneStatusBarTransitions.java  NavigationBarTransitions.java   LewaBatteryController.java   msim_status_bar.xml

LEWA_PRETTY_TEXT_VIEW:    PrettyTextView.java  PhoneStatusBarView.java  assets/fonts/NeoSans.otf

LEWA_GOTO_SETTINGS_NEW_ACTIVITY: PhoneStatusBar.java(mSettingsButtonListener)

LEWA_CARRIER_LABEL: PhoneStatusBar.java (updateCarrierLabelVisibility)  gemini_carrier_label.xml

LEWA_DATE_AND_TIME:  PhoneStatusBar.java(mDateTimeView.setOnClickListener)

CONFIG_HIDE_NETWORK_TYPE: SignalClusterView.java(apply)

LEWA_SIGNAL_CLUSTER_HIDE_WHEN_NO_SIM : SignalClusterView.java(apply)

LEWA_SINGAL_CLUSTER_STYLE : signal_cluster_view.xml    gemini_signal_cluster_view.xml

LEWA_SINGAL_CLUSTER_ICONS : TelephonyIcons.java

LEWA_SIGNAL_CLUSTER_HIDE_WHEN_NO_SIM: NetworkController.java

LEWA_SINGAL_CLUSTER_SHOW_INDICATOR : SignalClusterView.java(apply, onAttachedToWindow)

CONFIG_SHOW_NOTIFILTER_MENU_ON_LONG_CLICK BaseStatusBar.java

CONFIG_SWIPE_TO_SHOW_NOTIFILTER :status_bar_notification_row.xml

CONFIG_NOTIFICATION_TRANSPARENT_BACKGROUND: BaseStatusBar.java(applyLegacyRowBackground)

CONFIG_POWER_UI_NO_WARNING ：　PowerUI.java

CONFIG_FACNCY_DRAWABLE ：　SystemUIService.java

CONFIG_LEWA_STYLE : SystemUIService.java(onCreate)

CONFIG_LEWA_EXPANED: gemini_status_bar_expanded.xml status_bar_expanded.xml  status_bar_expanded_header.xml

**TODO**

- make netusage and battery platform independent


