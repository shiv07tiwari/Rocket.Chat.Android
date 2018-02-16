package chat.rocket.android.widget.emoji

import android.support.v4.view.PagerAdapter
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.widget.emoji.EmojiBottomPicker.OnEmojiClickCallback
import java.util.*

class CategoryPagerAdapter(val callback: OnEmojiClickCallback) : PagerAdapter() {
    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(container.context)
                .inflate(R.layout.emoji_category_layout, container, false)
        val layoutManager = GridLayoutManager(view.context, 5)
        val recycler = view.findViewById(R.id.emojiRecyclerView) as RecyclerView
        val adapter = EmojiAdapter(layoutManager.spanCount, callback)
        val category = EmojiCategory.values().get(position)
        val emojis = if (category != EmojiCategory.RECENTS)
            EmojiLoader.getEmojisByCategory(category)
        else
            EmojiLoader.getRecents()
        adapter.addEmojis(emojis)
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.adapter = adapter
        recycler.isNestedScrollingEnabled = false
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }

    override fun getCount() = EmojiCategory.values().size

    override fun getPageTitle(position: Int) = EmojiCategory.values()[position].icon()


    class EmojiAdapter(val spanCount: Int, val callback: OnEmojiClickCallback) : RecyclerView.Adapter<EmojiRowViewHolder>() {
        private var emojis: List<Emoji> = Collections.emptyList()

        fun addEmojis(emojis: List<Emoji>) {
            this.emojis = emojis
            notifyItemRangeInserted(0, emojis.size)
        }

        override fun onBindViewHolder(holder: EmojiRowViewHolder, position: Int) {
            holder.bind(emojis[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiRowViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.emoji_row_item, parent, false)
            return EmojiRowViewHolder(view, itemCount, spanCount, callback)
        }

        override fun getItemCount(): Int = emojis.size
    }

    class EmojiRowViewHolder(itemView: View, val itemCount: Int, val spanCount: Int, val callback: OnEmojiClickCallback) : RecyclerView.ViewHolder(itemView) {
        private val emojiView: TextView = itemView.findViewById(R.id.emoji)

        fun bind(emoji: Emoji) {
            val context = itemView.context
            emojiView.text = EmojiParser.parse(emoji.unicode)
            val remainder = itemCount % spanCount
            val lastLineItemCount = if (remainder == 0) spanCount else remainder
            val paddingBottom = context.resources.getDimensionPixelSize(R.dimen.picker_padding_bottom)
            if (adapterPosition >= itemCount - lastLineItemCount) {
                itemView.setPadding(0, 0, 0, paddingBottom)
            }
            itemView.setOnClickListener {
                callback.onEmojiAdded(emoji)
            }
        }
    }
}