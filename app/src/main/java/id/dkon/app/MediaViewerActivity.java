package id.dkon.app;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import id.dkon.app.adapter.MediaViewerAdapter;
import id.dkon.app.app.App;
import id.dkon.app.common.ActivityBase;
import id.dkon.app.model.MediaItem;
import id.dkon.app.util.CustomRequest;

public class MediaViewerActivity extends ActivityBase {

    private MediaViewerAdapter adapter;
    private ViewPager mViewPager;
    private TextView mTextView;

    private ArrayList<MediaItem> images;

    private long itemId = 0;
    private int count = 0, total = 0, position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_media_viewer);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mTextView = (TextView) findViewById(R.id.textView);

        Intent i = getIntent();

        itemId = i.getLongExtra("itemId", 0);
        count = i.getIntExtra("count", 0);

        position = i.getIntExtra("position", 0);

        images = i.getParcelableArrayListExtra("images");
        adapter = new MediaViewerAdapter(this, images);

        mViewPager.setAdapter(adapter);

        mViewPager.setCurrentItem(position);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int pos, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int pos) {

                mTextView.setText(String.format(getString(R.string.image_of), (pos + 1), total));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        ((ImageButton) findViewById(R.id.btnClose)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                finish();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.black));
        }

        if (itemId != 0 && count > 0) {

            getMediaItems();
        }

        updateView();
    }

    private void updateView() {

        total = count + 1;

        mTextView.setText(String.format(getString(R.string.image_of), (position + 1), total));
    }

    private void getMediaItems() {

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, METHOD_ITEM_GET_IMAGES, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            int arrayLength = 0;

                            if (!response.getBoolean("error")) {

                                if (response.has("items")) {

                                    JSONArray itemsArray = response.getJSONArray("items");

                                    arrayLength = itemsArray.length();

                                    if (arrayLength > 0) {

                                        for (int i = 0; i < itemsArray.length(); i++) {

                                            JSONObject itemObj = (JSONObject) itemsArray.get(i);

                                            MediaItem item = new MediaItem();

                                            item.setImageUrl(itemObj.getString("imgUrl"));
                                            item.setType(0);

                                            images.add(item);
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                            adapter.notifyDataSetChanged();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                adapter.notifyDataSetChanged();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("accountId", Long.toString(App.getInstance().getId()));
                params.put("accessToken", App.getInstance().getAccessToken());
                params.put("itemId", Long.toString(itemId));
                params.put("language", "en");

                return params;
            }
        };

        App.getInstance().addToRequestQueue(jsonReq);
    }
}

