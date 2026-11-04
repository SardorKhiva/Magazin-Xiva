package sardor.xiva.dukono.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;

import java.util.List;

import sardor.xiva.dukono.R;
import sardor.xiva.dukono.databinding.ActivityMainBinding;
import sardor.xiva.dukono.data.db.ProductListItem;
import sardor.xiva.dukono.data.db.SearchHistory;
import sardor.xiva.dukono.util.AppLock;
import sardor.xiva.dukono.util.AppPrefs;
import sardor.xiva.dukono.util.SimpleWatcher;
import sardor.xiva.dukono.util.Ui;

/** Bosh ekran: qidiruv. */
public class MainActivity extends AppCompatActivity implements ProductAdapter.Listener {

    private ActivityMainBinding b;
    private ProductAdapter adapter;
    private SearchViewModel viewModel;
    private boolean wasLocked = true;
    private List<SearchHistory> lastHistory = java.util.Collections.emptyList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });

        adapter = new ProductAdapter(this);
        b.list.setLayoutManager(new LinearLayoutManager(this));
        b.list.setAdapter(adapter);
        b.list.setItemAnimator(null);

        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        b.searchInput.addTextChangedListener(new SimpleWatcher() {
            @Override
            protected void onChanged(String text) {
                viewModel.search(text, false);
                updateViews(text, adapter.getCurrentList());
            }
        });

        b.searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.saveHistory(currentQuery());
                Ui.hideKeyboard(this);
                return true;
            }
            return false;
        });

        b.clearHistoryButton.setOnClickListener(v -> viewModel.clearHistory());
        b.fab.setOnClickListener(v -> openEdit(0, null, null));
        b.addFromSearch.setOnClickListener(v -> {
            String q = currentQuery().trim();
            if (q.isEmpty()) {
                openEdit(0, null, null);
            } else if (q.matches("\\d+")) {
                openEdit(0, null, q);
            } else {
                openEdit(0, q, null);
            }
        });

        viewModel.getResults().observe(this, result -> {
            adapter.submitList(result.items);
            updateViews(currentQuery(), result.items);
        });

        viewModel.getHistory().observe(this, history -> {
            lastHistory = history == null ? java.util.Collections.emptyList() : history;
            renderHistory(lastHistory);
        });

        viewModel.getTotalCount().observe(this, count ->
                b.totalText.setText(getString(R.string.total_count, count == null ? 0 : count)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setConfig(AppPrefs.getLowStockLimit(), AppPrefs.getExpiryWarningDays());
        viewModel.refresh();

        if (AppLock.isUnlocked()) {
            if (wasLocked) {
                b.searchInput.postDelayed(() -> {
                    if (!isFinishing() && AppLock.isUnlocked()) {
                        Ui.showKeyboard(b.searchInput);
                    }
                }, 250);
            }
            wasLocked = false;
        } else {
            wasLocked = true;
        }
    }

    private String currentQuery() {
        return b.searchInput.getText() == null ? "" : b.searchInput.getText().toString();
    }

    private void updateViews(String query, List<ProductListItem> items) {
        boolean emptyQuery = query == null || query.trim().isEmpty();
        boolean hasItems = items != null && !items.isEmpty();

        b.historyPanel.setVisibility(emptyQuery && !lastHistory.isEmpty() ? View.VISIBLE : View.GONE);
        b.list.setVisibility(emptyQuery ? View.GONE : View.VISIBLE);
        b.emptyState.setVisibility(!emptyQuery && !hasItems ? View.VISIBLE : View.GONE);
    }

    private void renderHistory(List<SearchHistory> history) {
        b.historyChips.removeAllViews();
        for (SearchHistory h : history) {
            Chip chip = new Chip(this);
            chip.setText(h.queryText);
            chip.setCheckable(false);
            chip.setChipBackgroundColorResource(R.color.chip_bg);
            chip.setOnClickListener(v -> {
                b.searchInput.setText(h.queryText);
                b.searchInput.setSelection(h.queryText.length());
                viewModel.saveHistory(h.queryText);
            });
            b.historyChips.addView(chip);
        }
        boolean emptyQuery = currentQuery().trim().isEmpty();
        b.historyPanel.setVisibility(emptyQuery && !history.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openEdit(long id, String prefillName, String prefillCode) {
        Intent intent = new Intent(this, ProductEditActivity.class);
        if (id > 0) intent.putExtra(ProductEditActivity.EXTRA_ID, id);
        if (prefillName != null) intent.putExtra(ProductEditActivity.EXTRA_NAME, prefillName);
        if (prefillCode != null) intent.putExtra(ProductEditActivity.EXTRA_CODE, prefillCode);
        startActivity(intent);
    }

    @Override
    public void onOpen(ProductListItem item) {
        viewModel.saveHistory(currentQuery());
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.EXTRA_ID, item.id);
        startActivity(intent);
    }

    @Override
    public void onEdit(ProductListItem item) {
        openEdit(item.id, null, null);
    }
}
