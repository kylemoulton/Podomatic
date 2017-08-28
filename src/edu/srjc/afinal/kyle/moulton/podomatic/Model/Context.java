/*
Student: Kyle Moulton
Student Email: kylealexmoulton@gmail.com
Date: 5/23/2017
Project Name: AFinal - Podomatic
Course: CS 17.11 - Section 6991

Description: The Context class is what I've heard some refere to as a "Singleton" class/object. It provides the means
by which each different controller in the application can communicate with each other, call methods of
other controllers, and share data. The idea was taken from a stackoverflow thread,
(http://stackoverflow.com/questions/12166786/multiple-fxml-with-controllers-share-object)
Unfortunately, it seems a bit close to a collection of global data to be considered clean code, but I wasn't thrilled
with the other options I could find.
The Context class holds a HashMap of PodcastSeries accessible by the series title, the saved or default directory
for which downloaded podcasts will be stored, A LinkedList that acts as a queue for episode downloads, a boolean to
determine whether the queueu is being processed, and a reference to the primary PodcastPreviewController.
The Context class handles loading and saving data from serialized files and setting off a single thread that manages
one or more download threads.
*/

package edu.srjc.afinal.kyle.moulton.podomatic.Model;

import edu.srjc.afinal.kyle.moulton.podomatic.Controller.PodcastPreviewController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.control.Alert;
import java.io.*;
import java.util.*;

public class Context
{
    private final static Context instance = new Context();

    public static Context getInstance()
    {
        return instance;
    }

    private final String savedCollection = System.getProperty("user.dir").replace("\\", "/") + "/ProfileData/savedCollection.ser";
    private final String savedDownloadLocation = System.getProperty("user.dir").replace("\\", "/") + "/ProfileData/downloadLocation.ser";

    private ObservableMap<String, PodcastSeries> podcastCollection;
    private File downloadLocation;

    private LinkedList<Thread> downloadQueue = new LinkedList<>();
    private boolean downloadingInProcess = false;

    private PodcastPreviewController primaryController;

    private Context()
    {
        File inFile = null;
        try
        {
            this.podcastCollection = FXCollections.observableHashMap();

            inFile = new File(savedCollection);
            FileInputStream ifStream = new FileInputStream(inFile);
            ObjectInputStream inObject = new ObjectInputStream(ifStream);

            HashMap<String, PodcastSeries> tempCollection = (HashMap<String, PodcastSeries>)inObject.readObject();

            for (String key : tempCollection.keySet())
            {
                this.podcastCollection.put(key, tempCollection.get(key));
            }

            inObject.close();
            ifStream.close();

            inFile = new File(savedDownloadLocation);
            ifStream = new FileInputStream(inFile);
            inObject = new ObjectInputStream(ifStream);
            this.downloadLocation = (File)inObject.readObject();

            inObject.close();
            ifStream.close();
        }
        catch (IOException | ClassNotFoundException e)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No save data found");
            alert.setHeaderText("Loading default series");
            alert.setContentText("No saved data could be found. A selection of podcasts will be added for you. " +
                    "You may add or delete new series after first startup.");
            alert.showAndWait();

            podcastCollection = FXCollections.observableHashMap();
            downloadLocation = new File(System.getProperty("user.dir").replace("\\", "/"));
            return;
        }
    }

    public void setPrimaryController(PodcastPreviewController primaryController)
    {
        this.primaryController = primaryController;
    }

    public PodcastPreviewController getPrimaryController()
    {
        return primaryController;
    }

    public ObservableMap<String, PodcastSeries> getPodcastCollection()
    {
        return podcastCollection;
    }

    public File getDownloadLocation()
    {
        return downloadLocation;
    }

    public void setDownloadLocation(File downloadLocation)
    {
        this.downloadLocation = downloadLocation;
    }

    public void saveData()
    {
        FileOutputStream outFile;
        ObjectOutputStream outObject;
        try
        {
            outFile = new FileOutputStream(savedCollection);
            outObject = new ObjectOutputStream(outFile);
            outObject.writeObject(new HashMap<String, PodcastSeries>(this.podcastCollection));

            outObject.close();
            outFile.close();

            outFile = new FileOutputStream(savedDownloadLocation);
            outObject = new ObjectOutputStream(outFile);
            outObject.writeObject(this.downloadLocation);

            outObject.close();
            outFile.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Data saved successfully");
            alert.setHeaderText("Save Success");
            alert.setContentText("Podcast Collection Data was saved successfully");
            alert.showAndWait();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Alert failure = new Alert(Alert.AlertType.ERROR);
            failure.setTitle("Error");
            failure.setHeaderText("Data not saved");
            failure.setContentText("Data could not be saved for future program executions.");
            failure.showAndWait();
            return;
        }
    }

    public void setDownloadingInProcess(boolean downloadingInProcess)
    {
        this.downloadingInProcess = downloadingInProcess;
    }

    public LinkedList<Thread> getDownloadQueue()
    {
        return this.downloadQueue;
    }

    public void addTask(Thread downloadThread)
    {
        this.downloadQueue.add(downloadThread);

        if (!downloadingInProcess)
        {
            processQueue();
        }
    }

    public void processQueue()
    {
        new Thread(new DownloadQueue()).start();
    }
}

class DownloadQueue implements Runnable
{
    @Override
    public void run() {
        Context.getInstance().setDownloadingInProcess(true);

        Thread currentThread = null;
        while(!Context.getInstance().getDownloadQueue().isEmpty())
        {
            currentThread = Context.getInstance().getDownloadQueue().poll();
            currentThread.start();
            try
            {
                currentThread.join();
            }
            catch (InterruptedException e)
            {
                Alert failure = new Alert(Alert.AlertType.ERROR);
                failure.setTitle("Episode did not finish downloading");
                failure.setContentText("Episode could not complete its download. Ensure you are connected to the internet and try re-downloading in a moment");
                failure.showAndWait();
                continue;
            }
            if (Context.getInstance().getPrimaryController().getSeries().getSelectionModel().getSelectedItem() != null)
            {
                // Found this suggestion on https://stackoverflow.com/questions/24329395/app-hangs-up-or-not-on-fx-application-thread-occurs-during-app-activity
                // When I was getting a "Not on FX application thread" error on the method call from this thread. This seems to work fine with the Platform.runLater()
                Platform.runLater(() -> Context.getInstance().getPrimaryController().showEpisodes(Context.getInstance().getPrimaryController().getSeries().getSelectionModel().getSelectedItem()));
            }
        }
        Context.getInstance().setDownloadingInProcess(false);
    }
}

