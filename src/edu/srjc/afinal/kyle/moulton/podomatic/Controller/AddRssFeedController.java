/*
Student: Kyle Moulton
Student Email: kylealexmoulton@gmail.com
Date: 5/23/2017
Project Name: AFinal - Podomatic
Course: CS 17.11 - Section 6991

Description: The AddRssFeedController handles adding new RSS feeds and instantiating new PodcastSeries and related
PodcastEpisode objects from the parsed feeds. When the AddRssFeed dialog stage has completed with a new RSS feed added,
the series ListView is updated to reflect the added series.
*/

package edu.srjc.afinal.kyle.moulton.podomatic.Controller;

import edu.srjc.afinal.kyle.moulton.podomatic.Model.Context;
import edu.srjc.afinal.kyle.moulton.podomatic.Model.PodcastSeries;
import com.sun.syndication.io.FeedException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class AddRssFeedController
{
    @FXML
    private Label resultMessage;

    @FXML
    public TextField feedUrl;

    @FXML
    public void handleAddRssFeed(ActionEvent ae)
    {
        resultMessage.setText("Adding new feed in progress...");
        PodcastSeries newSeries = null;
        try
        {
            newSeries = new PodcastSeries(feedUrl.getText());
        }
        catch (IOException e)
        {
            resultMessage.setText("Unable to access rss feed. There may be an error in the URL you entered. Try again?");
            feedUrl.requestFocus();
            return;
        }
        catch (FeedException e)
        {
            resultMessage.setText("RSS feed was unable to be read. The feed may be provided in a format this program cannot process.");
            feedUrl.requestFocus();
            return;
        }

        Context.getInstance().getPodcastCollection().put(newSeries.getTitle(), newSeries);

        resultMessage.setText("Adding " + newSeries.getTitle() + ": Complete");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Feed added successfully");
        alert.setContentText(newSeries.getTitle() + " Was added successfully");
        alert.showAndWait();

        Node source = (Node) ae.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleBack(ActionEvent e)
    {
        // Couldn't figure out how to access "this" stage, the following stackoverflow page provided some insight
        // http://stackoverflow.com/questions/33932309/how-to-access-a-javafx-stage-from-a-controller
        Node source = (Node) e.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
