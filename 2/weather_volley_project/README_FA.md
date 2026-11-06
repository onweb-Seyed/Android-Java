# پروژه آب‌وهوا با Java و کتابخانه Volley

این پروژه یک نمونه آموزشی اندروید است که نام شهر را از کاربر می‌گیرد، اطلاعات آن را از دو وب‌سرویس Open-Meteo دریافت می‌کند و نتیجه را روی صفحه نشان می‌دهد.

## 1) امکانات برنامه

- دریافت نام شهر به فارسی یا انگلیسی؛ نمونه: «کرمان» یا `Kerman`
- دریافت دمای فعلی بر حسب درجه سانتی‌گراد
- نمایش وضعیت کلی هوا (صاف، ابری، بارانی و ...)
- نمایش رطوبت نسبی، دمای احساسی و سرعت باد
- نمایش زمان داده‌ای که سرویس برگردانده است
- نمایش پیام مناسب هنگام خالی بودن ورودی، پیدا نشدن شهر یا خطای شبکه
- استفاده از Volley برای ارسال درخواست‌های HTTP به شکل غیرهم‌زمان

## 2) ابزارها و فناوری‌ها

- Android Studio
- زبان Java
- رابط کاربری با XML
- کتابخانه شبکه: `com.android.volley:volley:1.2.1`
- وب‌سرویس مکان‌یابی: Open-Meteo Geocoding API
- وب‌سرویس آب‌وهوا: Open-Meteo Forecast API

برای نمونه آموزشی و استفاده غیرتجاری این پروژه، کلید API جداگانه‌ای لازم نیست. برای شرایط استفاده و محدودیت‌های سرویس، مستندات رسمی Open-Meteo را ببینید.

## 3) ساختار پوشه‌ها

```text
WeatherVolley/
├── settings.gradle
├── build.gradle
├── gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
└── app/
    ├── build.gradle
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/example/weathervolley/MainActivity.java
        └── res/
            ├── drawable/bg_city_input.xml
            ├── drawable/bg_weather_card.xml
            ├── layout/activity_main.xml
            └── values/styles.xml
```

## 4) باز کردن پروژه در Android Studio

1. فایل ZIP را Extract کنید.
2. در Android Studio گزینه **Open** را بزنید و پوشه `WeatherVolley` را انتخاب کنید.
3. مطمئن شوید Android SDK Platform 35 نصب است.
4. برای اجرای Android Gradle Plugin 8.7.3 از JDK 17 استفاده کنید.
5. اتصال اینترنت و دسترسی Gradle به مخازن `google()` و `mavenCentral()` برقرار باشد؛ سپس **Sync Project with Gradle Files** را بزنید.
6. گوشی را با USB وصل کنید یا یک Emulator اجرا کنید.
7. دکمه Run را بزنید. برنامه به اینترنت نیاز دارد.

**نکته درباره Gradle Wrapper:** فایل تنظیمات نسخه Gradle در `gradle/wrapper/gradle-wrapper.properties` قرار دارد. فایل اجرایی Wrapper (`gradle-wrapper.jar`) در این بسته قرار نگرفته است. اگر Android Studio به‌دلیل نبود Wrapper نتوانست پروژه را مستقیماً Sync کند، یک پروژه جدید از نوع **Empty Views Activity** با زبان Java بسازید و فایل‌های پوشه `app` و تنظیمات Gradle این بسته را در پروژه جدید جایگزین کنید؛ یا از Gradle نصب‌شده در سیستم استفاده کنید. این موضوع به کد Java و عملکرد وب‌سرویس ارتباطی ندارد.

## 5) وابستگی Volley کجاست؟

در فایل `app/build.gradle`:

```gradle
dependencies {
    implementation 'com.android.volley:volley:1.2.1'
}
```

این خط به Gradle می‌گوید کتابخانه Volley را از مخازن Maven دریافت کند و به برنامه اضافه کند.

## 6) مجوز اینترنت کجاست؟

در فایل `app/src/main/AndroidManifest.xml` و بیرون از تگ `application`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

این مجوز اجازه می‌دهد برنامه درخواست شبکه ارسال کند. این مجوز از نوع مجوزهای عادی اندروید است و مانند دوربین یا موقعیت مکانی، پنجره تأیید جداگانه‌ای در زمان اجرا نشان نمی‌دهد.

## 7) منطق کلی برنامه

```text
کاربر نام شهر را وارد می‌کند
             |
             v
درخواست اول با Volley به Geocoding API
             |
             v
دریافت پاسخ JSON و استخراج latitude و longitude
             |
             v
درخواست دوم با Volley به Forecast API
             |
             v
دریافت پاسخ JSON و استخراج current
             |
             v
نمایش دما، رطوبت، دمای احساسی و سرعت باد
```

### چرا دو درخواست داریم؟

کاربر نام شهر را می‌داند، اما سرویس پیش‌بینی آب‌وهوا برای مکان موردنظر به مختصات جغرافیایی نیاز دارد. درخواست اول نام شهر را به مختصات تبدیل می‌کند. درخواست دوم با استفاده از همان مختصات اطلاعات هواشناسی را می‌گیرد.

## 8) توضیح کلاس MainActivity.java، بخش به بخش

### الف) Importها

```java
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
```

این کلاس‌ها اجزای اصلی Volley هستند:

- `Request`: نوع درخواست، مانند GET یا POST را مشخص می‌کند.
- `RequestQueue`: صفی است که درخواست‌های شبکه را مدیریت و اجرا می‌کند.
- `Response.Listener`: زمانی اجرا می‌شود که پاسخ موفق از سرور دریافت شده باشد.
- `VolleyError`: اطلاعات مربوط به خطای شبکه یا پاسخ ناموفق را نگه می‌دارد.
- `JsonObjectRequest`: برای ارسال درخواست و دریافت پاسخ با قالب JSON استفاده می‌شود.
- `Volley`: ابزار ساخت RequestQueue است.

همچنین `JSONObject` و `JSONArray` از کتابخانه داخلی JSON اندروید برای خواندن پاسخ استفاده می‌شوند.

### ب) متغیرهای ثابت آدرس وب‌سرویس

```java
private static final String GEOCODING_URL =
        "https://geocoding-api.open-meteo.com/v1/search";
private static final String WEATHER_URL =
        "https://api.open-meteo.com/v1/forecast";
```

آدرس‌ها را در متغیرهای ثابت قرار داده‌ایم تا اگر آدرس سرویس تغییر کرد، فقط یک محل را ویرایش کنیم. `static final` یعنی این مقادیر متعلق به کلاس‌اند و در طول اجرای برنامه تغییر نمی‌کنند.

### پ) متغیرهای رابط کاربری

```java
private EditText etCity;
private Button btnSearch;
private ProgressBar progressBar;
private TextView tvStatus;
```

این متغیرها نماینده اجزای XML هستند:

- `EditText`: دریافت نام شهر
- `Button`: شروع عملیات جستجو
- `ProgressBar`: نشان دادن اینکه برنامه در حال دریافت اطلاعات است
- `TextView`: نمایش وضعیت و نتایج

در `onCreate` با `findViewById` هر متغیر به View متناظر خودش وصل می‌شود.

### ت) ساخت صف درخواست‌ها

```java
requestQueue = Volley.newRequestQueue(getApplicationContext());
```

به‌جای اینکه برای هر درخواست یک سازوکار شبکه جدا بسازیم، یک RequestQueue ایجاد می‌کنیم و هر دو درخواست را به همین صف می‌دهیم. Volley اجرای شبکه را در پس‌زمینه مدیریت می‌کند؛ بنابراین اجرای درخواست نباید رابط کاربری را متوقف کند.

### ث) رویداد کلیک دکمه

```java
btnSearch.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        searchCityAndLoadWeather();
    }
});
```

`OnClickListener` یعنی وقتی کاربر روی دکمه کلیک کرد، متد `searchCityAndLoadWeather` فراخوانی شود. در نتیجه، منطق برنامه از رویداد دکمه جدا نگه داشته شده است.

### ج) خواندن و بررسی نام شهر

```java
final String city = etCity.getText().toString().trim();

if (city.isEmpty()) {
    etCity.setError("ابتدا نام شهر را وارد کنید");
    etCity.requestFocus();
    return;
}
```

- `getText()` متن واردشده را می‌گیرد.
- `toString()` آن را به String تبدیل می‌کند.
- `trim()` فاصله‌های ابتدا و انتهای متن را حذف می‌کند.
- `isEmpty()` بررسی می‌کند که ورودی خالی نباشد.
- `return` باعث می‌شود اگر شهر وارد نشده بود، متد همان‌جا تمام شود و درخواست شبکه ارسال نشود.

### چ) آماده کردن URL درخواست اول

```java
Uri uri = Uri.parse(GEOCODING_URL).buildUpon()
        .appendQueryParameter("name", city)
        .appendQueryParameter("count", "1")
        .appendQueryParameter("language", "fa")
        .appendQueryParameter("format", "json")
        .build();
```

هر `appendQueryParameter` یک پارامتر به آدرس اضافه می‌کند:

- `name`: نام شهر
- `count=1`: فقط یک نتیجه برگردد
- `language=fa`: نام مکان‌ها در صورت پشتیبانی به فارسی برگردد
- `format=json`: پاسخ با قالب JSON باشد

استفاده از `Uri.Builder` کمک می‌کند حروف فارسی و فاصله‌ها به‌درستی در URL کدگذاری شوند.

### ح) ارسال درخواست اول با JsonObjectRequest

```java
JsonObjectRequest cityRequest = new JsonObjectRequest(
        Request.Method.GET,
        url,
        null,
        new Response.Listener<JSONObject>() { ... },
        new Response.ErrorListener() { ... }
);
requestQueue.add(cityRequest);
```

ترتیب پارامترهای سازنده در این مثال:

1. `Request.Method.GET`: نوع درخواست GET است؛ یعنی از سرور اطلاعات می‌خواهیم.
2. `url`: آدرس نهایی وب‌سرویس.
3. `null`: بدنه درخواست نداریم؛ در یک GET ساده نیازی به ارسال JSON در بدنه نیست.
4. `Response.Listener`: کدی که در صورت دریافت پاسخ موفق اجرا می‌شود.
5. `Response.ErrorListener`: کدی که در صورت بروز خطا اجرا می‌شود.

در پایان با `requestQueue.add(cityRequest)` درخواست را به صف Volley تحویل می‌دهیم.

### خ) خواندن پاسخ JSON درخواست اول

پاسخ مکان‌یابی شبیه این ساختار است:

```json
{
  "results": [
    {
      "name": "Kerman",
      "latitude": 30.2832,
      "longitude": 57.0788,
      "country": "Iran"
    }
  ]
}
```

کد ابتدا آرایه `results` را می‌گیرد. اگر آرایه وجود نداشته باشد یا خالی باشد، یعنی شهری مطابق ورودی پیدا نشده است.

```java
JSONArray results = response.optJSONArray("results");
JSONObject location = results.getJSONObject(0);
double latitude = location.getDouble("latitude");
double longitude = location.getDouble("longitude");
```

- `JSONObject`: یک شیء JSON شامل زوج‌های کلید و مقدار است.
- `JSONArray`: یک آرایه از چند مقدار یا شیء JSON است.
- `getJSONObject(0)`: اولین نتیجه آرایه را می‌گیرد.
- `getDouble("latitude")`: مقدار عددی طول یا عرض جغرافیایی را از کلید موردنظر می‌خواند.

در این پروژه فقط اولین نتیجه را استفاده می‌کنیم تا مثال ساده بماند.

### د) درخواست دوم برای دریافت آب‌وهوا

متد `loadWeather` طول و عرض جغرافیایی را می‌گیرد و URL درخواست دوم را می‌سازد. پارامتر `current` مشخص می‌کند چه فیلدهایی را از سرویس می‌خواهیم:

```text
temperature_2m
relative_humidity_2m
apparent_temperature
wind_speed_10m
weather_code
```

معنی فیلدها:

| نام فیلد | معنی |
|---|---|
| `temperature_2m` | دمای هوا در ارتفاع ۲ متری |
| `relative_humidity_2m` | رطوبت نسبی در ارتفاع ۲ متری |
| `apparent_temperature` | دمای احساسی |
| `wind_speed_10m` | سرعت باد در ارتفاع ۱۰ متری |
| `weather_code` | کد عددی توصیف وضعیت هوا |

پارامتر `temperature_unit=celsius` واحد دما را سانتی‌گراد و `wind_speed_unit=kmh` واحد باد را کیلومتر بر ساعت تعیین می‌کند. `timezone=auto` باعث می‌شود زمان پاسخ بر اساس منطقه زمانی مکان انتخاب‌شده باشد.

### ذ) استخراج داده از پاسخ دوم

اطلاعات فعلی آب‌وهوا در شیء `current` قرار دارد. نمونه ساده‌شده:

```json
{
  "current": {
    "temperature_2m": 25.4,
    "relative_humidity_2m": 18,
    "apparent_temperature": 24.8,
    "wind_speed_10m": 12.5,
    "weather_code": 1,
    "time": "2026-11-06T14:30"
  }
}
```

کد با `response.getJSONObject("current")` به شیء فعلی دسترسی پیدا می‌کند و سپس با `getDouble` یا `getInt` مقادیر را می‌خواند.

### ر) نمایش خروجی

```java
tvTemperature.setText(String.format(Locale.getDefault(), "%.1f °C", temperature));
```

`String.format` مقدار عددی را در قالب متن قرار می‌دهد. `%.1f` یعنی عدد اعشاری با یک رقم بعد از ممیز نمایش داده شود. سپس `setText` متن را در TextView قرار می‌دهد.

### ز) تبدیل کد هوا به توضیح

سرویس برای وضعیت هوا یک کد عددی به نام `weather_code` برمی‌گرداند. متد `getWeatherDescription` این کد را به عبارت فارسی تبدیل می‌کند؛ برای مثال کد صفر به معنی آسمان صاف است. به این ترتیب کاربر لازم نیست کدهای عددی را بشناسد.

### ژ) نمایش حالت انتظار و مدیریت خطا

متد `setLoading(true)` نوار انتظار را نمایش می‌دهد و دکمه را موقتاً غیرفعال می‌کند. پس از پایان موفق یا ناموفق درخواست، `setLoading(false)` نوار را مخفی و دکمه را فعال می‌کند.

`showNetworkError` نیز خطا را به کاربر توضیح می‌دهد. اگر پاسخ HTTP با کد خطا دریافت شده باشد، کد وضعیت نمایش داده می‌شود؛ در غیر این صورت، پیام بررسی اتصال اینترنت نشان داده می‌شود.

## 9) مفاهیم مهم برای پاسخ به سؤال‌های استاد

**وب‌سرویس چیست؟** یک سرویس نرم‌افزاری است که از طریق شبکه، داده یا قابلیت مشخصی را در اختیار برنامه‌های دیگر قرار می‌دهد.

**API چیست؟** رابطی است که مشخص می‌کند برنامه‌ها با چه آدرس و پارامترهایی می‌توانند از یک سرویس درخواست کنند و چه پاسخی می‌گیرند.

**HTTP چیست؟** پروتکل رایج تبادل درخواست و پاسخ در وب است. در این پروژه از GET برای دریافت داده استفاده شده است.

**JSON چیست؟** قالب متنی سبک برای نمایش داده به شکل کلید و مقدار و آرایه است. مثال: `{"temperature": 25.4}`.

**Volley چیست؟** کتابخانه اندروید برای مدیریت درخواست‌های شبکه؛ صف درخواست، دریافت پاسخ، Listener موفقیت و Listener خطا را فراهم می‌کند.

**چرا Volley؟** برای درخواست‌های معمول API مناسب است، کارهای تکراری شبکه را ساده می‌کند و پاسخ‌ها را از طریق Listener در اختیار برنامه می‌گذارد.

**چرا درخواست غیرهم‌زمان است؟** تا هنگام انتظار برای پاسخ سرور، رابط کاربری قفل نشود و کاربر بتواند وضعیت انتظار را ببیند.

**چرا API Key نداریم؟** سرویس انتخاب‌شده برای این سناریوی آموزشی و استفاده غیرتجاری، دسترسی عمومی ارائه می‌کند؛ همه APIها چنین شرایطی ندارند.

## 10) اگر برنامه کار نکرد

1. مطمئن شوید اینترنت گوشی یا Emulator وصل است.
2. بررسی کنید در `AndroidManifest.xml` مجوز `INTERNET` وجود داشته باشد.
3. مطمئن شوید Gradle وابستگی Volley را دانلود کرده و Sync بدون خطا تمام شده باشد.
4. نام شهر را با املای قابل‌شناسایی وارد کنید؛ مثلاً «کرمان» یا `Kerman`.
5. اگر پاسخ سرور کد HTTP دارد، اتصال، محدودیت سرویس یا آدرس درخواست را بررسی کنید.
6. اگر خطای `Could not resolve com.android.volley:volley:1.2.1` دیدید، معمولاً مشکل دسترسی Gradle به مخازن Google یا Maven Central است، نه اشکال کد Java.

## 11) منابع رسمی

- Volley: https://google.github.io/volley/
- Open-Meteo Geocoding API: https://open-meteo.com/en/docs/geocoding-api
- Open-Meteo Forecast API: https://open-meteo.com/en/docs
