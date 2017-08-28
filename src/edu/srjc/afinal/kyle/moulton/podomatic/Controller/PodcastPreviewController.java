/*
Student: Kyle Moulton
Student Email: kylealexmoulton@gmail.com
Date: 5/23/2017
Project Name: AFinal - Podomatic
Course: CS 17.11 - Section 6991

Description: The PodcastPreviewController is the primary stage of the application. It contains the ListViews for the
PodcastSeries and associated PodcastEpisodes. It also contains the functionality for the mediaplayer object via
buttons and sliders to seek and adjust volume. Buttons are managed here to add new podcast series by rss feeds, set
the directory where episodes will be downloaded to, and fetch the most recent RSS feeds for each added series and add
new episodes. A ComboBox is used to select between displaying0 only downloaded, or all available episodes of the selected
series.
The Menubar options are defined here and include, save data, close, and about, which displays details about the application,
its author and their contact information (me).
*/

package edu.srjc.afinal.kyle.moulton.podomatic.Controller;

import java.io.*;
import java.net.URL;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Scanner;
import edu.srjc.afinal.kyle.moulton.podomatic.Model.Context;
import edu.srjc.afinal.kyle.moulton.podomatic.Model.PodcastEpisode;
import edu.srjc.afinal.kyle.moulton.podomatic.Model.PodcastSeries;
import com.sun.syndication.io.FeedException;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PodcastPreviewController implements Initializable
{
    private static final String savedSeriesFileName = "savedSeries.csv";

    @FXML
    private ComboBox<String> sortByComboBox;

    @FXML
    private ListView<PodcastSeries> series;

    @FXML
    private ListView<PodcastEpisode> episodes;

    @FXML
    private Label timeLabel;

    @FXML
    private Label playbackRateLabel;

    @FXML
    private Slider timeSlider;

    @FXML
    private Slider volSlider;

    @FXML
    private Button updateButton;

    @FXML
    private Button playButton;

    @FXML
    private Button stopButton;

    @FXML
    private Button backwardButton;

    @FXML
    private Button forwardButton;

    @FXML
    private Button skipForwardButton;

    @FXML
    private Button skipBackwardButton;

    @FXML
    private Button resetRateButton;

    @FXML
    private Label nowPlaying;

    private MediaPlayer mediaPlayer;

    private boolean atEndOfPodcast = false;

    private Background playButtonBackground;
    private Background stopButtonBackground;
    private Background forwardButtonBackground;
    private Background backwardButtonBackground;
    private Background pauseButtonBackground;
    private Background skipForwardButtonBackground;
    private Background skipBackwardButtonBackground;
    private Background resetRateButtonBackground;

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        if (Context.getInstance().getPodcastCollection().isEmpty())
        {
            populateSeriesFromFile(savedSeriesFileName);
        }

        Context.getInstance().setPrimaryController(this);

        instantiateImages();

        nowPlaying.setText("");

        playButton.setBackground(playButtonBackground);
        playButton.setDisable(true);
        stopButton.setBackground(stopButtonBackground);
        forwardButton.setBackground(forwardButtonBackground);
        backwardButton.setBackground(backwardButtonBackground);
        skipForwardButton.setBackground(skipForwardButtonBackground);
        skipBackwardButton.setBackground(skipBackwardButtonBackground);
        resetRateButton.setBackground(resetRateButtonBackground);

        sortByComboBox.getItems().addAll("All Available", "Downloaded");
        sortByComboBox.getSelectionModel().select("All Available");
        sortByComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                if (series.getSelectionModel().getSelectedItem() != null)
                {
                    showEpisodes(series.getSelectionModel().getSelectedItem());
                }
            }
        });

        // Concept of a custom listcell with listener taken from https://www.turais.de/how-to-custom-listview-cell-in-javafx/
        series.getItems().addAll(Context.getInstance().getPodcastCollection().values());
        series.setCellFactory(podcastSeries -> new SeriesListViewCellController());
        series.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            if (newValue != null)
            {
                showEpisodes(newValue);
            }

        });
    }

    public ListView<PodcastSeries> getSeries()
    {
        return series;
    }

    public ListView<PodcastEpisode> getEpisodes()
    {
        return episodes;
    }

    public MediaPlayer getMediaPlayer()
    {
        return mediaPlayer;
    }

    public Label getNowPlaying()
    {
        return nowPlaying;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer)
    {
        this.mediaPlayer = mediaPlayer;
    }

    // https://stackoverflow.com/questions/29984228/javafx-button-background-image
    // Images taken from https://www.vecteezy.com/
    public void instantiateImages()
    {
        playButtonBackground = new Background(new BackgroundImage(
                new Image(getClass().getResource("/edu/srjc/afinal/kyle/moulton/podomatic/View/playButton.png").toExternalForm(),
                        30, 30, true, true),
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT));
        stopButtonBackground = new Background(new BackgroundImage(
                new Image(getClass().getResource("/edu/srjc/afinal/kyle/moulton/podomatic/View/stopButton.png").toExternalForm(),
                        30, 30, true, true),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT));
        pauseButtonBackground = new Background(new BackgroundImage(
                new Image(getClass().getResource("/edu/srjc/afinal/kyle/moulton/podomatic/View/pauseButton.png").toExternalForm(),
                        30, 30, true, true),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT));
        forwardButtonBackground = new Background(new BackgroundImage(
                new Image(getClass().getResource("/edu/srjc/afinal/kyle/moulton/podomatic/View/forwardButton.png").toExternalForm(),
                        30, 30, true, true),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT));
        backwardButtonBackground = new Background(new BackgroundImage(
                new Image(getClass().getResource("/edu/srjc/afinal/kyle/moulton/podomatic/View/backwardButton.png").toExternalForm(),
                        30, 30, true, true),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT));
        skipForwardButtonBackground = new Background(new BackgroundImage(
                new Image(getClass().getResource("/edu/srjc/afinal/kyle/moulton/podomatic/View/skipForwardButton.png").toExternalForm(),
                        30, 30, true, true),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT));
        skipBackwardButtonBackground = new Background(new BackgroundImage(
                new Image(getClass().getResource("/edu/srjc/afinal/kyle/moulton/podomatic/View/skipBackwardButton.png").toExternalForm(),
                        30, 30, true, true),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT));
        resetRateButtonBackground = new Background(new BackgroundImage(
                new Image(getClass().getResource("/edu/srjc/afinal/kyle/moulton/podomatic/View/resetRateButton.png").toExternalForm(),
                        30, 30, true, true),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT));
    }

    public void showEpisodes(PodcastSeries selectedSeries)
    {
        episodes.getItems().clear();
        if (sortByComboBox.getSelectionModel().getSelectedItem().equals("Downloaded"))
        {
            episodes.getItems().addAll(selectedSeries.getDownloadedPodcasts());
        }
        else
        {
            episodes.getItems().addAll(selectedSeries.getEpisodes());
        }
        episodes.setCellFactory(episodes -> new EpisodeListViewCellController());
    }

    @FXML
    public void showAddRssFeed()
    {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/edu/srjc/afinal/kyle/moulton/podomatic/View/AddRssFeedLayout.fxml"));
        AnchorPane addRssFeed = null;
        try
        {
            addRssFeed = loader.load();
        }
        catch (IOException e)
        {
            Alert failure = new Alert(Alert.AlertType.ERROR);
            failure.setTitle("Failed to load dialog box");
            failure.setHeaderText("Failed to load dialog box for adding RSS feeds");
            failure.setContentText("Failed to load dialog box for adding RSS feeds");
            failure.showAndWait();
        }

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Add Podcast Feed");
        Scene scene = new Scene(addRssFeed);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();

        series.getItems().clear();
        series.getItems().addAll(Context.getInstance().getPodcastCollection().values());
        series.setCellFactory(podcastSeries -> new SeriesListViewCellController());
        series.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            if (newValue != null)
            {
                showEpisodes(newValue);
            }
        });
    }

    @FXML
    public void setDownloadLocation(ActionEvent e)
    {
        DirectoryChooser dirChooser = new DirectoryChooser();

        File downloadLocation = dirChooser.showDialog((Stage)(((Node)e.getSource()).getScene().getWindow()));

        if (downloadLocation != null)
        {
            if (downloadLocation.canWrite())
            {
                Context.getInstance().setDownloadLocation(downloadLocation);
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Download Location Set");
                alert.setHeaderText("Download Location successfully changed");
                alert.setContentText("Download Location set to: " + Context.getInstance().getDownloadLocation().getAbsolutePath());
                alert.showAndWait();
            }
            else
            {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Invalid directory");
                alert.setContentText("Cannot write to the specified directory. Please try again");
                alert.showAndWait();
            }
        }
    }

    // Mediaview Playback controls taken from https://docs.oracle.com/javase/8/javafx/media-tutorial/playercontrol.htm
    // with some modifications.
    @FXML
    public void handlePlayButton()
    {
        Status status = mediaPlayer.getStatus();

        if (status == Status.UNKNOWN || status == Status.HALTED)
        {
            return;
        }
        if (status == Status.PAUSED
                || status == Status.READY
                || status == Status.STOPPED)
        {
            if (atEndOfPodcast)
            {
                mediaPlayer.seek(mediaPlayer.getStartTime());
                atEndOfPodcast = false;
            }
            mediaPlayer.play();
            playButton.setBackground(pauseButtonBackground);
            mediaPlayer.currentTimeProperty().addListener(new InvalidationListener()
            {
                @Override
                public void invalidated(Observable observable)
                {
                    updateValues();
                }
            });
        }
        else
        {
            mediaPlayer.pause();
            playButton.setBackground(playButtonBackground);
        }
    }

    public void handlePlayButton(Media media)
    {
        if (mediaPlayer != null)
        {
            if (mediaPlayer.getStatus() == Status.PLAYING)
            {
                mediaPlayer.stop();
            }
        }

        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
        playButton.setDisable(false);
        playButton.setBackground(pauseButtonBackground);

        volSlider.setValue((int)Math.round(mediaPlayer.getVolume() * 100));

        volSlider.valueProperty().addListener(new InvalidationListener()
        {
            @Override
            public void invalidated(Observable observable)
            {
                if (volSlider.isValueChanging())
                {
                    mediaPlayer.setVolume(volSlider.getValue() / 100.0);
                }
            }
        });

        timeSlider.valueProperty().addListener(new InvalidationListener()
        {
            @Override
            public void invalidated(Observable observable)
            {
                if (timeSlider.isValueChanging())
                {
                    mediaPlayer.seek(mediaPlayer.getMedia().getDuration().multiply(timeSlider.getValue() / 100.0));
                }
            }
        });

        mediaPlayer.currentTimeProperty().addListener(new InvalidationListener()
        {
            @Override
            public void invalidated(Observable observable)
            {
                updateValues();
            }
        });
    }

    @FXML
    public void handleStopButton()
    {
        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
            playButton.setBackground(playButtonBackground);
        }

    }

    @FXML
    public void handleForwardButton()
    {
        if (mediaPlayer != null)
        {
            if (mediaPlayer.getStatus() == Status.PLAYING)
            {
                if (mediaPlayer.getRate() < 8.0)
                {
                    mediaPlayer.setRate(mediaPlayer.getRate() + 0.25);
                    playbackRateLabel.setText(String.valueOf(mediaPlayer.getRate()) + "x");
                }
            }
        }
    }

    @FXML
    public void handleBackwardButton()
    {
        if (mediaPlayer != null)
        {
            if (mediaPlayer.getStatus() == Status.PLAYING)
            {
                if (mediaPlayer.getRate() > 0.25)
                {
                    mediaPlayer.setRate(mediaPlayer.getRate() - 0.25);
                    playbackRateLabel.setText(String.valueOf(mediaPlayer.getRate()) + "x");
                }
            }
        }
    }

    @FXML
    public void handleResetRateButton()
    {
        if (mediaPlayer != null)
        {
            if (mediaPlayer.getStatus() == Status.PLAYING)
            {
                mediaPlayer.setRate(1.0);
                playbackRateLabel.setText(String.valueOf(mediaPlayer.getRate()) + "x");
            }
        }
    }

    @FXML
    public void handleSkipForwardButton()
    {
        if (mediaPlayer != null)
        {
            if (mediaPlayer.getTotalDuration().greaterThan(mediaPlayer.getCurrentTime().add(Duration.seconds(10))))
            {
                mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(10)));
            }
            else
            {
                mediaPlayer.seek(mediaPlayer.getTotalDuration());
            }
        }

    }

    @FXML
    public void handleSkipBackwardButton()
    {
        if (mediaPlayer != null)
        {
            if (mediaPlayer.getCurrentTime().subtract(Duration.seconds(10)).greaterThan(Duration.seconds(0)))
            {
                mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(10)));
            }
            else
            {
                mediaPlayer.seek(mediaPlayer.getStartTime());
            }
        }
    }

    // updateValues function taken with minimal modification from
    // https://docs.oracle.com/javase/8/javafx/media-tutorial/playercontrol.htm
    private void updateValues()
    {
        if (timeLabel != null)
        {
            Platform.runLater(new Runnable()
            {
                public void run()
                {
                    Duration currentDuration = mediaPlayer.getMedia().getDuration();
                    Duration currentTime = mediaPlayer.getCurrentTime();
                    timeLabel.setText(formatTime(currentTime, currentDuration));
                    timeSlider.setDisable(currentDuration.isUnknown());
                    if (!timeSlider.isDisabled()
                            && currentDuration.greaterThan(Duration.ZERO)
                            && !timeSlider.isValueChanging())
                    {
                        timeSlider.setValue(currentTime.divide(currentDuration).toMillis() * 100.0);
                    }
                }
            });
        }
    }
    // formatTime method taken without modification from
    // https://docs.oracle.com/javase/8/javafx/media-tutorial/playercontrol.htm
    private static String formatTime(Duration elapsed, Duration duration)
    {
        int intElapsed = (int)Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0)
        {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60
                - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO))
        {
            int intDuration = (int)Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0)
            {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 -
                    durationMinutes * 60;
            if (durationHours > 0)
            {
                return String.format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            }
            else
            {
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds,durationMinutes,
                        durationSeconds);
            }
        }
        else
        {
            if (elapsedHours > 0)
            {
                return String.format("%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds);
            }
            else
            {
                return String.format("%02d:%02d",elapsedMinutes,
                        elapsedSeconds);
            }
        }
    }

    public void populateSeriesFromFile(String fileName)
    {
        File savedSeriesFile = new File(System.getProperty("user.dir").replace("\\", "/") + "/" + fileName);
        Scanner inFile = null;

        try
        {
            inFile = new Scanner(savedSeriesFile);
        }
        catch (FileNotFoundException e)
        {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Invalid saved RSS file");
            alert.setHeaderText("Could not load default series file");
            alert.setContentText("The default podcast series file could not be loaded. RSS feeds can still be entered manually");
            alert.showAndWait();
        }

        String input = inFile.nextLine();
        PodcastSeries newSeries = null;
        while (inFile.hasNext())
        {
            if (input.charAt(0) != '#')
            {
                try
                {
                    newSeries = new PodcastSeries(input);
                    Context.getInstance().getPodcastCollection().put(newSeries.getTitle(), newSeries);
                }
                catch (IOException e)
                {
                    Alert failure = new Alert(Alert.AlertType.ERROR);
                    failure.setTitle("Invalid URL from file");
                    failure.setHeaderText("Invalid data in default podcast series file");
                    failure.setContentText("Failed to validate URL read from default series file, or the rss feed is inaccessible");
                    failure.showAndWait();
                    continue;
                }
                catch (FeedException e)
                {
                    Alert failure = new Alert(Alert.AlertType.ERROR);
                    failure.setTitle("Invalid RSS feed");
                    failure.setHeaderText("Invalid RSS feed acquired from default series file");
                    failure.setContentText("The feed may be provided in a format this program cannot process");
                    failure.showAndWait();
                    continue;
                }
            }
            input = inFile.nextLine();
        }
    }

    @FXML
    public void handleRefresh()
    {
        updateButton.setDisable(true);
        updateButton.setText("Updating...");

        // Update process in a separate thread so it doesn't lock up application during its process.
        // The same Platform.runLater() is called at the end for things that are JavaFX thread related.
        Thread updateThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                boolean episodeFound = false;

                for (PodcastSeries currentSeries : Context.getInstance().getPodcastCollection().values())
                {
                    try
                    {
                        PodcastSeries tempSeries = new PodcastSeries(currentSeries.getPodcastFeedUrl());

                        if (tempSeries.getEpisodes().size() > currentSeries.getEpisodes().size())
                        {
                            // Checking tempSeries in reverse order because newest episodes appear first in the episodes list,
                            // and new episodes are added to the episodes podcastCollection by oldest first to retain this pattern
                            //
                            // On a side note, I could speed this process up (if it were necessary) by only checking the new episodes
                            // in tempSeries (by taking the episode list size differences and looking at 0 through that many indexes.
                            // I'm unsure of how often people who maintain RSS feeds change up their order or anything of that nature,
                            // so I figured it was best to just check every episode and compare by title.
                            for (int i = tempSeries.getEpisodes().size() - 1; i >= 0; i--)
                            {
                                for (PodcastEpisode episodeToCompare: currentSeries.getEpisodes())
                                {
                                    if (tempSeries.getEpisodes().get(i).getTitle().equals(episodeToCompare.getTitle()))
                                    {
                                        episodeFound = true;
                                        break;
                                    }
                                }
                                if (!episodeFound)
                                {
                                    currentSeries.getEpisodes().add(0, tempSeries.getEpisodes().get(i));
                                }
                                episodeFound = false;
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        Alert failure = new Alert(Alert.AlertType.ERROR);
                        failure.setTitle("Invalid URL");
                        failure.setHeaderText("Unable to validate the feed URL in a stored series.");
                        failure.setContentText(currentSeries.getTitle() + " could not be updated. Either the feed URL is invalid or the feed is inaccessible");
                        failure.showAndWait();
                        continue;
                    }
                    catch (FeedException e)
                    {
                        Alert failure = new Alert(Alert.AlertType.ERROR);
                        failure.setTitle("Invalid RSS feed");
                        failure.setHeaderText("Invalid RSS feed acquired from URL in stored series");
                        failure.setContentText("The feed may be provided in a format this program cannot process");
                        failure.showAndWait();
                        continue;
                    }
                }
                Platform.runLater(() ->
                {
                    if (series.getSelectionModel().getSelectedItem() != null)
                    {
                        showEpisodes(series.getSelectionModel().getSelectedItem());
                    }
                    updateButton.setDisable(false);
                    updateButton.setText("Update All");
                });
            }
        });

        updateThread.start();
    }

    @FXML
    public void handleSave()
    {
        Context.getInstance().saveData();
    }

    @FXML
    public void handleClose()
    {
        handleSave();
        System.exit(0);
    }

    @FXML
    public void handleAbout()
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("PodoMatic: About");
        alert.setHeaderText("Podomatic - by Kyle Moulton\nSRJC Spring 2017\nCS 17.11 - Section 6991\nInstructor: Sean Kirkpatrick");
        alert.setContentText("Podomatic is a podcast management and player application. It is made possible by using the ROME and JDOM libraries. " +
                "Images for the media player buttons are provided by Vecteezy. " +
                "The licenses and links for all of these sources can be found in the Licenses directory.");
        alert.showAndWait();
    }
}


