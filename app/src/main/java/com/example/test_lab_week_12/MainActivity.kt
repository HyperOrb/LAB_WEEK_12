package com.example.test_lab_week_12

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.example.test_lab_week_12.MovieAdapter
import com.example.test_lab_week_12.model.Movie
import com.example.test_lab_week_12.MovieApplication
import com.example.lab_week_12.MovieViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity(), MovieAdapter.MovieClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.movie_list)
        val movieAdapter = MovieAdapter(this)
        recyclerView.adapter = movieAdapter

        val movieRepository = (application as MovieApplication).movieRepository
        val movieViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MovieViewModel(movieRepository) as T
                }
            }
        )[MovieViewModel::class.java]

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                launch {
                    movieViewModel.popularMovies.collect { movies: List<Movie> ->
                        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
                        movieAdapter.addMovies(
                            movies
                                .filter { movie: Movie -> movie.releaseDate?.startsWith(currentYear) == true }
                                .sortedByDescending { movie: Movie -> movie.popularity }
                        )
                    }
                }
                launch {
                    movieViewModel.error.collect { errorMessage: String ->
                        if (errorMessage.isNotEmpty()) {
                            Snackbar.make(recyclerView, errorMessage, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    override fun onMovieClick(movie: Movie) {
        val intent = Intent(this, DetailsActivity::class.java).apply {
            putExtra(DetailsActivity.EXTRA_TITLE, movie.title)
            putExtra(DetailsActivity.EXTRA_RELEASE, movie.releaseDate)
            putExtra(DetailsActivity.EXTRA_OVERVIEW, movie.overview)
            putExtra(DetailsActivity.EXTRA_POSTER, movie.posterPath)
        }
        startActivity(intent)
    }
}
