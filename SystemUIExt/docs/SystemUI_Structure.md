# SystemUI

**Code Structure**

+ LewaSystemUIExt
  - new apk
    - RoundR
    - LewaNotifilter
  - overlay resources
  - changed Modules
    -  `DateView`
    -  `Clock`
    - `SwipeHelper`
    - `carrierlabels`
  - new modules
    - Widgets
      -  `StatusbarSwitchLayout`
      -  `ExpandUsbButton`
      -  `NetUsageView`
      -  `NetSpeedView`
    - switchwidget buttons
    - transparent manager
    - FullScreen
    - `setSystemuiVisibility`
    - `ShakeListener`
    - USBManager
 - LewaPhoneStatusBar
   - lifeCycle
     - `onExpandVisible`
     - `onExpandInVisible`
   - loadConfigs
   - loadLewaSystemUI stuffs
   - override
+ `NavigationBar`
+ `ImageWallpaper`
 //cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE);
+ recents
+ power
  - `PowerUI`
+ net
  - `NetworkOverLimitActivity`
+ Policy
  - `NetworkController`
  - `NetworkControllerGemini`
+ StatusBar
  -  notificationIcons
    - `IconMerger`
  - ticker
  -  `StatusBarIconView`
  -  `DateView`
  -  `Clock`
  - statusIcons
  - signals
    - `SignalClusterView`
  - battery
    - `BatteryMergeView`

  - `BarTransitions` : bar animation helper```
  - `PhoneStatusBarView`: statusbar view
  - `PanelView` : parent class for panels that can be dropped down
+ Notifications
  - CarrierLabel

+ `GestureRecorder` Convenience class for capturing gestures for later analysis.
+ `PhoneStatusBar` : manager for statusbar and notification

**Performance**
1. 全屏时取消状态栏在后台接受广播等
2. 去除开关栏等

**MSimCarrierLabel**

- to update :
      - updateNetworkName in mCarrier1.updateNetworkName(false, "", true, "");
      -  Telephony.Intents.SPN_STRINGS_UPDATED_ACTION actions in PhoneStatusBar

**BatteryController**

- 实现了
    1. 充电动画
    2. 电池combo
    3. 电池text
- files
  - status_bar.xml && gemini_status_bar.xml
  - PhoneStatusBar.java

**资源对照表**
1. netusage up : dividerColor
2. netusage down : topliner.9.png
3. switch button color:

**FloatingNavigationBar**

**NetworkController**

+ `NetworkController`
  - `addCombinedLabelView`
  - `addMobileLabelView`
  - `hasMobileDataFeature`
  
**NEW in 4.4**

- Demo Mode
- SystemBars
- remove InstructorView add  HeadsUpView
- mDreamManager
- BatteryController

**Debug**

- 模拟出各种网络环境

**Questions**

+ What to do when configuration changes
+ 目前有哪些缺点?
+ 没有去掉其他的东西，资源加载过多了

