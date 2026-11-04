package sardor.xiva.dukono.ui;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Collections;
import java.util.List;

import sardor.xiva.dukono.data.Repo;
import sardor.xiva.dukono.data.db.ProductListItem;
import sardor.xiva.dukono.data.db.SearchHistory;
import sardor.xiva.dukono.util.AppExecutors;

/** Qidiruv natijalari, tarix va umumiy son. */
public class SearchViewModel extends ViewModel {

    public static final class SearchResult {
        public final String query;
        public final List<ProductListItem> items;

        public SearchResult(String query, List<ProductListItem> items) {
            this.query = query;
            this.items = items;
        }
    }

    private static final long DEBOUNCE_MS = 200L;

    private final MutableLiveData<SearchResult> results = new MutableLiveData<>();
    private final MutableLiveData<List<SearchHistory>> history = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalCount = new MutableLiveData<>();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable pendingSearch = this::runSearch;

    private String currentQuery = "";
    private int generation = 0;

    public LiveData<SearchResult> getResults() {
        return results;
    }

    public LiveData<List<SearchHistory>> getHistory() {
        return history;
    }

    public LiveData<Integer> getTotalCount() {
        return totalCount;
    }

    public void search(String query, boolean immediate) {
        currentQuery = query == null ? "" : query;
        handler.removeCallbacks(pendingSearch);
        if (immediate) {
            runSearch();
        } else {
            handler.postDelayed(pendingSearch, DEBOUNCE_MS);
        }
    }

    private void runSearch() {
        final String query = currentQuery;
        final int gen = ++generation;
        if (query.trim().isEmpty()) {
            results.setValue(new SearchResult(query, Collections.emptyList()));
            return;
        }
        AppExecutors.io(() -> {
            List<ProductListItem> items = Repo.get().search(query);
            AppExecutors.main(() -> {
                if (gen == generation && query.equals(currentQuery)) {
                    results.setValue(new SearchResult(query, items));
                }
            });
        });
    }

    /** Ekranga qaytganda ro'yxat, tarix va umumiy sonni yangilaydi. */
    public void refresh() {
        search(currentQuery, true);
        loadTotalCount();
        loadHistory();
    }

    public void loadTotalCount() {
        AppExecutors.io(() -> {
            int count = Repo.get().countProducts();
            totalCount.postValue(count);
        });
    }

    public void loadHistory() {
        AppExecutors.io(() -> {
            List<SearchHistory> list = Repo.get().recentHistory(20);
            history.postValue(list);
        });
    }

    public void saveHistory(String query) {
        if (query == null || query.trim().isEmpty()) return;
        AppExecutors.io(() -> {
            Repo.get().addHistory(query);
            List<SearchHistory> list = Repo.get().recentHistory(20);
            history.postValue(list);
        });
    }

    public void clearHistory() {
        AppExecutors.io(() -> {
            Repo.get().clearHistory();
            List<SearchHistory> list = Repo.get().recentHistory(20);
            history.postValue(list);
        });
    }

    @Override
    protected void onCleared() {
        handler.removeCallbacks(pendingSearch);
    }
}
