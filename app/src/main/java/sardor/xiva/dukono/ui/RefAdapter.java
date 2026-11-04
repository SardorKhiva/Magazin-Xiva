package sardor.xiva.dukono.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import sardor.xiva.dukono.R;
import sardor.xiva.dukono.data.db.RefItem;
import sardor.xiva.dukono.databinding.ItemRefBinding;

/** Birliklar/kategoriyalar ro'yxati. */
public class RefAdapter extends ListAdapter<RefItem, RefAdapter.VH> {

    public interface Listener {
        void onEdit(RefItem item);

        void onDelete(RefItem item);
    }

    private static final DiffUtil.ItemCallback<RefItem> DIFF =
            new DiffUtil.ItemCallback<RefItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull RefItem a, @NonNull RefItem b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull RefItem a, @NonNull RefItem b) {
                    return a.id == b.id
                            && a.usageCount == b.usageCount
                            && a.allowFraction == b.allowFraction
                            && (a.name == null ? b.name == null : a.name.equals(b.name));
                }
            };

    private final Listener listener;
    private final boolean unitMode;

    public RefAdapter(Listener listener, boolean unitMode) {
        super(DIFF);
        this.listener = listener;
        this.unitMode = unitMode;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRefBinding b = ItemRefBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position));
    }

    class VH extends RecyclerView.ViewHolder {
        private final ItemRefBinding b;

        VH(ItemRefBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(RefItem item) {
            b.name.setText(item.name);
            String usage = b.getRoot().getContext().getString(R.string.usage_count, item.usageCount);
            if (unitMode) {
                String kind = b.getRoot().getContext().getString(
                        item.allowFraction ? R.string.fraction_yes : R.string.fraction_no);
                b.subtitle.setText(kind + " · " + usage);
            } else {
                b.subtitle.setText(usage);
            }
            b.getRoot().setOnClickListener(v -> listener.onEdit(item));
            b.editButton.setOnClickListener(v -> listener.onEdit(item));
            b.deleteButton.setOnClickListener(v -> listener.onDelete(item));
            b.getRoot().setOnLongClickListener(v -> {
                listener.onDelete(item);
                return true;
            });
            b.getRoot().setOnClickListener(v -> listener.onEdit(item));
        }
    }
}
