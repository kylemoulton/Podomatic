/*
Student: Kyle Moulton
Student Email: kylealexmoulton@gmail.com
Date: 5/23/2017
Project Name: AFinal - Podomatic
Course: CS 17.11 - Section 6991

Description: The SeriesListViewCellController handles the creation of custom cells in a ListView item which display
details pertaining to PodcastSeries. The Series title, related image, and delete button are handled within this
controller. In the PodcastPreviewController, a listener is added to the SelectionModel of the ListView, so that when
a SeriesListViewCell is selected, all of its PodcastEpisodes are displayed in an adjance ListView.
*/
package edu.srjc.afinal.kyle.moulton.podomatic.Controller;

import edu.srjc.afinal.kyle.moulton.podomatic.Model.Context;
import edu.srjc.afinal.kyle.moulton.podomatic.Model.PodcastEpisode;
import edu.srjc.afinal.kyle.moulton.podomatic.Model.PodcastSeries;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class SeriesListViewCellController extends ListCell<PodcastSeries>
{
    @FXML
    private Label podcastTitle;

    @FXML
    private Label podcastDescription;

    @FXML
    private ImageView podcastImage;

    @FXML
    private AnchorPane anchorPane;

    private FXMLLoader loader;

    public SeriesListViewCellController()
    {
    }

    @Override
    protected void updateItem(PodcastSeries item, boolean empty) {
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
                loader = new FXMLLoader(getClass().getResource("/edu/srjc/afinal/kyle/moulton/podomatic/View/SeriesListViewCell.fxml"));
                loader.setController(this);
                try
                {
                    loader.load();
                }
                catch (IOException e)
                {
                    Alert failure = new Alert(Alert.AlertType.ERROR);
                    failure.setTitle("Fatal Error");
                    failure.setHeaderText("Failed to load series cell");
                    failure.setContentText("Could load the layout for series cells");
                    failure.showAndWait();
                    System.exit(1);
                }
            }
            podcastTitle.setText(item.getTitle());

            if (item.getImage() != null)
            {
                podcastImage.setImage(item.getImage());
            }
            setText(null);
            setGraphic(anchorPane);
        }
    }

    @FXML
    private void deleteSeries()
    {
        ArrayList<String> failedEpisodes = new ArrayList<>();
        boolean failedToDelete = false;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete " + getItem().getTitle());
        alert.setContentText("Are you sure you want to delete this series and all associated episodes?");

        Optional<ButtonType> result = alert.showAndWait();

        for (PodcastEpisode episode : getItem().getDownloadedPodcasts())
        {
            try
            {
                episode.deletePodcast();
                Context.getInstance().getPrimaryController().getEpisodes().getItems().remove(episode);
            }
            catch (IOException e)
            {
                failedEpisodes.add(episode.getTitle());
                failedToDelete = true;
            }
        }

        Context.getInstance().getPodcastCollection().remove(getItem().getTitle());
        Context.getInstance().getPrimaryController().getSeries().getItems().remove(getItem());

        if (Context.getInstance().getPrimaryController().getSeries().getItems().isEmpty())
        {
            if (!Context.getInstance().getPrimaryController().getEpisodes().getItems().isEmpty())
            {
                Context.getInstance().getPrimaryController().getEpisodes().getItems().clear();
            }
        }

        Context.getInstance().getPrimaryController().getSeries().refresh();
        Context.getInstance().getPrimaryController().getEpisodes().refresh();

        if (failedToDelete)
        {
            Alert failure = new Alert(Alert.AlertType.ERROR);
            failure.setTitle("Failed to delete");
            failure.setHeaderText("Failed to delete one or more episodes.");
            StringBuilder sb = new StringBuilder();
            for (String title : failedEpisodes)
            {
                sb.append(title + "\n");
            }
            failure.setContentText("Episodes not deleted: \n" + sb.toString());
            failure.showAndWait();
        }
        else
        {
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Delete successful");
            success.setHeaderText("Series Deleted");
            success.setContentText("Series and all episodes were deleted.");
            success.showAndWait();
        }
    }
}
