package sardor.xiva.dukono.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import sardor.xiva.dukono.R;
import sardor.xiva.dukono.databinding.ItemProductBinding;
import sardor.xiva.dukono.data.db.ProductListItem;
import sardor.xiva.dukono.util.Fmt;

/** Qidiruv natijalari ro'yxati. */
public class ProductAdapter extends ListAdapter<ProductListItem, ProductAdapter.VH> {

    public interface Listener {
        void onOpen(ProductListItem item);

        void onEdit(ProductListItem item);
    }

    private static final DiffUtil.ItemCallback<ProductListItem> DIFF =
            new DiffUtil.ItemCallback<ProductListItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull ProductListItem a, @NonNull ProductListItem b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull ProductListItem a, @NonNull ProductListItem b) {
                    return a.price == b.price
                            && Double.compare(a.balance, b.balance) == 0
                            && Double.compare(a.initialQty, b.initialQty) == 0
                            && equal(a.name, b.name)
                            && equal(a.code, b.code)
                            && equal(a.unitName, b.unitName)
                            && a.allowFraction == b.allowFraction
                            && equal(a.expiryDate, b.expiryDate);
                }
            };

    private static boolean equal(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    private final Listener listener;
    private float lowStockLimit = 5f;
    private int expiryWarningDays = 30;
    private long today = Fmt.todayEpochDay();

    public ProductAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    /** Sozlamalar o'zgargan bo'lsa qayta chizadi (DiffUtil rangni sezmaydi). */
    public void setConfig(float lowStockLimit, int expiryWarningDays) {
        long newToday = Fmt.todayEpochDay();
        if (this.lowStockLimit == lowStockLimit
                && this.expiryWarningDays == expiryWarningDays
                && this.today == newToday) {
            return;
        }
        this.lowStockLimit = lowStockLimit;
        this.expiryWarningDays = expiryWarningDays;
        this.today = newToday;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding b = ItemProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position));
    }

    class VH extends RecyclerView.ViewHolder {
        private final ItemProductBinding b;

        VH(ItemProductBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(ProductListItem item) {
            Context c = b.getRoot().getContext();
            b.name.setText(item.name);
            b.code.setText(c.getString(R.string.code_value, item.code));
            b.price.setText(c.getString(R.string.price_value,
                    c.getString(R.string.money_format, Fmt.money(item.price))));
            String unit = item.unitName == null ? "" : item.unitName;
            b.qty.setText(c.getString(R.string.qty_value,
                    Fmt.qty(item.initialQty, item.allowFraction) + " " + unit));
            b.balance.setText(c.getString(R.string.balance_value,
                    Fmt.qty(item.balance, item.allowFraction) + " " + unit));

            // Qoldiq rangi (fon).
            int bgColor;
            if (item.balance <= 0) {
                bgColor = ContextCompat.getColor(c, R.color.no_stock_bg);
            } else if (item.balance <= lowStockLimit) {
                bgColor = ContextCompat.getColor(c, R.color.low_stock_bg);
            } else {
                bgColor = ContextCompat.getColor(c, android.R.color.transparent);
            }
            b.getRoot().setBackgroundTintList(ColorStateList.valueOf(bgColor));

            // Yaroqlilik muddati.
            Long expiry = item.expiryDate;
            if (expiry == null) {
                b.expiryChip.setVisibility(View.GONE);
                b.expiryLine.setVisibility(View.GONE);
            } else {
                long left = expiry - today;
                if (left < 0) {
                    b.expiryChip.setVisibility(View.VISIBLE);
                    b.expiryChip.setText(c.getString(R.string.expiry_overdue));
                    b.expiryChip.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(c, R.color.expired_chip)));
                    b.expiryLine.setVisibility(View.VISIBLE);
                    b.expiryLine.setBackgroundColor(
                            ContextCompat.getColor(c, R.color.expired_line));
                } else if (left <= expiryWarningDays) {
                    b.expiryChip.setVisibility(View.VISIBLE);
                    b.expiryChip.setText(left == 0
                            ? c.getString(R.string.expiry_today)
                            : c.getString(R.string.expiry_near, (int) left));
                    b.expiryChip.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(c, R.color.expiry_near_chip)));
                    b.expiryLine.setVisibility(View.VISIBLE);
                    b.expiryLine.setBackgroundColor(
                            ContextCompat.getColor(c, R.color.expiry_near_line));
                } else {
                    b.expiryChip.setVisibility(View.GONE);
                    b.expiryLine.setVisibility(View.GONE);
                }
            }

            b.getRoot().setOnClickListener(v -> listener.onOpen(item));
            b.editButton.setOnClickListener(v -> listener.onEdit(item));
        }
    }
}
