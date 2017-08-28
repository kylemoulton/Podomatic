/*
Student: Kyle Moulton
Student Email: kylealexmoulton@gmail.com
Date: 5/23/2017
Project Name: AFinal - Podomatic
Course: CS 17.11 - Section 6991

Description: The PodcastEpisode Class handles storing data pertaining to specific episodes of a podcast series. Data
stored within the PodcastEpisode object includes, episode title, description, published date, link for download, file
(if downloaded and stored locally), associated PodcastSeries, and boolean variables to determine if the episode is
already downloaded, or is currently in the process of being downloaded.
The PodcastEpisode class implements the Serializable interface so that it can be saved to a serialized file for re-loading
on successive program executions.
*/

package edu.srjc.afinal.kyle.moulton.podomatic.Model;

import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Date;

public class PodcastEpisode implements Serializable
{
    private String title;
    private String description;
    private Date publishedDate;
    private String downloadLink;
    private File episodeFile;
    private PodcastSeries series;
    private boolean isDownloading = false;
    private boolean isDownloaded = false;

    public PodcastEpisode()
    {
    }

    public PodcastEpisode(SyndEntryImpl entry, PodcastSeries series)
    {
        this.title = entry.getTitle();
        this.description = entry.getDescription().getValue();
        this.publishedDate = entry.getPublishedDate();
        if (entry.getEnclosures().size() >= 1)
        {
            SyndEnclosure enclosure = (SyndEnclosure)entry.getEnclosures().get(0);
            this.downloadLink = enclosure.getUrl();
        }
        this.series = series;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Episode title: " + title + "\n");
        sb.append("Description: " + description + "\n");
        sb.append("Date Published: " + publishedDate.toString() + "\n");
        sb.append("Download Link: " + downloadLink + "\n");
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

    public Date getPublishedDate()
    {
        return publishedDate;
    }

    public String getDownloadLink()
    {
        return downloadLink;
    }

    public File getEpisodeFile()
    {
        return episodeFile;
    }

    public void setEpisodeFile(File episodeFile)
    {
        this.episodeFile = episodeFile;
    }

    public boolean isDownloaded()
    {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded)
    {
        isDownloaded = downloaded;
    }

    public PodcastSeries getSeries()
    {
        return series;
    }

    public void setDownloading(boolean isDownloading)
    {
        this.isDownloading = isDownloading;
    }

    public boolean getIsDownloading()
    {
        return isDownloading;
    }

    public void deletePodcast() throws IOException
    {
        Files.delete(this.episodeFile.toPath());
    }
}