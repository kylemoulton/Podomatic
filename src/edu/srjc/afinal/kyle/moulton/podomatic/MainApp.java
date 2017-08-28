/*
Student: Kyle Moulton
Student Email: kylealexmoulton@gmail.com
Date: 5/23/2017
Project Name: AFinal - Podomatic
Course: CS 17.11 - Section 6991

Description:
    The Podomatic application is a desktop podcast manager, supporting direct podcast downloads from sources provided by
RSS feeds. Feeds URLs are either manually entered in after the application has started, or listed in the savedSeries.csv
file, separated by newlines. When the application first starts, it will search for saved data in a serialized form in the
ProfileData directory. If saved data is not found, it will default to loading feeds stored in the savedSeries.csv. The
program downloads each RSS feed and instantiates PodcastSeries objects and associated PodcastEpisode objects.
    The GUI provides a visual interface for users to manage, download, play, and delete podcast series and episodes. You
can also specify the location you desire podcasts to be downloaded to. By default, this is set to the root directory of
the applcation.
    Current data can be saved using the file menu-bar context menu option, or by exiting the application through the
file-close context menu option. Exiting out of the application by any other means will not save your data. Downloaded
podcasts will remain in the specified download file location, but references to those files are not retained within the
application on successive executions unless data is saved.
*/

/*
External Sources and Libraries Used:

ROME is an open source Java Framework for RSS and Atom feeds used under the
Apache 2.0 license
Documentation and information about the ROME framework can be found at https://rometools.github.io/rome/index.html

JDOM is an open source Java representation of an XML document. It is a dependency required for the ROME RSS/Atom parser.
Their website states that "JDOM is available under an Apache-style open source license, with the acknowledgment clause
removed."
Documentation and information about the JDOM framework can be found at http://www.jdom.org/

Mediaplayer buttons are provided by Vecteezy.com - author: lavarsmg

These licenses for both of these frameworks and the provided copyright for the Vecteezy images are provided in the
Licenses directory of this project, ROME-License.txt, JDOM-License.txt, Vecteezy-License.txt
*/

package edu.srjc.afinal.kyle.moulton.podomatic;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApp extends Application
{
    @Override
    public void start(Stage stage) throws IOException
    {
        Parent root = FXMLLoader.load(getClass().getResource("/edu/srjc/afinal/kyle/moulton/podomatic/View/PodcastPreviewLayout.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Podomatic");
        stage.setMinWidth(1050);
        stage.setMinHeight(650);
        stage.show();

    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
