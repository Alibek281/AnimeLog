package com.alib.myanimelist.ui.Details;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alib.myanimelist.Database.AnimeDatabaseHelper;
import com.alib.myanimelist.R;
import com.alib.myanimelist.ui.FavAnime.FavAnimeFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import net.sandrohc.jikan.Jikan;
import net.sandrohc.jikan.exception.JikanQueryException;
import net.sandrohc.jikan.model.anime.Anime;

import io.netty.resolver.DefaultAddressResolverGroup;

public class DetailsActivity extends AppCompatActivity {
    private AnimeDatabaseHelper dbHelper;
    private int malId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        TextView titleTextView = findViewById(R.id.detail_anime_title);
        TextView descriptionTextView = findViewById(R.id.animeDescription);
        ImageView bannerImageView = findViewById(R.id.detail_anime_image);
        FloatingActionButton addToFavBtn = findViewById(R.id.addToFav);

        dbHelper = new AnimeDatabaseHelper(getApplicationContext());

        Intent intent = getIntent();
        malId = intent.getIntExtra("malId",1);


        Jikan jikan = new Jikan.JikanBuilder()
                .httpClientCustomizer(httpClient -> httpClient.resolver(DefaultAddressResolverGroup.INSTANCE))
                .build();

        try {
            Anime anime = jikan.query().anime().get(malId)
                    .execute()
                    .block();

            String description = anime.getSynopsis() + "\n\n\n" +  anime.getBackground() ;
            String title = anime.getTitle();
            String bannerImageUrl = anime.getImages().getPreferredImageUrl();

            titleTextView.setText(title);
            descriptionTextView.setText(description);
            Picasso.get().load(bannerImageUrl).into(bannerImageView);

            addToFavBtn.setOnClickListener(v -> {
                dbHelper.updateAnime(anime);
                dbHelper.close();

                Intent databaseUpdatedIntent = new Intent(FavAnimeFragment.ACTION_DATABASE_UPDATED);
                getApplicationContext().sendBroadcast(databaseUpdatedIntent);
            });
        } catch (JikanQueryException e) {
            throw new RuntimeException(e);
        }

        titleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", titleTextView.getText().toString());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(getApplicationContext(),"Copied", Toast.LENGTH_SHORT).show();
            }
        });

    }
}

