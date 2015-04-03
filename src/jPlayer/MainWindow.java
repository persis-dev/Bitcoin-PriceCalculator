package jPlayer;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Border;
import com.googlecode.lanterna.gui.Border.Bevel;
import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.Component.Alignment;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.component.Panel.Orientation;
import com.googlecode.lanterna.gui.component.Table;
import com.googlecode.lanterna.gui.component.TextBox;
import com.googlecode.lanterna.gui.layout.LayoutParameter;
import com.googlecode.lanterna.input.Key;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainWindow
extends Window
implements Runnable
{
	private static final Pattern DOUBLE_PATTERN = Pattern.compile(
			"[\\x00-\\x20]*[+-]?(NaN|Infinity|((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*");
	DecimalFormat db = new DecimalFormat("##.######");
	String[] currencies = { "USD", "ISK", "HKD", "TWD", "CHF", "EUR", "DKK", "CLP", "CAD", "CNY", "THB", "AUD", "SGD", "KRW", "JPY", "PLN", "GBP", "SEK", "NZD", "BRL", "RUB" };
	String currentAddressString = "1HDpq3oTzrFV49cTa31o6tFYnzJ6ooBjS4";
	Button changeCurrency = null;
	Label[] currenciesValue = new Label[this.currencies.length];
	Label latestHash = null;
	Label currentAddress = null;
	Label balance = null;
	Label confirmationLabel = null;
	Label sent = null;
	Label recieved = null;
	Label err = null;
	Label firstseen = null;
	Label eta = null;
	Panel mainPanel = null;
	Panel sidePanel = null;
	TextBox amtInput = null;
	Table ticker = null;
	BufferedReader serverAPI = Blockchain.getPage("https://blockchain.info/ticker");
	float amountOfBTC = 1.0F;
	public static String currency = "USD";
	int tickRate = 100;
	int confirmations = 6;
	public static final int BUFFER_SIZE = 1000;

	public MainWindow()
			throws NumberFormatException, IOException
	{
		super("BTC Calculator");
		this.mainPanel = new Panel(new Border.Bevel(true), Panel.Orientation.VERTICAL);
		//this.sidePanel = new Panel(new Border.Bevel(true), Panel.Orientation.VERTICAL);
		this.mainPanel.setAlignment(Component.Alignment.CENTER);
		this.ticker = new Table();
		int i = 0;
		String[] arrayOfString;
		int j = (arrayOfString = this.currencies).length;
		for (i = 0; i < j; i++)
		{
			System.out.println("DEBUG +"+i);
			String s = arrayOfString[i];
			this.ticker.addRow(new Component[] { this.currenciesValue[i] = new Label(String.format("%s BTC >> %s%s [%s]", new Object[] { this.db.format(this.amountOfBTC), this.db.format(parseTickerPrice(s, this.serverAPI) * this.amountOfBTC), parseSymbol(s, this.serverAPI), s })) });
			//i++;
		}
		i=0;
		this.ticker.addShortcut('V', true, false, new Action()
		{
			public void doAction()
			{
				try
				{
					System.out.println("shortcutted!");
					MainWindow.this.amtInput.setText((String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
				}
				catch (HeadlessException|UnsupportedFlavorException|IOException e)
				{
					e.printStackTrace();
				}
			}
		});
		this.ticker.setColumnPaddingSize(20);
		this.amtInput = new TextBox();
		this.amtInput.setAlignment(Component.Alignment.CENTER);
		addComponent(this.mainPanel, new LayoutParameter[0]);
		this.mainPanel.addComponent(this.ticker, new LayoutParameter[0]);
		addComponent(this.amtInput, new LayoutParameter[0]);
		setSoloWindow(true);
		addComponent(this.sidePanel, new LayoutParameter[0]);
		//this.sidePanel.addComponent(new Label("        Statistics         "), new LayoutParameter[0]);
		this.currentAddress = new Label();
		this.currentAddress.setText(String.format("Current address: %s", new Object[] { this.currentAddressString }));
		this.latestHash = new Label();
		this.latestHash.setText(String.format("Latest Hash: %s", new Object[] { Blockchain.getLatestHash() }));
		this.balance = new Label();

		this.confirmationLabel = new Label();
		this.confirmationLabel.setText(String.format("Confirmation level: %d", new Object[] { Integer.valueOf(this.confirmations) }));
		this.eta = new Label();
		this.eta.setText(String.format("Estimated time for block discovery: %f", new Object[] { Double.valueOf(Blockchain.getETA()) }));
		this.err = new Label();
		this.sent = new Label();

		this.recieved = new Label();

		this.firstseen = new Label();

		/*this.sidePanel.addComponent(this.latestHash, new LayoutParameter[0]);
		this.sidePanel.addComponent(this.confirmationLabel, new LayoutParameter[0]);
		this.sidePanel.addComponent(this.eta, new LayoutParameter[0]);
		this.sidePanel.addComponent(this.currentAddress, new LayoutParameter[0]);
		this.sidePanel.addComponent(this.balance, new LayoutParameter[0]);
		this.sidePanel.addComponent(this.sent, new LayoutParameter[0]);
		this.sidePanel.addComponent(this.recieved, new LayoutParameter[0]);
		this.sidePanel.addComponent(this.firstseen, new LayoutParameter[0]);

		this.sidePanel.addComponent(this.err, new LayoutParameter[0]);
		*/
	}

	public void run()
	{
		int o = 0;
		Key b;
		for (;;)
		{
			String clipboard = "null";
			try
			{
				int k = 0;
				o++;
				if (this.getOwner().getScreen().getTerminal().readInput()!=null){
					if ((b=this.getOwner().getScreen().getTerminal().readInput()).getKind().toString().equals("Escape")){
						System.exit(-1);
					}
				}
				//clipboard = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
				this.serverAPI = Blockchain.getPage("https://blockchain.info/ticker");
				this.eta.setText(String.format("Estimated time for block discovery: %f", new Object[] { Double.valueOf(Blockchain.getETA()) }));
				Thread.sleep(this.tickRate);
				/*if (o > this.tickRate / 10)
				{
					this.currentAddressString = parseWhiteSpace(clipboard);
					this.latestHash.setText(String.format("Latest Hash: %s", new Object[] { Blockchain.getLatestHash() }));
					this.balance.setText(String.format("Balance: %s BTC", new Object[] { Float.valueOf(Float.parseFloat(Blockchain.getData(String.format("https://blockchain.info/q/addressbalance/%s?confirmations=%d", new Object[] { this.currentAddressString, Integer.valueOf(this.confirmations) }))) / 1.0E8F) }));
					this.currentAddress.setText(String.format("Current address: %s", new Object[] { this.currentAddressString }));
					this.confirmationLabel.setText(String.format("Confirmation level: %d", new Object[] { Integer.valueOf(this.confirmations) }));
					this.sent.setText(String.format("BTC sent from address: %f BTC", new Object[] { Float.valueOf(Float.parseFloat(Blockchain.getData(String.format("https://blockchain.info/q/getsentbyaddress/%s?confirmations=%d", new Object[] { this.currentAddressString, Integer.valueOf(this.confirmations) }))) / 1.0E8F) }));
					this.recieved.setText(String.format("BTC sent from address: %f BTC", new Object[] { Float.valueOf(Float.parseFloat(Blockchain.getData(String.format("https://blockchain.info/q/getreceivedbyaddress/%s?confirmations=%d", new Object[] { this.currentAddressString, Integer.valueOf(this.confirmations) }))) / 1.0E8F) }));
					this.firstseen.setText(String.format("Wallet created: %s GMT", new Object[] { parseDate(Long.parseLong(Blockchain.getData(String.format("https://blockchain.info/q/addressfirstseen/%s", new Object[] { this.currentAddressString })))) }));
					this.err.setText("");
					o = 0;
				 */
				if (isFloat(this.amtInput.getText())) {
					this.amountOfBTC = Float.parseFloat(this.amtInput.getText());
				}
				/*for (int i = 0; i < currencies.length; i++)
				{
					String s = currencies[i];
					currenciesValue[i].setText(String.format("%s BTC >> %s%s [%s]",db.format(amountOfBTC),db.format(parseTickerPrice(s, serverAPI) * amountOfBTC), parseSymbol(s, serverAPI), s ));
				}*/
				int i=0;
				for (String s:currencies){
					//System.out.println(i);
					currenciesValue[i].setText(String.format("%s BTC >> %s%s [%s]",db.format(amountOfBTC),db.format(parseTickerPrice(s, serverAPI)* amountOfBTC), parseSymbol(s, serverAPI), s));
					i++;
				}
			}
			catch(Exception e){
				
			}
		}
	}

	public static double parseTickerPrice(String currency, BufferedReader page)
			throws NumberFormatException, IOException
	{
		String s = getLineOnTicker(currency, page);
		return Double.parseDouble(s.substring(s.indexOf("15m") + 6, s.indexOf("last") - 3));
	}

	public static String parseSymbol(String currency, BufferedReader page)
			throws NumberFormatException, IOException
	{
		String s = getLineOnTicker(currency, page);
		return s.substring(s.indexOf("symbol") + 11, s.indexOf("}") - 1);
	}

	public static double currencyToBTC(String currency, int amount)
			throws NumberFormatException, IOException
	{
		return Double.parseDouble(Blockchain.getData(String.format("https://blockchain.info/tobtc?currency=%s&value=%d", new Object[] { currency, Integer.valueOf(amount) })));
	}

	public static boolean isFloat(String s)
	{
		return DOUBLE_PATTERN.matcher(s).matches();
	}

	public static String getLineOnTicker(String currency, BufferedReader serverAPI)
			throws IOException
	{
		String line = null;
		serverAPI.mark(1000);
		while ((line = serverAPI.readLine()) != null) {
			if (line.contains(currency))
			{
				serverAPI.reset();
				return line;
			}
		}
		return null;
	}

	public static String parseWhiteSpace(String s)
	{
		StringBuilder sb = new StringBuilder();
		char[] arrayOfChar;
		int j = (arrayOfChar = s.toCharArray()).length;
		for (int i = 0; i < j; i++)
		{
			char c = arrayOfChar[i];
			if (c != ' ') {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String parseDate(long l)
	{
		Date date = new Date(l);
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("Etc/GMT"));
		String formatted = format.format(date);
		return formatted;
	}
}
