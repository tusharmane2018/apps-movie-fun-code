package org.superbiz.moviefun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

import java.util.Map;

@Controller
public class HomeController {

    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;
    private final PlatformTransactionManager albumsTransactionManager;
    private final PlatformTransactionManager moviesTransactionManager;

    @Autowired
    public HomeController(MoviesBean moviesBean, AlbumsBean albumsBean, MovieFixtures movieFixtures, AlbumFixtures albumFixtures,
                          @Qualifier("getAlbumsPlatformTransactionManager") PlatformTransactionManager albumsTransactionManager,
                          @Qualifier("getMoviesPlatformTransactionManager")PlatformTransactionManager moviesTransactionManager) {
        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;
        this.albumsTransactionManager = albumsTransactionManager;
        this.moviesTransactionManager = moviesTransactionManager;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/setup")
    public String setup(Map<String, Object> model) {
        TransactionStatus movieTransactionStatus = moviesTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            for (Movie movie : movieFixtures.load()) {
                moviesBean.addMovie(movie);
            }
            moviesTransactionManager.commit(movieTransactionStatus);
        } catch (Exception e) {
            moviesTransactionManager.rollback(movieTransactionStatus);
        }

        TransactionStatus albumTransactionStatus = albumsTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            for (Album album : albumFixtures.load()) {
                albumsBean.addAlbum(album);
            }
            albumsTransactionManager.commit(albumTransactionStatus);
        } catch (Exception e) {
            albumsTransactionManager.rollback(albumTransactionStatus);
        }

        model.put("movies", moviesBean.getMovies());
        model.put("albums", albumsBean.getAlbums());

        return "setup";
    }
}
