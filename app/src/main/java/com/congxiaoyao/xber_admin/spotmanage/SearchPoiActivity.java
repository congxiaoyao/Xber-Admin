package com.congxiaoyao.xber_admin.spotmanage;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.congxiaoyao.adapter.base.binding.BindingAdapterHelper;
import com.congxiaoyao.adapter.base.binding.annotations.ItemLayout;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.databinding.ActivitySearchPoiBinding;
import com.congxiaoyao.xber_admin.databinding.ItemPoiSuggestionBinding;
import com.congxiaoyao.xber_admin.utils.DisplayUtils;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.List;

import static com.baidu.mapapi.search.sug.SuggestionResult.*;

public class SearchPoiActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ActivitySearchPoiBinding binding;
    private BaseQuickAdapter adapter;
    private SuggestionSearch search;

    private ContentLoadingProgressBar progressBar;
    private List<SuggestionInfo> suggestions;

    public static final String EXTRA_KEY = "SPOT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_poi);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("搜索地点");

        progressBar = (ContentLoadingProgressBar) binding.getRoot().findViewById(R.id.content_progress_bar);

        adapter = BindingAdapterHelper.create(binding.recyclerView)
                .with(new LinearLayoutManager(this)).setBindingAdapter(this);
        binding.recyclerView.addItemDecoration(new HorizontalDividerItemDecoration
                .Builder(this)
                .margin(DisplayUtils.dp2px(this, 16))
                .size(1)
                .colorResId(R.color.colorLightGray)
                .build());
        binding.recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                SuggestionInfo info = suggestions.get(position);
                LatLng pt = info.pt;
                if (pt == null) return;
                Intent intent = new Intent();
                intent.putExtra(EXTRA_KEY, info);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        search = SuggestionSearch.newInstance();
        search.setOnGetSuggestionResultListener(new OnGetSuggestionResultListener() {
            @Override
            public void onGetSuggestionResult(SuggestionResult suggestionResult) {
                hideLoading();
                if (suggestionResult == null || suggestionResult.getAllSuggestions() == null) {
                    showEmptyDataView();
                    return;
                }
                suggestions = suggestionResult.getAllSuggestions();
                adapter.setNewData(suggestions);
            }
        });
    }

    @ItemLayout(R.layout.item_poi_suggestion)
    public void bindItemData(ItemPoiSuggestionBinding binding, SuggestionInfo bean) {
        binding.setBean(bean);
        binding.executePendingBindings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_search);
        menuItem.collapseActionView();
        menuItem.expandActionView();
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(SearchPoiActivity.this);
        boolean b = super.onCreateOptionsMenu(menu);
        return b;
    }

    @Override
    public void supportNavigateUpTo(@NonNull Intent upIntent) {
        super.onBackPressed();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        hindEmptyDataView();
        if (query == null || query.isEmpty()) return false;
        search.requestSuggestion(new SuggestionSearchOption().keyword(query).city("天津"));
        showLoading();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        onQueryTextSubmit(newText);
        return true;
    }


    public void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideLoading() {
        if (progressBar != null) {
            progressBar.hide();
        }
    }

    public void showEmptyDataView() {
        binding.tvEmpty.setVisibility(View.VISIBLE);
    }

    public void hindEmptyDataView() {
        binding.tvEmpty.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        search.destroy();
    }
}
