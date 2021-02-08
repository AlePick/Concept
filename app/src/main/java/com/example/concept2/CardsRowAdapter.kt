package com.example.concept2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class CardsRowAdapter (private var cardsRows:List<CardsRow>, private val context: Context):
    RecyclerView.Adapter<CardsRowAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: CardsRowAdapter.ViewHolder, position: Int) {
        val card = cardsRows[position]
        Picasso.get().load(card.imageUrl1).into(holder.card1View)
        Picasso.get().load(card.imageUrl2).into(holder.card2View)
        Picasso.get().load(card.imageUrl3).into(holder.card3View)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardsRowAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.cards_row,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = cardsRows.size


    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val card1View: ImageView = view.findViewById(R.id.cardView)
        val card2View: ImageView = view.findViewById(R.id.cardView2)
        val card3View: ImageView = view.findViewById(R.id.cardView3)
    }
}