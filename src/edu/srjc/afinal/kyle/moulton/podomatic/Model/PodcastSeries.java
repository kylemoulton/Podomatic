/*
Student: Kyle Moulton
Student Email: kylealexmoulton@gmail.com
Date: 5/23/2017
Project Name: AFinal - Podomatic
Course: CS 17.11 - Section 6991

Description: The PodcastSeries object handles storing data pertaining to a particular Podcast series. Data members
include Podcast title, description, feed Url, image Url, and lists of all available episodes, and all downloaded episodes.
The PodcastSeries Object also holds an Image object which is displayed in the SeriesListViewCell. I found that instantiating
the image object in the cell's updateItem method causes stuttering. I further determined this was because the updateItem
method is called frequently, especially when scrolling through the list. Having the Image pre-instantiated and merely
just "added" to the ImageView object greatly improves performance.
PodcastSeries objects are instantiated with a constructor that takes an RSS feed url. The RSS feed is downloaded and
parsed with the ROME library to extract relevant data for the series. The constructor also instantiates a PodcastEpisode
object for every episode parsed in the RSS feed, and passes them all to an ObservableList.
The PodcastSeries Object implements Serializable so that the object can be stored in a serialized file, and re-loaded
on successive program executions. Some properties, namely the Image, cannot be serialized, so the image is manually
re-instantiated on load from the saved image Url in the serialized file.
*/

package edu.srjc.afinal.kyle.moulton.podomatic.Model;

import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import org.xml.sax.InputSource;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class PodcastSeries implements Serializable
{
    private String title;
    private String description;
    private String podcastFeedUrl;
    private String podcastImageUrl;

    private transient ObservableList<PodcastEpisode> downloadedPodcasts;
    private transient ObservableList<PodcastEpisode> episodes;
    private transient Image image;

    public PodcastSeries()
    {
    }

    public PodcastSeries(String feed) throws IOException, FeedException
    {
        URLConnection openConnection = new URL(feed).openConnection();
        openConnection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        InputStream is = openConnection.getInputStream();
        InputSource source = new InputSource(is);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed sf = input.build(source);

        podcastFeedUrl = feed;
        downloadedPodcasts = FXCollections.observableArrayList();
        title = sf.getTitle();
        description = sf.getDescription();

        List<SyndEntryImpl> entries = sf.getEntries();

        episodes = FXCollections.observableArrayList();
        for (SyndEntryImpl ep : entries)
        {
            episodes.add(new PodcastEpisode(ep, this));
        }

        if (sf.getImage() != null)
        {
            image = new Image(sf.getImage().getUrl());
            podcastImageUrl = sf.getImage().getUrl();
        }
        else
        {
            File imageFile = new File(System.getProperty("user.dir").replace("\\", "/") + "/src/edu/srjc/afinal/kyle/moulton/podomatic/View/noImage.png");
            image = new Image(imageFile.toURI().toString());
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
        out.writeObject(new ArrayList<PodcastEpisode>(downloadedPodcasts));
        out.writeObject(new ArrayList<PodcastEpisode>(episodes));
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        this.downloadedPodcasts = FXCollections.observableArrayList((ArrayList<PodcastEpisode>)in.readObject());
        this.episodes = FXCollections.observableArrayList((ArrayList<PodcastEpisode>)in.readObject());
        if (podcastImageUrl != null)
        {
            this.image = new Image(podcastImageUrl);
        }
        else
        {
            File imageFile = new File(System.getProperty("user.dir").replace("\\", "/") + "/src/edu/srjc/afinal/kyle/moulton/podomatic/View/noImage.png");
            image = new Image(imageFile.toURI().toString());
        }
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Podcast Title: " + title + "\n");
        sb.append("Description: " + description + "\n");
        sb.append("\n");
        return sb.toString();
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

    public ObservableList<PodcastEpisode> getEpisodes()
    {
        return episodes;
    }

    public Image getImage()
    {
        return image;
    }

    public String getPodcastFeedUrl()
    {
        return podcastFeedUrl;
    }

    public ObservableList<PodcastEpisode> getDownloadedPodcasts()
    {
        return downloadedPodcasts;
    }
}

