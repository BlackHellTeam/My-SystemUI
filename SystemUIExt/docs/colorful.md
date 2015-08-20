
彩色通知栏方案
===

####标准通知
 状态栏提供了两种接口
1. 如果需要在左侧的图标换为`应用图标`， 例如流量监控的“月流量剩余不足”提示，下载管理
　可以这样写：

```java
    private Notification buildOverrideLeftNotification(final String componentName) {
        int notifyId = new Random().nextInt(Integer.MAX_VALUE);
        Bundle extras = new Bundle();
        extras.putString("override-left", componentName);
        NotificationManager nm = getNotificationManager();
        Notification.Builder builder= new Notification.Builder(mContext)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentText("buildOverrideLeftNotification with componentName")
        .setContentTitle("contentTitle")
        .setExtras(extras);
        Notification n = builder.build();
        dumpNotification(n);
        nm.notify(notifyId, n);
        return null;
    }
```
  其中`componentName`为包名(例如`com.android.systemui`)或者activity名(例如`com.lewa.PIM/com.android.contacts.activities.DialtactsActivity`)

2 . 如果需要在图标右侧小图标处使用其他应用的`应用图标`，可以使用类似下面的代码

    ```java
        private void buildOverrideRighttNotification(final String componentName) {
            int notifyId = new Random().nextInt(Integer.MAX_VALUE);
            Bundle extras = new Bundle();
            extras.putString("override-right", componentName);
            NotificationManager nm = getNotificationManager();
            Notification.Builder builder= new Notification.Builder(mContext)
            .setLargeIcon(loadIcon(mContext, "com.android.systemui"))
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentText("componentName")
            .setContentTitle("buildOverrideRighttNotification componentName")
            .setExtras(extras);
            Notification n = builder.build();
            nm.notify(notifyId, n);
        }
    ```
　其中`componentName`为包名

3.如果需要在左侧的图标换为`Bitmap`，
可以这样写：
```java
    private Notification buildOverrideLeftNotification(final Bitmap bitmap) {
        int notifyId = new Random().nextInt(Integer.MAX_VALUE);
        Bundle extras = new Bundle();
        extras.putParcelable("override-left", bitmap);
        NotificationManager nm = getNotificationManager();
        Notification.Builder builder= new Notification.Builder(mContext)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentText("buildOverrideLeftNotification with bitmap")
        .setContentTitle("contentTitle")
        .setExtras(extras);
        Notification n = builder.build();
        dumpNotification(n);
        nm.notify(notifyId, n);
        return null;
    }
```
　其中`bitmap`是要替换的图片


4.如果需要在图标右侧小图标处使用'Bitmap'，可以使用类似下面的代码
```java
    private void buildOverrideRighttNotification(final Bitmap bitmap) {
        int notifyId = new Random().nextInt(Integer.MAX_VALUE);
        Bundle extras = new Bundle();
        extras.putParcelable("override-right", bitmap);
        NotificationManager nm = getNotificationManager();
        Notification.Builder builder= new Notification.Builder(mContext)
        .setLargeIcon(loadIcon(mContext, "com.android.systemui"))
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentText("bitmap")
        .setContentTitle("buildOverrideRighttNotification bitmap")
        .setExtras(extras);
        Notification n = builder.build();
        nm.notify(notifyId, n);
    }
```
其中`bitmap`是要替换的图片
####自定义通知

**获取图片**

对于自定义通知，即没有使用标准布局文件的通知，例如音乐与省电管理、邮件等，可以执行
```java
lewa.content.res.IconCustomizer.generateIconDrawable(drawable, true);
```
将图标转换为圆角矩形图标。
其中drawable为所需要的图片,类型为 `android.graphics.drawable.Drawable`

**图标大小**
我们定义的图标大小为40dp

**字体**
1.对于通知上部分主标题,请设置字体样式
`android:textAppearance="@style/TextAppearance.StatusBar.EventContent.Title"`
2.对于通知上部分主标题,请设置字体样式
`android:textAppearance="@style/TextAppearance.StatusBar.EventContent"`

**随着主题变化的图标**
如果需要随着主题变化的话，可以用类似下面的方法监听主题改变，并且重新获取图标
```java
mIntentFilter = new IntentFilter();
IntentFilter.addAction("com.lewa.intent.action.THEME_CHANGED");
mIntentFilter.addDataScheme("content");
mIntentFilter.addDataAuthority("com.lewa.themechooser.themes", null);
mIntentFilter.addDataPath("/theme", PatternMatcher.PATTERN_PREFIX);
```
