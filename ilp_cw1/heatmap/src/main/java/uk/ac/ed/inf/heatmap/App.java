package uk.ac.ed.inf.heatmap;

import java.io.*;
import java.nio.file.*;
import com.mapbox.geojson.*;
import java.util.*;

/**
 * Hello world!
 *
 */
public class App 
{
	public static String txtFileToString(String prediction_data) throws Exception
	{
		//accepting the text file predictions.txt as a string
		String p = "";
		p = new String(Files.readAllBytes(Paths.get(prediction_data)));
		return p;
	}
    public static void main( String[] args ) throws Exception
    {
        String input = txtFileToString(args[0]);
        input = input.replaceAll(" ", "");
        
        //convert string to 2D array of integers
        
        String[] l = input.split("\n"); //split by new line
        int [][] predictions = new int[l.length][l.length];
        for(int i=0; i<l.length; i++)
        {
        	l[i] = l[i].trim();
        	String[] v = l[i].split(","); //split by ,
        	for(int j=0; j<v.length; j++)
        	{
        		predictions[i][j] = (int)Integer.parseInt(v[j]);
        	}
        }
        
        //initialise drone confinement coordinates
        Point point = Point.fromLngLat(-3.192473, 55.946233); //Forrest Hill
        Point point2 = Point.fromLngLat(-3.184319, 55.946233); //KFC
        Point point3 = Point.fromLngLat(-3.184319, 55.942617); //Buccleuch St
        Point point4 = Point.fromLngLat(-3.192473, 55.942617); //Top of Meadows
        
        //List of Points of the coordinates above
        List<Point> coordinates = new ArrayList<Point>();
        coordinates.add(point);
        coordinates.add(point2);
        coordinates.add(point3);
        coordinates.add(point4);
        coordinates.add(point);
        
        //creating the border of the drone confinement area
        LineString border = LineString.fromLngLats(coordinates);
        String f = border.toJson();
        
        //initialising polygons inside the border
        double lntd = -3.192473;
        double lat = 55.946233;
        double lntd2 = -3.184319;
        double lat2 = 55.942617;
        final double d1 = (lntd2-lntd)/10; //longitude difference for two points of a polygon
        final double d2 = (lat-lat2)/10; //latitude difference for two points of a polygon
        
        //creating a list of features
        List<Feature> features_list = new ArrayList<Feature>();
        
        for(int i=0; i<10; i++)
        {
        	lntd = -3.192473;
        	for(int j=0; j<10; j++)
        	{
        		Point p1 = Point.fromLngLat(lntd, lat);
        		Point p2 = Point.fromLngLat((lntd+d1), lat);
        		Point p3 = Point.fromLngLat((lntd+d1), (lat-d2));
        		Point p4 = Point.fromLngLat(lntd, (lat-d2));
        		//creating list of coordinates for polygon
        		List<Point> temp = new ArrayList<Point>();
        		temp.add(p1);
        		temp.add(p2);
        		temp.add(p3);
        		temp.add(p4);
        		temp.add(p1);
        		List<List<Point>> polygon_coordinates = new ArrayList<List<Point>>();
        		polygon_coordinates.add(temp);
        		//creating polygon and feature of polygon
        		Polygon polygon = Polygon.fromLngLats(polygon_coordinates);
        		Feature feature = Feature.fromGeometry(polygon);
        		
        		//properties for polygon
        		feature.addNumberProperty("fill-opacity", 0.75);
        		int sensor_reading = predictions[i][j];
        		if(sensor_reading>=0 && sensor_reading<32)
        		{
        			feature.addStringProperty("rgb-string", "#00ff00");
        			feature.addStringProperty("fill", "#00ff00");
        		}
        		else if(sensor_reading>=32 && sensor_reading<64)
        		{
        			feature.addStringProperty("rgb-string", "#40ff00");
        			feature.addStringProperty("fill", "#40ff00");
        		}
        		else if(sensor_reading>=64 && sensor_reading<96)
        		{
        			feature.addStringProperty("rgb-string", "#80ff00");
        			feature.addStringProperty("fill", "#80ff00");
        		}
        		else if(sensor_reading>=96 && sensor_reading<128)
        		{
        			feature.addStringProperty("rgb-string", "#c0ff00");
        			feature.addStringProperty("fill", "#c0ff00");
        		}
        		else if(sensor_reading>=128 && sensor_reading<160)
        		{
        			feature.addStringProperty("rgb-string", "#ffc000");
        			feature.addStringProperty("fill", "#ffc000");
        		}
        		else if(sensor_reading>=160 && sensor_reading<192)
        		{
        			feature.addStringProperty("rgb-string", "#ff8000");
        			feature.addStringProperty("fill", "#ff8000");
        		}
        		else if(sensor_reading>=192 && sensor_reading<224)
        		{
        			feature.addStringProperty("rgb-string", "#ff4000");
        			feature.addStringProperty("fill", "#ff4000");
        		}
        		else if(sensor_reading>=224 && sensor_reading<256)
        		{
        			feature.addStringProperty("rgb-string", "#ff0000");
        			feature.addStringProperty("fill", "#ff0000");
        		}
        		
        		//add to feature list
        		features_list.add(feature);
        		lntd = lntd+d1;
        		
        	} //j loop
        	
        	lat = lat-d2;
        	
        } //i loop
        
        Feature border_feature = Feature.fromGeometry(border);
        features_list.add(border_feature);
        
        //creating geojson feature collection
        FeatureCollection heatmap_features = FeatureCollection.fromFeatures(features_list);
        String heatmap_geojson = heatmap_features.toJson();
        
        //creating and writing heatmap_geojson to heatmap.geojson file
        try
        {
        	 File file = new File("heatmap.geojson");
             if(file.createNewFile())
             {
            	 System.out.println(file.getName() + " file created.");
             }
             else
             {
            	 System.out.println("File already exists.");
             }
        }
        catch(IOException e)
        {
        	System.out.println("An error occured.");
        	e.printStackTrace();
        }
        
        try
        {
        	FileWriter myWriter = new FileWriter("heatmap.geojson");
        	myWriter.write(heatmap_geojson);
        	myWriter.close();
        }
        catch(IOException e)
        {
        	System.out.println("An error occured.");
        	e.printStackTrace();
        }
       
    } //main
} //App class