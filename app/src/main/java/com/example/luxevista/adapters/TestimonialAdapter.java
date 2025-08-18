package com.example.luxevista.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luxevista.R;
import com.example.luxevista.models.Testimonial;

import java.util.List;

public class TestimonialAdapter extends RecyclerView.Adapter<TestimonialAdapter.TestimonialViewHolder> {

    private Context context;
    private List<Testimonial> testimonials;

    public TestimonialAdapter(Context context, List<Testimonial> testimonials) {
        this.context = context;
        this.testimonials = testimonials;
    }

    @NonNull
    @Override
    public TestimonialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_testimonial, parent, false);
        return new TestimonialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestimonialViewHolder holder, int position) {
        Testimonial testimonial = testimonials.get(position);
        
        holder.tvUserName.setText(testimonial.getUserName());
        holder.tvComment.setText("\"" + testimonial.getComment() + "\"");
        holder.tvDate.setText(testimonial.getFormattedDate());
        
        // Show star rating
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < testimonial.getRating(); i++) {
            stars.append("★");
        }
        holder.tvRating.setText(stars.toString());
    }

    @Override
    public int getItemCount() {
        return testimonials.size();
    }

    public void updateTestimonials(List<Testimonial> newTestimonials) {
        this.testimonials = newTestimonials;
        notifyDataSetChanged();
    }

    static class TestimonialViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvComment, tvDate, tvRating;

        public TestimonialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvTestimonialUserName);
            tvComment = itemView.findViewById(R.id.tvTestimonialComment);
            tvDate = itemView.findViewById(R.id.tvTestimonialDate);
            tvRating = itemView.findViewById(R.id.tvTestimonialRating);
        }
    }
}
