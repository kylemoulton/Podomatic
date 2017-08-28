/*
Student: Kyle Moulton
Student Email: kylealexmoulton@gmail.com
Date: 5/23/2017
Project Name: AFinal - Podomatic
Course: CS 17.11 - Section 6991

Description: The EpisodeListViewCellController handles the creation of custom cells in a ListView item which display
details pertaining to PodcastEpisodes. The episode title, description, published date, download, play, and delete buttons
are handled within this controller. Because the MediaView object resides within the PodcastPreviewController, the play
button in EpisodeListViewCells call the play method through the PodcastPreviewController. The download button adds a
custom Thread task, WorkerThread, to a LinkedList in the Context singleton class, which handles downloading episodes in
a queue without hanging the application.
*/
package edu.srjc.afinal.kyle.moulton.podomatic.Controller;

import edu.srjc.afinal.kyle.moulton.podomatic.Model.Context;
import edu.srjc.afinal.kyle.moulton.podomatic.Model.PodcastEpisode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class EpisodeListViewCellController extends ListCell<PodcastEpisode>
{
    @FXML
    private Label episodeTitle;

    @FXML
    private Label episodeDescription;

    @FXML
    private Label datePublished;

    @FXML
    private Label downloadLink;

    @FXML
    private Label downloadingLabel;

    @FXML
    private Button playButton;

    @FXML
    private Button downloadButton;

    @FXML
    private Button deleteButton;

    @FXML
    private BorderPane borderPane;

    private FXMLLoader loader;

    public EpisodeListViewCellController()
    {
    }

    @Override
    protected void updateItem(PodcastEpisode item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null)
        {
            setText(null);
            setGraphic(null);
        }
        else
        {
            if (loader == null)
            {
                loader = new FXMLLoader(getClass().getResource("/edu/srjc/afinal/kyle/moulton/podomatic/View/EpisodeListViewCell.fxml"));
                loader.setController(this);
                try
                {
                    loader.load();
                }
                catch (IOException e)
                {
                    Alert failure = new Alert(Alert.AlertType.ERROR);
                    failure.setTitle("Failed to load episode cell");
                    failure.setContentText("Could load the layout for episode cells");
                    failure.showAndWait();
                    System.exit(1);
                }
            }
            if (item.getTitle() != null)
            {
                episodeTitle.setText(item.getTitle());
            }
            if (item.getDescription() != null)
            {
                episodeDescription.setText(item.getDescription().replaceAll("\\<[^>]*>",""));
            }
            if (item.getPublishedDate() != null)
            {
                datePublished.setText("Published: " + item.getPublishedDate().toString());
            }

            if (item.getIsDownloading())
            {
                downloadingLabel.setVisible(true);
                downloadButton.setVisible(false);
            }
            else if (getItem().isDownloaded())
            {
                downloadingLabel.setVisible(false);
                playButton.setVisible(true);
                deleteButton.setVisible(true);
                downloadButton.setVisible(false);
            }
            else
            {
                downloadingLabel.setVisible(false);
                playButton.setVisible(false);
                deleteButton.setVisible(false);
                downloadButton.setVisible(true);
            }
            setText(null);
            setGraphic(borderPane);
        }
    }

    @FXML
    public void downloadPodcast()
    {
        this.downloadButton.setVisible(false);
        this.downloadingLabel.setVisible(true);
        getItem().setDownloading(true);
        Context.getInstance().addTask(new Thread(new WorkerThread(getItem(), this)));
    }

    @FXML
    public void playSelected()
    {
        // Temporary file is the file played because I've experienced and have read elsewhere of other's experience
        // that mediaplayer objects will retain the file handle of its media even after the dispose method is called
        // on the mediaplayer object. If I use a temporary file, I can be sure that just a single file is locked up
        // and it is overwritten every time a new episode is selected to be played. Unfortunately, this temp file is
        // not even able to be deleted on exit, it has to be deleted manually after the program has terminated. I've
        // tried a number of solutions and nothing works yet. I've also read that this issue might be exclusive to
        // Win 10 and could work fine on other systems.
        FileOutputStream outFile;
        File tempFile = new File(Context.getInstance().getDownloadLocation().toPath() + "/tempPlayFile.mp3");
        try
        {
            outFile = new FileOutputStream(tempFile);
            Files.copy(getItem().getEpisodeFile().toPath(), outFile);
            outFile.close();
        }
        catch (IOException e)
        {
            Alert failure = new Alert(Alert.AlertType.ERROR);
            failure.setTitle("Could not play the selected file.");
            failure.setContentText("Could not play the selected. File. File may have to be deleted and re-downloaded");
            failure.showAndWait();
            return;
        }
        tempFile.deleteOnExit();

        Context.getInstance().getPrimaryController().handlePlayButton(new Media(tempFile.toURI().toString()));
        Context.getInstance().getPrimaryController().getNowPlaying().setText(getItem().getTitle());
    }

    @FXML
    public void deleteSelected()
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete " + getItem().getTitle());
        alert.setContentText("Are you sure you want to delete this item?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK)
        {
            try
            {
                getItem().deletePodcast();

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Delete successful");
                success.setHeaderText("Episode deleted successfully");
                success.setContentText(getItem().getTitle() + " was deleted.");
                success.showAndWait();
            }
            catch (IOException e)
            {
                Alert failure = new Alert(Alert.AlertType.ERROR);
                failure.setTitle("Failed to delete");
                failure.setHeaderText("Failed to delete episode");
                failure.setContentText("Could not delete episode: " + getItem().getTitle());
                failure.showAndWait();
            }

            getItem().setDownloaded(false);
            getItem().setEpisodeFile(null);
            getItem().getSeries().getDownloadedPodcasts().remove(getItem());

            downloadButton.setVisible(true);
            playButton.setVisible(false);
            deleteButton.setVisible(false);

            if(Context.getInstance().getPrimaryController().getSeries().getSelectionModel().getSelectedItem() != null)
            {
                Context.getInstance().getPrimaryController().showEpisodes(Context.getInstance().getPrimaryController().getSeries().getSelectionModel().getSelectedItem());
            }
        }
    }

    public void setPlayButtonVisible(boolean visible)
    {
        this.playButton.setVisible(visible);
    }

    public void setDownloadButtonVisible(boolean visible)
    {
        this.downloadButton.setVisible(visible);
    }

    public void setDeleteButtonVisible(boolean visible)
    {
        this.deleteButton.setVisible(visible);
    }

    public void setDownloadingLabelVisible(boolean visible)
    {
        this.downloadingLabel.setVisible(visible);
    }

    public void setDownloadingLabelText(String text)
    {
        this.downloadingLabel.setText(text);
    }

}


class WorkerThread implements Runnable
{
    private PodcastEpisode episode;
    private boolean episodeDownloaded = false;
    EpisodeListViewCellController parentController;

    public WorkerThread(PodcastEpisode episode, EpisodeListViewCellController parentController)
    {
        this.episode = episode;
        this.parentController = parentController;
    }

    @Override
    public void run()
    {
        // Download file details taken from http://stackoverflow.com/questions/921262/how-to-download-and-save-a-file-from-internet-using-java
        URL url = null;
        try
        {
            url = new URL(episode.getDownloadLink());
        }
        catch (MalformedURLException e) {
            Alert failure = new Alert(Alert.AlertType.ERROR);
            failure.setTitle("Invalid URL");
            failure.setHeaderText("URL could not be parsed");
            failure.setContentText("The URL for this episode is not in a valid format");
            failure.showAndWait();
            return;
        }
        String fileName = Context.getInstance().getDownloadLocation().toPath() + "/"
                +  episode.getDownloadLink().substring(episode.getDownloadLink().lastIndexOf('/') + 1,
                episode.getDownloadLink().lastIndexOf("mp3") + 3);

        Path targetPath = new File(fileName).toPath();

        try
        {
            Files.copy(url.openStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            episodeDownloaded = true;
            episode.setDownloaded(true);
            episode.setEpisodeFile(new File(fileName));
            episode.getSeries().getDownloadedPodcasts().add(episode);

            episode.setDownloading(false);
            parentController.setDownloadButtonVisible(false);
            parentController.setPlayButtonVisible(true);
            parentController.setDeleteButtonVisible(true);
            parentController.setDownloadingLabelVisible(false);
        }
        catch (IOException e)
        {
            episode.setDownloading(false);
            parentController.setDownloadButtonVisible(true);
            parentController.setDownloadingLabelVisible(false);
            Platform.runLater(() ->
                    {
                        Alert failure = new Alert(Alert.AlertType.ERROR);
                        failure.setTitle("Failed download");
                        failure.setHeaderText(episode.getTitle() + " Could not be downloaded.");
                        failure.setContentText("The episode may not be accessible or you may not be connected to the internet.");
                        failure.showAndWait();
                    });

        }

    }
}
