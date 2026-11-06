package com.example.weathervolley;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * نمونه آموزشی دریافت اطلاعات از وب سرویس با Java و کتابخانه Volley.
 * روند برنامه:
 * 1) نام شهر به Geocoding API ارسال می شود.
 * 2) مختصات جغرافیایی از پاسخ JSON خوانده می شود.
 * 3) با مختصات، اطلاعات آب و هوای فعلی از Forecast API دریافت می شود.
 * 4) مقادیر استخراج شده در صفحه نمایش داده می شوند.
 */
public class MainActivity extends Activity {

    private static final String GEOCODING_URL =
            "https://geocoding-api.open-meteo.com/v1/search";
    private static final String WEATHER_URL =
            "https://api.open-meteo.com/v1/forecast";

    // اجزای رابط کاربری که در activity_main.xml تعریف شده اند.
    private EditText etCity;
    private Button btnSearch;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private TextView tvCity;
    private TextView tvTemperature;
    private TextView tvDescription;
    private TextView tvHumidity;
    private TextView tvFeelsLike;
    private TextView tvWind;
    private TextView tvUpdated;

    // صف Volley درخواست های شبکه را مدیریت می کند.
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // اتصال متغیرهای Java به Viewهای XML از طریق شناسه (id).
        etCity = findViewById(R.id.etCity);
        btnSearch = findViewById(R.id.btnSearch);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        tvCity = findViewById(R.id.tvCity);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvDescription = findViewById(R.id.tvDescription);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvWind = findViewById(R.id.tvWind);
        tvUpdated = findViewById(R.id.tvUpdated);

        // ساخت RequestQueue با Context برنامه؛ از همین صف برای هر دو درخواست استفاده می کنیم.
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        // وقتی کاربر روی دکمه بزند، جستجوی شهر شروع می شود.
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchCityAndLoadWeather();
            }
        });
    }

    /** مرحله اول: تبدیل نام شهر به طول و عرض جغرافیایی. */
    private void searchCityAndLoadWeather() {
        final String city = etCity.getText().toString().trim();

        // اعتبارسنجی ورودی؛ درخواست خالی به سرور نمی فرستیم.
        if (city.isEmpty()) {
            etCity.setError("ابتدا نام شهر را وارد کنید");
            etCity.requestFocus();
            return;
        }

        setLoading(true);
        tvStatus.setText("در حال پیدا کردن شهر...");

        // Uri.Builder پارامترها را به شکل امن به URL اضافه و متن فارسی را Encode می کند.
        Uri uri = Uri.parse(GEOCODING_URL).buildUpon()
                .appendQueryParameter("name", city)
                .appendQueryParameter("count", "1")
                .appendQueryParameter("language", "fa")
                .appendQueryParameter("format", "json")
                .build();

        String url = uri.toString();

        // JsonObjectRequest یک درخواست GET می فرستد و پاسخ JSON را به ما می دهد.
        JsonObjectRequest cityRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.optJSONArray("results");

                            // اگر آرایه results وجود نداشته باشد یا خالی باشد، شهر پیدا نشده است.
                            if (results == null || results.length() == 0) {
                                setLoading(false);
                                tvStatus.setText("شهری با این نام پیدا نشد؛ املای نام شهر را بررسی کنید.");
                                return;
                            }

                            JSONObject location = results.getJSONObject(0);
                            double latitude = location.getDouble("latitude");
                            double longitude = location.getDouble("longitude");
                            String cityName = location.optString("name", city);
                            String country = location.optString("country", "");

                            // درخواست دوم بعد از دریافت مختصات اجرا می شود.
                            loadWeather(cityName, country, latitude, longitude);

                        } catch (JSONException e) {
                            setLoading(false);
                            tvStatus.setText("پاسخ سرویس مکان یابی قابل خواندن نیست.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showNetworkError(error);
                    }
                }
        );

        requestQueue.add(cityRequest);
    }

    /** مرحله دوم: دریافت وضعیت فعلی آب و هوا با مختصات شهر. */
    private void loadWeather(final String cityName,
                             final String country,
                             double latitude,
                             double longitude) {

        tvStatus.setText("در حال دریافت وضعیت آب و هوا...");

        Uri uri = Uri.parse(WEATHER_URL).buildUpon()
                .appendQueryParameter("latitude", String.valueOf(latitude))
                .appendQueryParameter("longitude", String.valueOf(longitude))
                .appendQueryParameter("current",
                        "temperature_2m,relative_humidity_2m,apparent_temperature,wind_speed_10m,weather_code")
                .appendQueryParameter("temperature_unit", "celsius")
                .appendQueryParameter("wind_speed_unit", "kmh")
                .appendQueryParameter("timezone", "auto")
                .build();

        JsonObjectRequest weatherRequest = new JsonObjectRequest(
                Request.Method.GET,
                uri.toString(),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // اطلاعات لحظه ای داخل شیء current قرار دارد.
                            JSONObject current = response.getJSONObject("current");

                            double temperature = current.getDouble("temperature_2m");
                            double humidity = current.getDouble("relative_humidity_2m");
                            double feelsLike = current.getDouble("apparent_temperature");
                            double windSpeed = current.getDouble("wind_speed_10m");
                            int weatherCode = current.getInt("weather_code");
                            String updateTime = current.optString("time", "نامشخص")
                                    .replace("T", " ");

                            String fullCityName = cityName;
                            if (!country.isEmpty()) {
                                fullCityName += "، " + country;
                            }

                            // تبدیل داده های JSON به متن قابل فهم و نمایش در صفحه.
                            tvCity.setText(fullCityName);
                            tvTemperature.setText(String.format(
                                    Locale.getDefault(), "%.1f °C", temperature));
                            tvDescription.setText(getWeatherDescription(weatherCode));
                            tvHumidity.setText(String.format(
                                    Locale.getDefault(), "رطوبت نسبی: %.0f٪", humidity));
                            tvFeelsLike.setText(String.format(
                                    Locale.getDefault(), "دمای احساسی: %.1f °C", feelsLike));
                            tvWind.setText(String.format(
                                    Locale.getDefault(), "سرعت باد: %.1f کیلومتر بر ساعت", windSpeed));
                            tvUpdated.setText("زمان داده دریافتی: " + updateTime);

                            tvStatus.setText("اطلاعات با موفقیت دریافت شد.");
                            setLoading(false);

                        } catch (JSONException e) {
                            setLoading(false);
                            tvStatus.setText("ساختار پاسخ آب و هوا کامل یا معتبر نیست.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showNetworkError(error);
                    }
                }
        );

        requestQueue.add(weatherRequest);
    }

    /** تبدیل کد استاندارد WMO به عبارت فارسی قابل نمایش. */
    private String getWeatherDescription(int code) {
        if (code == 0) return "آسمان صاف";
        if (code == 1) return "عمدتاً صاف";
        if (code == 2) return "نیمه ابری";
        if (code == 3) return "ابری";
        if (code == 45 || code == 48) return "مه آلود";
        if (code >= 51 && code <= 57) return "نم نم باران";
        if (code >= 61 && code <= 67) return "بارانی";
        if (code >= 71 && code <= 77) return "برفی";
        if (code >= 80 && code <= 82) return "رگبار باران";
        if (code == 85 || code == 86) return "رگبار برف";
        if (code == 95 || code == 96 || code == 99) return "طوفان همراه با رعد و برق";
        return "وضعیت نامشخص";
    }

    /** نمایش ProgressBar هنگام دریافت و آزاد کردن دکمه جستجو در پایان کار. */
    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSearch.setEnabled(!loading);
    }

    /** مدیریت خطاهای شبکه، اینترنت یا پاسخ ناموفق سرور. */
    private void showNetworkError(VolleyError error) {
        setLoading(false);

        if (error != null && error.networkResponse != null) {
            tvStatus.setText("خطای سرور؛ کد HTTP: " + error.networkResponse.statusCode);
        } else {
            tvStatus.setText("ارتباط با وب سرویس برقرار نشد؛ اینترنت دستگاه را بررسی کنید.");
        }
    }
}
