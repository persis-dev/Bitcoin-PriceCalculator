package jPlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class Blockchain
{
  public static long getTotalHashrate()
    throws NumberFormatException, IOException
  {
    return (long) Float.parseFloat(getData("https://blockchain.info/q/hashrate"));
  }
  
  public static long getTotalMarket()
    throws NumberFormatException, IOException
  {
    return (long) (getTotalBTC() * getBTCPrice());
  }
  
  public static double getBTCPrice()
    throws NumberFormatException, IOException
  {
    return Double.parseDouble(getData("https://blockchain.info/q/24hrprice"));
  }
  
  public static String getBTCTicker(String currency)
    throws NumberFormatException, IOException
  {
    String line = null;
    BufferedReader page = getPage("https://blockchain.info/ticker");
    while ((line = page.readLine()) != null) {
      if (line.contains(currency)) {
        return line;
      }
    }
    return null;
  }
  
  public static long getBlockReward()
    throws NumberFormatException, IOException
  {
    return Long.parseLong(getData("https://blockchain.info/q/bcperblock")) / 100000000L;
  }
  
  public static long getTotalBTC()
    throws NumberFormatException, IOException
  {
    return Long.parseLong(getData("https://blockchain.info/q/totalbc")) / 100000000L;
  }
  
  public static float getProbability()
    throws IOException
  {
    return Float.parseFloat(getData("https://blockchain.info/q/probability"));
  }
  
  public static String getLatestHash()
    throws IOException
  {
    return getData("https://blockchain.info/q/latesthash");
  }
  
  public static double getETA()
    throws IOException
  {
    return Double.parseDouble(getData("https://blockchain.info/q/eta"));
  }
  
  public static String getData(String url)
    throws IOException
  {
    return new BufferedReader(new InputStreamReader(new URL(url).openStream())).readLine();
  }
  
  public static BufferedReader getPage(String url) throws MalformedURLException, IOException
  {
    try {
		return new BufferedReader(new InputStreamReader(new URL(url).openStream()));
	} catch (IOException e) {
		System.out.println("Banned from blockchain :( too many API requests");
	}
	return null;
  }
}
