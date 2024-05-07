package id.dkon.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;
import java.util.Locale;

import github.ankushsachdeva.emojicon.EmojiconTextView;
import id.dkon.app.ProfileActivity;
import id.dkon.app.R;
import id.dkon.app.constants.Constants;
import id.dkon.app.model.BalanceItem;
import id.dkon.app.model.Chat;
import id.dkon.app.model.Notify;

public class BalanceHistoryListAdapter extends RecyclerView.Adapter<BalanceHistoryListAdapter.ViewHolder> implements Constants {

    private Context ctx;
    private List<BalanceItem> items;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {

        void onItemClick(View view, BalanceItem item, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {

        this.mOnItemClickListener = mItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView credits, time;
        public LinearLayout parent;
        public EmojiconTextView message;

        public ViewHolder(View view) {

            super(view);

            credits = (TextView) view.findViewById(R.id.credits);
            message = (EmojiconTextView) view.findViewById(R.id.message);
            time = (TextView) view.findViewById(R.id.time);
            parent = (LinearLayout) view.findViewById(R.id.parent);
        }
    }

    public BalanceHistoryListAdapter(Context mContext, List<BalanceItem> items) {

        this.ctx = mContext;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_balance_history_list_row, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final BalanceItem item = items.get(position);

        if (item.getPaymentAction() == PA_BUY_CREDITS || item.getPaymentAction() == PA_BUY_REGISTRATION_BONUS || item.getPaymentAction() == PA_BUY_OTP_VERIFICATION || item.getPaymentAction() == PA_BUY_REFERRAL_BONUS) {

            holder.credits.setText("+" + Integer.toString(item.getCreditsCount()) + " " + ctx.getString(R.string.label_credits));

        } else {

            holder.credits.setText("-" + Integer.toString(item.getCreditsCount()) + " " + ctx.getString(R.string.label_credits));
        }

        switch (item.getPaymentAction()) {

            case PA_BUY_CREDITS: {

                switch (item.getPaymentType()) {

                    case PT_CARD: {

                        holder.message.setText(ctx.getString(R.string.label_payments_credits_stripe));

                        break;
                    }

                    case PT_GOOGLE_PURCHASE: {

                        holder.message.setText(ctx.getString(R.string.label_payments_credits_android));

                        break;
                    }

                    case PT_APPLE_PURCHASE: {

                        holder.message.setText(ctx.getString(R.string.label_payments_credits_ios));

                        break;
                    }

                    case PT_ADMOB_REWARDED_ADS: {

                        holder.message.setText(ctx.getString(R.string.label_payments_credits_admob));

                        break;
                    }
                }

                break;
            }

            case PA_BUY_GIFT: {

                holder.message.setText(ctx.getString(R.string.label_payments_send_gift));

                break;
            }

            case PA_BUY_VERIFIED_BADGE: {

                holder.message.setText(ctx.getString(R.string.label_payments_verified_badge));

                break;
            }

            case PA_BUY_GHOST_MODE: {

                holder.message.setText(ctx.getString(R.string.label_payments_ghost_mode));

                break;
            }

            case PA_BUY_DISABLE_ADS: {

                holder.message.setText(ctx.getString(R.string.label_payments_off_admob));

                break;
            }

            case PA_BUY_REGISTRATION_BONUS: {

                holder.message.setText(ctx.getString(R.string.label_payments_registration_bonus));

                break;
            }

            case PA_BUY_REFERRAL_BONUS: {

                holder.message.setText(ctx.getString(R.string.label_payments_referral_bonus));

                break;
            }

            case PA_BUY_OTP_VERIFICATION: {

                holder.message.setText(ctx.getString(R.string.label_payments_otp_verification_bonus));

                break;
            }
        }

        holder.time.setText(item.getDate());
    }

    public BalanceItem getItem(int position) {

        return items.get(position);
    }

    @Override
    public int getItemCount() {

        return items.size();
    }

    public interface OnClickListener {

        void onItemClick(View view, Chat item, int pos);
    }
}