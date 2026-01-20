package com.capyreader.app.ui.articles.list

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.capyreader.app.R
import com.capyreader.app.preferences.RowSwipeOption
import com.capyreader.app.ui.articles.ArticleActions
import com.google.android.material.R as MaterialR
import com.jocmp.capy.Article
import kotlin.math.abs

class ArticleItemTouchHelper(
    private val adapter: ArticlePagingAdapter,
    private val getSwipeStart: () -> RowSwipeOption,
    private val getSwipeEnd: () -> RowSwipeOption,
    private val articleActions: () -> ArticleActions,
    private val openLink: (Uri) -> Unit,
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val paint = Paint()
    private val iconPadding = 48f

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.25f

    override fun getSwipeDirs(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val startEnabled = getSwipeStart() != RowSwipeOption.DISABLED
        val endEnabled = getSwipeEnd() != RowSwipeOption.DISABLED

        var dirs = 0
        if (startEnabled) dirs = dirs or ItemTouchHelper.RIGHT
        if (endEnabled) dirs = dirs or ItemTouchHelper.LEFT

        return dirs
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        val article = adapter.getArticle(position) ?: return

        when (direction) {
            ItemTouchHelper.LEFT -> executeAction(getSwipeEnd(), article)
            ItemTouchHelper.RIGHT -> executeAction(getSwipeStart(), article)
        }

        adapter.notifyItemChanged(position)
    }

    private fun executeAction(option: RowSwipeOption, article: Article) {
        val actions = articleActions()

        when (option) {
            RowSwipeOption.TOGGLE_READ -> {
                if (article.read) {
                    actions.markUnread(article.id)
                } else {
                    actions.markRead(article.id)
                }
            }
            RowSwipeOption.TOGGLE_STARRED -> {
                if (article.starred) {
                    actions.unstar(article.id)
                } else {
                    actions.star(article.id)
                }
            }
            RowSwipeOption.OPEN_EXTERNALLY -> {
                val url = article.url ?: return
                actions.markRead(article.id)
                openLink(Uri.parse(url.toString()))
            }
            RowSwipeOption.DISABLED -> {}
        }
    }

    private fun getThemeColor(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        val itemView = viewHolder.itemView
        val position = viewHolder.bindingAdapterPosition
        val article = adapter.getArticle(position)

        val context = recyclerView.context
        val backgroundColor = getThemeColor(context, MaterialR.attr.colorSurfaceContainerHighest)
        paint.color = backgroundColor

        val option = if (dX > 0) getSwipeStart() else getSwipeEnd()
        val iconRes = getIconForOption(option, article)

        if (dX > 0) {
            val rect = RectF(
                itemView.left.toFloat(),
                itemView.top.toFloat(),
                dX,
                itemView.bottom.toFloat()
            )
            c.drawRect(rect, paint)

            if (iconRes != 0) {
                val drawable = ContextCompat.getDrawable(context, iconRes)
                drawable?.let {
                    val wrappedDrawable = DrawableCompat.wrap(it.mutate())
                    val iconColor = getThemeColor(context, MaterialR.attr.colorOnSurfaceVariant)
                    DrawableCompat.setTint(wrappedDrawable, iconColor)

                    val iconSize = (24 * context.resources.displayMetrics.density).toInt()
                    val iconMargin = iconPadding.toInt()
                    val iconTop = itemView.top + (itemView.height - iconSize) / 2
                    val iconLeft = itemView.left + iconMargin

                    if (abs(dX) > iconMargin + iconSize) {
                        wrappedDrawable.setBounds(
                            iconLeft,
                            iconTop,
                            iconLeft + iconSize,
                            iconTop + iconSize
                        )
                        wrappedDrawable.draw(c)
                    }
                }
            }
        } else if (dX < 0) {
            val rect = RectF(
                itemView.right.toFloat() + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )
            c.drawRect(rect, paint)

            if (iconRes != 0) {
                val drawable = ContextCompat.getDrawable(context, iconRes)
                drawable?.let {
                    val wrappedDrawable = DrawableCompat.wrap(it.mutate())
                    val iconColor = getThemeColor(context, MaterialR.attr.colorOnSurfaceVariant)
                    DrawableCompat.setTint(wrappedDrawable, iconColor)

                    val iconSize = (24 * context.resources.displayMetrics.density).toInt()
                    val iconMargin = iconPadding.toInt()
                    val iconTop = itemView.top + (itemView.height - iconSize) / 2
                    val iconRight = itemView.right - iconMargin

                    if (abs(dX) > iconMargin + iconSize) {
                        wrappedDrawable.setBounds(
                            iconRight - iconSize,
                            iconTop,
                            iconRight,
                            iconTop + iconSize
                        )
                        wrappedDrawable.draw(c)
                    }
                }
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun getIconForOption(option: RowSwipeOption, article: Article?): Int {
        return when (option) {
            RowSwipeOption.TOGGLE_READ -> {
                if (article?.read == true) {
                    R.drawable.icon_circle_filled
                } else {
                    R.drawable.icon_circle_outline
                }
            }
            RowSwipeOption.TOGGLE_STARRED -> {
                if (article?.starred == true) {
                    R.drawable.icon_star_outline
                } else {
                    R.drawable.icon_star_filled
                }
            }
            RowSwipeOption.OPEN_EXTERNALLY -> R.drawable.icon_open_in_new
            RowSwipeOption.DISABLED -> 0
        }
    }
}
